package hw11;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Httpd implements Runnable {
	private int PORT = 8888;
	private ServerSocket serverSocket = null;
	private AccessCounter counter = new AccessCounter();
	private StaticThreadPool pool = new StaticThreadPool(3, new WaitingRunnableQueue2(false), false);
	private FileCache cache = new LRUFileCache();
	private volatile boolean done = false;
	private static Httpd instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	private Httpd(){}
	public static Httpd getInstance() {
		if (instance == null) {
			lock.lock();
			if (instance == null) {
				Httpd inst = new Httpd();
				instance = inst;
			}
			lock.unlock();
		}
		return instance;
	}

	public void setDone(boolean done) {
		this.done = done;
		if (done)
			pool.shutdown();
	}
	
	public void setThreadPool(StaticThreadPool pool) {
		pool.shutdown();
		this.pool = pool;
	}
	
	public void setFileCache(FileCache cache) {
		this.cache = cache;
	}
	
	public void getFile(String path) {
		final String targetFile = path;
		pool.execute(new Runnable() {

			@Override
			public void run() {
				String content = cache.fetch(targetFile);
				counter.increment(targetFile);
				System.out.println("Thread "+Thread.currentThread().getId()+" visit "+targetFile+":\n"+content);
			}
			
		});
	}
	
	@Override
	public void run() {
		try {
			try {
				serverSocket = new ServerSocket(PORT);
				System.out.println("Socket created.");
			
				while(!done) {	
					System.out.println( "Listening to a connection on the local port " +
										serverSocket.getLocalPort() + "..." );
					Socket client = serverSocket.accept();
					System.out.println( "\nA connection established with the remote port " + 
										client.getPort() + " at " +
										client.getInetAddress().toString() );
					pool.execute(new FileLoader(client, this));
				}
			}finally {
				serverSocket.close();
			}
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}

	public void setPort(int port) {
		if (serverSocket == null || serverSocket.isClosed())
			PORT = port;
	}
	
	private class FileLoader implements Runnable {
		private Socket client;
		private Httpd server;
		
		public FileLoader(Socket client, Httpd server) {
			this.client = client;
			this.server = server;
		}

		@Override
		public void run() {
			try {
				try {
					client.setSoTimeout(30000);
					BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
					PrintStream out = new PrintStream( client.getOutputStream() );  
					System.out.println( "I/O setup done" );
					
					String line = in.readLine();
//					while ( in.ready() && line != null ) {
					while( line != null ) {
						System.out.println(line);
						if(line.equals("")) break;
						line = in.readLine();
					}
					System.out.println(line);

					File file = new File("index.html");
					System.out.println(file.getName() + " requested.");
					sendFile(out, file);

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
		
		private void sendFile(PrintStream out, File file){
			try{
				out.println("HTTP/1.0 200 OK");
				out.println("Content-Type: text/html");
				
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
	}
	
	public static void main(String[] args)
	{
		Httpd server = Httpd.getInstance();
		String files[] = {
				"site/a/page1.html", "site/a/page2.html",
				"site/b/page1.html", "site/b/page2.html",
				"site/c/page1.html", "site/c/page2.html"
		};
		Random g = new Random();
		for (int i=0;i<15;i++)
			server.getFile(files[g.nextInt(files.length)]);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		server.setDone(true);;
	}

}
