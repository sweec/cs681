package hw16_17;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Pattern;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.File;

public class TinyHttpd2 {
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
					executeCommand( client );
				}
			} catch (SocketException e) {
				System.out.println("Interrupted, stop.");
			} finally {
				serverSocket.close();
			}
		}
		catch(IOException exception){
			exception.printStackTrace();
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
					if (getP.matcher(line).matches())
						tokens = line.split("\\s+");
					line = in.readLine();
				}
				System.out.println(line);

				if (tokens != null && tokens.length > 2 && tokens[1].startsWith("/")) {
					if (tokens[1].equals("/"))
						tokens[1] = "index.html";
					else
						tokens[1] = tokens[1].substring(1);
					String type = getFileType(tokens[1]);
					File file = new File(tokens[1]);
					System.out.println(file.getName() + " requested.");
					if (type == null)
						System.out.println("Not supported type");
					else
						sendFile(out, file, type);
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
	
	public static void main(String[] args) {
		final TinyHttpd2 server = new TinyHttpd2();
		new Thread(new Runnable() {

			@Override
			public void run() {
				server.init();
			}
			
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					server.serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] paths = {
						"http://localhost:8888/",
				};
				for (String path:paths)
					HttpClientGet.get(path);
			}
			
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] paths = {
						"http://localhost:8888/",
						"http://localhost:8888/a.jpg",
						"http://localhost:8888/b.png",
				};
				for (String path:paths)
					HttpClientHead.head(path);
			}
			
		}).start();
	}

}