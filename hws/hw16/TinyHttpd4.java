package hw16;

import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class TinyHttpd4 {
	private static final int PORT = 8888;
	private ServerSocket serverSocket;

	public void init() {
		try {
			try {
				serverSocket = new ServerSocket(PORT);
				System.out.println("Socket created.");
			
				while(true) {	
					System.out.println( "Listening to a connection on the local port " +
										serverSocket.getLocalPort() + "..." );
					Socket client = serverSocket.accept();
					System.out.println( "\nA connection established with the remote port " + 
										client.getPort() + " at " +
										client.getInetAddress().toString() );
					new Thread( new Worker(client) ).start();
				}
			}finally {
				serverSocket.close();
			}
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}

	private class Worker implements Runnable {
		private Socket client;
		
		public Worker(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
				PrintStream out = new PrintStream( client.getOutputStream() );
				System.out.println( "I/O setup done" );
				try {
					while (true) {
						try {
							boolean done = executeCommand(new HttpExchange(client, serverSocket, in, out));
							if (done)
								break;
						} catch (Exception e) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				} finally {
					in.close();
					out.close();
					client.close();
					System.out.println( "A connection is closed." );				
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean executeCommand( HttpExchange he) {
		String command = he.getRequestCommand();
		if (command == null)
			return true;
		String url = he.getRrequestURI();
		if(!url.startsWith("/"))
			he.makeErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST);
		else {
			if (url.equals("/"))
				url = "index.html";
			else
				url = url.substring(1);
			String type = HttpUtility.getFileType(url);
			File file = new File(url);
			System.out.println(file.getName() + " requested.");
			if (!file.exists())
				he.makeErrorResponse(HttpURLConnection.HTTP_NOT_FOUND);
			else if (HttpUtility.isGetCommand(command)) {
				if (type == null)
					he.makeErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
				else {
					setResponseHeader(he, file, type);  
					sendFile(he, file, type);
				}
			} else if (HttpUtility.isHeadCommand(command)) {
				setResponseHeader(he, file, type);
			} else if (HttpUtility.isPostCommand(command)) {
				System.out.println(he.getRequestBody());
				he.makeSuccessfulResponse();
			} else
				he.makeErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
			he.sendResponse();
		}
		if (he.isPersistent())
			return false;
		else
			return true;
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
	
	public static void main(String[] args) {
		TinyHttpd4 server = new TinyHttpd4();
		server.init();
	}

}