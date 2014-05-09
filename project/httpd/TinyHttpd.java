package httpd;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class TinyHttpd {
	private static final int PORT = 8888;
	private ServerSocket serverSocket;
	private StaticThreadPool pool = new StaticThreadPool(6, new WaitingRunnableQueue(false), false);
	private volatile boolean done = false;

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
					pool.execute( new HttpHandler(serverSocket, client) );
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
		TinyHttpd server = new TinyHttpd();
		server.init();
	}

}