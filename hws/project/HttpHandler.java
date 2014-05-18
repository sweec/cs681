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
import java.util.ArrayList;

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

	private void executeCommand(HttpExchange ex) {
		String command = ex.getRequestCommand();
		String url = ex.getRequestURI();
		if (command == null || url == null || !url.startsWith("/")) {
			ex.setErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST);
			ex.sendResponse();
			return;
		}
		if (url.equals("/"))
			url = "index.html";
		else
			url = url.substring(1);
		if (url.startsWith("site/admin")) {
			handleAdminUrl(ex, url);
			return;
		}
		if (HttpUtility.isPostCommand(command)) {
			System.out.println("Received POST request with data: "+ex.getRequestBody());
			ex.setSuccessResponse("<html><body><h2>Post "+ex.getRequestURI()+"<br>"+ex.getRequestBody()+"</h2></body></html>", "text/html");
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
				ex.setSuccessResponse(server.getFileCache().fetch(url), type, file.lastModified());
			}
		} else if (HttpUtility.isHeadCommand(command)) {
			ex.setSuccessResponseHeader(type, file.length(), file.lastModified());
		} else
			ex.setErrorResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
		ex.sendResponse();
	}
	
	private void handleAdminUrl(HttpExchange ex, String url) {
		if (HttpUtility.isPostCommand(ex.getRequestCommand())) {
			String[] cache = server.getFileCache().toString().split(" ");
			String auth = server.getAuthenticator().toString();
			for (String input:ex.getRequestBody().split("&")) {
				String[] kv = input.split("=");
				if (kv[0].equalsIgnoreCase("cache"))
					cache[0] = kv[1];
				else if (kv[0].equalsIgnoreCase("size"))
					cache[1] = kv[1];
				else if (kv[0].equalsIgnoreCase("auth"))
					auth = kv[1];
			}
			server.setFileCache(cache[0], Integer.parseInt(cache[1]));
			server.setAuthenticator(auth);
			url = "site/admin/page1.html";
		}
		if (url.equals("site/admin/page1.html")) {
			StringBuilder content = new StringBuilder();
			content.append("<html><body>\n<h1>Server Management</h1>\n");
			content.append("<h2>URL Access Statistics</h2>\n<table border=\"1\">\n");
			content.append("<tr><td>URL</td><td>count</td><td align=\"center\">last visited</td></tr>\n");
			ArrayList<String> urls = new ArrayList<String>();
			ArrayList<Integer> counts = new ArrayList<Integer>();
			ArrayList<String> ages = new ArrayList<String>();
			AccessCounter.getInstance().summary(urls, counts, ages);
			for (int i=0;i<urls.size();i++) {
				content.append("<tr><td>"+urls.get(i)+"</td><td align=\"center\">"+counts.get(i)+"</td><td>"+ages.get(i)+"</td></tr>\n");
			}
			content.append("</table><br>\n");
			
			content.append("<h2>Server Policies</h2>\n<form method=\"post\" action=\"upload\"><table border=\"1\">\n");
			
			content.append("<tr><th>Policy</th><th>Current</th><th>Change to</th></tr>");
			content.append("<tr><td>File Cache</td><td>"+server.getFileCache()+"</td>");
			content.append("<td><select name=\"cache\">");
			content.append("<option value=\"LFU\">LFU</option>");
			content.append("<option value=\"LRU\">LRU</option>");
			content.append("<option value=\"NULL\">None</option></select>");
			content.append("Size: <input type=\"text\" name=\"size\" value=\""+server.getFileCache().toString().split(" ")[1]+"\"></td></tr>\n");
			content.append("<tr><td>Authenticator</td><td>"+server.getAuthenticator()+"</td>");
			content.append("<td><select name=\"auth\">");
			content.append("<option value=\"BASIC\">BASIC</option>");
			content.append("<option value=\"DIGEST\">DIGEST</option>");
			content.append("<option value=\"NULL\">None</option>");
			content.append("</td></tr>\n");
			content.append("<tr><td>Thread Pool Size</td><td align=\"center\">"+server.getPoolSize()+"</td><td align=\"center\"><input type=\"submit\" value=\"Apply\"></td></tr>\n");
			content.append("</table>\n</form><br>");
			
			content.append("<h2>Actions</h2>\n<table border=\"1\">\n");
			content.append("<tr><td><a href=\"stop.html\" style=\"color: #FF0000\">stop the server!</a></td></tr>\n");
			content.append("</table>\n");
			content.append("</body></html>");
			ex.setSuccessResponse(content.toString(), "text/html");
			ex.sendResponse();
			return;
		}
		if (url.equals("site/admin/stop.html")) {
			ex.setSuccessResponse("<html><body><h1>Server is stopped.</h1></body></html>", "text/html");
			ex.sendResponse();
			server.setDone();
			return;
		}
	}
}
