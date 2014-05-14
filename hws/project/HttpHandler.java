package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HttpHandler implements Runnable {
	private Httpd server;
	private ServerSocket serverSocket;
	private Socket client;
	//private long MaxIdleTime = 900000;
	
	public HttpHandler(Httpd server, ServerSocket serverSocket, Socket client) {
		this.server = server;
		this.serverSocket = serverSocket;
		this.client = client;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
			PrintStream out = new PrintStream( client.getOutputStream() );
			System.out.println( "I/O setup done" );
			//long before = System.currentTimeMillis();
			try {
				HttpExchange ex = null;
				// authenticate
				//while (true) {
					ex = getNewHttpExchange(in, out);
					if (ex == null) {
						//if ((System.currentTimeMillis()-before)>MaxIdleTime)
							//break;
					} else if (server.getAuthenticator().authenticate(ex)) {
						executeCommand(ex);
						//before = System.currentTimeMillis();
						//break;
					} else if (!ex.isPersistent()) {
						//break;
					}
				//}
				/*if (ex == null || !ex.isPersistent())
					return;
				while (true) {
					ex = getNewHttpExchange( in, out);
					if (ex == null) {
						//if ((System.currentTimeMillis()-before)>MaxIdleTime)
							break;
					} else {
						executeCommand(ex);
						//before = System.currentTimeMillis();
						if (!ex.isPersistent())
							break;
					}
				}*/
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

	private HttpExchange getNewHttpExchange(BufferedReader in, PrintStream out) {
		HttpExchange ex = null;
		try {
			ex = new HttpExchange(client, serverSocket, in, out);
		} catch(SocketTimeoutException exception) {
			System.out.println("Socket read time out.");
			ex = null;
		} catch (Exception e) {
			System.out.println("Something error happened");
			ex = null;
		}
		return ex;
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
			ex.makeErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST);
			ex.sendResponse();
			return;
		}
		if (url.equals("/"))
			url = "index.html";
		else
			url = url.substring(1);
		// somehow my browser turns post request into get with parameters
		if (HttpUtility.isGetCommand(command)
				&& (url.contains("?") || url.contains("="))) {
			ex.setSuccessResponse("<html><body>Get converted from Post: <p>"+url.substring(url.indexOf("?")+1)+"</p></body></html>", "text/html");
			ex.sendResponse();
			return;
		}
		String type = HttpUtility.getFileType(url);
		File file = new File(url);
		System.out.println(file.getName() + " requested.");
		if (!file.exists()) {
			ex.makeErrorResponse(HttpURLConnection.HTTP_NOT_FOUND);
		} else if (HttpUtility.isGetCommand(command)) {
			if (type == null)
				ex.makeErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
			else {
				ex.setSuccessResponse(file, type);
			}
		} else if (HttpUtility.isHeadCommand(command)) {
			ex.setSuccessResponseHeader(file, type);
		} else
			ex.makeErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
		ex.sendResponse();
	}
}
