package hw16_17;

import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class TinyHttpd4 {
	private static final int PORT = 8888;
	private ServerSocket serverSocket = null;

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
			} catch (SocketException e) {
				System.out.println("Interrupted, stop.");
			} finally {
				if (serverSocket != null)
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
					long before = System.currentTimeMillis();
					while (true) {
						try {
							HttpExchange ex = new HttpExchange(client, serverSocket, in, out);
							executeCommand(ex);
							before = System.currentTimeMillis();
							if (!ex.isPersistent())
								break;
						} catch(SocketTimeoutException exception) {
							System.out.println("Client read time out.");
							break;
						} catch (SocketException e) {
							System.out.println("Thread "+Thread.currentThread().getId()+": Interrupted, stop.");
							break;
						} catch (IOException e) {
							System.out.println("Thread "+Thread.currentThread().getId()+": Interrupted, stop.");
							break;
						} catch (Exception e) {
							try {
								long current = System.currentTimeMillis();
								if (current-before>1000) {
									System.out.println("Client read time out.");
									break;
								}
								Thread.sleep(100);
								//System.out.println("hit here: "+e.getMessage());
							} catch (InterruptedException e1) {
								System.out.println("Interrupted, stop");
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void executeCommand( HttpExchange ex) {
		String command = ex.getRequestCommand();
		String url = ex.getRrequestURI();
		if(!url.startsWith("/"))
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
			ex.sendResponse();
		}
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
		final TinyHttpd4 server = new TinyHttpd4();
		new Thread(new Runnable() {

			@Override
			public void run() {
				server.init();
				System.out.println("Sever is over");
			}
			
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] paths = {
						"http://localhost:8888/",
						"http://localhost:8888/fakefile",
						"http://localhost:8888/fakeDir/a.jpg"
				};
				for (String path:paths)
					HttpClientGet.get(path);
				System.out.println("Get test done");
			}
			
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] paths = {
						"http://localhost:8888/",
				};
				for (String path:paths)
					HttpClientHead.head(path);
				System.out.println("Head test done");
			}
			
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					server.serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Stop command issued");
			}
			
		}).start();
		
	}

}