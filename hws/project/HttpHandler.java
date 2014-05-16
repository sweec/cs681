package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class HttpHandler implements StoppableRunnable {
	private Httpd server;
	private ServerSocket serverSocket;
	private Socket client;
	private volatile boolean done = false;
	
	public HttpHandler(Httpd server, ServerSocket serverSocket, Socket client) {
		this.server = server;
		this.serverSocket = serverSocket;
		this.client = client;
	}

	@Override
	public void stop() {
		done = true;
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("Thread "+Thread.currentThread().getId()+" start");
		try {
			BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
			PrintStream out = new PrintStream( client.getOutputStream() );
			System.out.println( "I/O setup done" );
			try {
				while (!done) {
					try {
						HttpExchange ex = new HttpExchange(client, serverSocket, in, out);
						if (server.getAuthenticator().authenticate(ex)) {
							executeCommand(ex);
							//System.out.println("Thread "+Thread.currentThread().getId()+" execute command");
						}
						// stop if thread per resource
						if (!ex.isPersistent())
							break;
					} catch(SocketTimeoutException exception) {
						System.out.println("Client read time out.");
						break;
					} catch (SocketException e) {
						System.out.println("Thread "+Thread.currentThread().getId()+": Interrupted, stop.");
						break;
					} catch (NullPointerException e) {
						// nothing to read yet, try later
						if (done) break;
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							System.out.println("Thread "+Thread.currentThread().getId()+": Interrupted, stop.");
							break;
						}
					}
				}
			} finally {
				in.close();
				out.close();
				client.close();
				System.out.println( "A connection is closed." );				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeCommand( HttpExchange ex) {
		String command = ex.getRequestCommand();
		if (HttpUtility.isPostCommand(command)) {
			System.out.println(ex.getRequestBody());
			ex.setSuccessResponse("<html><body>Post input: <p>"+ex.getRequestBody()+"</p></body></html>", "text/html");
			ex.sendResponse();
			return;
		}
		String url = ex.getRrequestURI();
		if (command == null || url == null || !url.startsWith("/")) {
			ex.setErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST);
			ex.sendResponse();
			//System.out.println("hit here");
			return;
		}
		if (url.equals("/"))
			url = "index.html";
		else
			url = url.substring(1);
		if (url.equals("site/admin/stop.html")) {
			ex.setSuccessResponse("<html><body><p>Server is stopped.</p></body></html>", "text/html");
			ex.sendResponse();
			server.setDone();
			return;
		}
		// somehow my browser turns post request into get with parameters
		if (HttpUtility.isGetCommand(command) && url.contains("?")) {
			ex.setSuccessResponse("<html><body>Get converted from Post: <p>"+url.substring(url.indexOf("?")+1)+"</p></body></html>", "text/html");
			ex.sendResponse();
			return;
		}
		String type = HttpUtility.getFileType(url);
		File file = new File(url);
		System.out.println(file.getName() + " requested.");
		if (!file.exists()) {
			ex.setErrorResponse(HttpURLConnection.HTTP_NOT_FOUND);
		} else if (HttpUtility.isGetCommand(command)) {
			if (type == null)
				ex.setErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
			else {
				ex.setSuccessResponse(file, type);
			}
		} else if (HttpUtility.isHeadCommand(command)) {
			ex.setSuccessResponseHeader(file, type);
		} else
			ex.setErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
		ex.sendResponse();
	}
}
