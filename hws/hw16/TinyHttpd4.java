package hw16;

import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.File;

public class TinyHttpd4 {
	private static final int PORT = 8888;
	private ServerSocket serverSocket;
	static Pattern getP = Pattern.compile("^GET.*");
	static Pattern headP = Pattern.compile("^HEAD.*");
	static Pattern htmlP = Pattern.compile(".*html$");
	static Pattern jpgP = Pattern.compile(".*jpg$");
	static Pattern pngP = Pattern.compile(".*png$");

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
			executeCommand(client);
		}
		
	}
	
	private void executeCommand( Socket client ){
		try {
			try {
				client.setSoTimeout(30000);
				BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
				PrintStream out = new PrintStream( client.getOutputStream() );  
				System.out.println( "I/O setup done" );
				
				String[] tokens = null;
				String line = in.readLine();
//				while ( in.ready() && line != null ) {
				while( line != null ) {
					System.out.println(line);
					if(line.equals("")) break;
					if (getP.matcher(line).matches()
							|| headP.matcher(line).matches())
						tokens = line.split("\\s+");
					line = in.readLine();
				}
				System.out.println(line);

				if (tokens == null || tokens.length < 2)
					sendErrorMessage(out, HttpURLConnection.HTTP_NOT_IMPLEMENTED);
				else if (!tokens[1].startsWith("/"))
					sendErrorMessage(out, HttpURLConnection.HTTP_BAD_REQUEST);
				else {
					if (tokens[1].equals("/"))
						tokens[1] = "index.html";
					else
						tokens[1] = tokens[1].substring(1);
					String type = getFileType(tokens[1]);
					File file = new File(tokens[1]);
					System.out.println(file.getName() + " requested.");
					if (file.exists()) {
						if (getP.matcher(tokens[0]).matches()) {
							if (type == null)
								sendErrorMessage(out, HttpURLConnection.HTTP_NOT_IMPLEMENTED);
							else
								sendFile(out, file, type);
						} else if (headP.matcher(tokens[0]).matches()) {
							sendHead(out, file, type);
						}
					} else
						sendErrorMessage(out, HttpURLConnection.HTTP_NOT_FOUND);
				}

				out.flush();
				out.close();
				in.close();
			}finally {
				client.close();
				System.out.println( "A connection is closed." );				
			}
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	} 
	
	private String getFileType(String name) {
		if (htmlP.matcher(name).matches())
			return "text/html";
		else if (jpgP.matcher(name).matches())
			return "image/jpg";
		else if (pngP.matcher(name).matches())
			return "image/png";
		else
			return null;
	}
	
	private void sendFile(PrintStream out, File file, String type){
		try{
			out.println("HTTP/1.0 200 OK");
			out.println("Content-Type: " + type);
			
			int len = (int) file.length();
			out.println("Content-Length: " + len);
			out.println("");  

			DataInputStream fin = new DataInputStream(new FileInputStream(file));
			byte buf[] = new byte[len];
			fin.readFully(buf);
			out.write(buf, 0, len);
			out.flush();
			fin.close();
		}catch(IOException exception){
			exception.printStackTrace();
		}         
	}
	
	private void sendHead(PrintStream out, File file, String type) {
		if (type == null)
			type = "Unsupported";
		out.println("HTTP/1.0 200 OK");
		out.println("Server: Java socket "+System.getProperty("os.name"));
		out.println("Content-Type: " + type);
		int len = (int) file.length();
		out.println("Content-Length: " + len);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		out.println("Date: "+sdf.format(System.currentTimeMillis()));
		out.println("Last-Modified: "+sdf.format(file.lastModified()));
		out.println("");  
	}
	
	private void sendErrorMessage(PrintStream out, int code) {
		out.println("HTTP/1.0 "+code+" "+getHttpStatus(code));
		out.println("");         
	}
	
	private static final HashMap<Integer, String> httpStatus = new HashMap<Integer, String>();
	static {
		httpStatus.put(HttpURLConnection.HTTP_OK, "OK");
		httpStatus.put(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request");
		httpStatus.put(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized (authorization required)");
		httpStatus.put(HttpURLConnection.HTTP_NOT_FOUND, "Not Found");
		httpStatus.put(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error");
		httpStatus.put(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "Not Implemented");
		httpStatus.put(HttpURLConnection.HTTP_UNAVAILABLE, "Service Unavailable");
	}
	private static String getHttpStatus(int code) {
		return httpStatus.get(code);
	}
	
	public static void main(String[] args) {
		TinyHttpd4 server = new TinyHttpd4();
		server.init();
	}

}