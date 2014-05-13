package project;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class Httpd {
	private int PORT = 8888;
	private ServerSocket serverSocket;
	private StaticThreadPool pool = null;
	private Authenticator authenticator = null;
	private volatile boolean done = false;

	public Httpd(String[] args) {
		if (args.length > 0) {
			Integer port = Integer.parseInt(args[0]);
			if (port != null)
				PORT = port;
		}
		if (args.length > 1) {	
			if (args[1].equals("-b"))
				authenticator = new BasicAuthenticator();
			else if (args[1].equals("-d"))
				authenticator = new DigestAuthenticator();
		}
		if (authenticator == null)
			authenticator = new NullAuthenticator();
		pool = new StaticThreadPool(6, new WaitingRunnableQueue(false), false);
	}
	
	public Authenticator getAuthenticator() {
		return authenticator;
	}
	
	public void init() {
		try {
			try {
				serverSocket = new ServerSocket(PORT);
				System.out.println("Server socket created.");
			
				while(!done) {	
					System.out.println( "Listening to a connection on the local port " +
										serverSocket.getLocalPort() + "..." );
					Socket client = serverSocket.accept();
					System.out.println( "\nA connection established with the remote port " + 
										client.getPort() + " at " +
										client.getInetAddress().toString() );
					pool.execute( new HttpHandler(this, serverSocket, client) );
				}
			}finally {
				serverSocket.close();
				System.out.println("Server socket closed.");
				pool.shutdown();
			}
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}

	public void setDone(boolean value) {
		this.done = value;
	}
	
	public static void main(String[] args) {
		Httpd server = new Httpd(args);
		server.init();
	}

}