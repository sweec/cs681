package project;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	private long MaxIdleTime = 900000;
	
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
			try {
				HttpExchange ex = null;
				// authenticate
				while (true) {
					ex = new HttpExchange(client, serverSocket, in, out);
					if (server.getAuthenticator().authenticate(ex)) {
						executeCommand(ex);
						break;
					} else if (!ex.isPersistent())
					 return;
				}
				if (!ex.isPersistent())
					return;
				long idleTime = 0;
				while (true) {
					try {
						ex = new HttpExchange(client, serverSocket, in, out);
						executeCommand(ex);
						idleTime = 0;
					} catch(SocketTimeoutException exception) {
						System.out.println("Socket read time out.");
						idleTime += ex.getTimeOut();
						if (idleTime > MaxIdleTime) {
							System.out.println("Session time out");
							break;
						}
					} catch (Exception e) {
						System.out.println("Something error happened");
						break;
					}
					if (!ex.isPersistent())
						break;
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
		String url = ex.getRrequestURI();
		if (command == null || url == null || !url.startsWith("/"))
			ex.makeErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST);
		else {
			if (url.equals("/"))
				url = "index.html";
			else
				url = url.substring(1);
			String type = HttpUtility.getFileType(url);
			File file = new File(url);
			System.out.println(file.getName() + " requested.");
			if (!file.exists())
				ex.makeErrorResponse(HttpURLConnection.HTTP_NOT_FOUND);
			else if (HttpUtility.isGetCommand(command)) {
				if (type == null)
					ex.makeErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
				else {
					setResponseHeader(ex, file, type);  
					sendFile(ex, file, type);
				}
			} else if (HttpUtility.isHeadCommand(command)) {
				setResponseHeader(ex, file, type);
			} else if (HttpUtility.isPostCommand(command)) {
				System.out.println(ex.getRequestBody());
				ex.makeSuccessfulResponse();
			} else
				ex.makeErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
		}
		ex.sendResponse();
	}

	private void sendFile(HttpExchange he, File file, String type){
		try{
			int len = (int) file.length();
			DataInputStream fin = new DataInputStream(new FileInputStream(file));
			byte buf[] = new byte[len];
			fin.readFully(buf);
			he.setResponseBody(buf);;
			fin.close();
		}catch(IOException exception){
			exception.printStackTrace();
		}         
	}

	private void setResponseHeader(HttpExchange he, File file, String type) {
		if (type == null)
			type = "Unsupported";
		he.makeSuccessfulResponse();
		he.setResponseHeader("Server", "Java socket "+System.getProperty("os.name"));
		he.setResponseHeader("Content-Type", type);
		int len = (int) file.length();
		he.setResponseHeader("Content-Length", String.valueOf(len));
		he.setResponseHeader("Date", HttpUtility.getGMT(System.currentTimeMillis()));
		he.setResponseHeader("Last-Modified", HttpUtility.getGMT(file.lastModified()));
	}
}
