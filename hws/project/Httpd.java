package project;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.IOException;

public class Httpd {
	private int PORT = 8888;
	private ServerSocket serverSocket;
	private StaticThreadPool pool = null;
	private Authenticator authenticator = null;
	private volatile boolean done = false;

	public Httpd(String[] args) {
		if (args.length > 0) {
			try {
			Integer port = Integer.parseInt(args[0]);
			if (port != null)
				PORT = port;
			} catch (NumberFormatException e) {
				System.out.println("port number not valid, use 8888");
			}
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
		AppInfo.getInstance().load();
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
					try {
						Socket client = serverSocket.accept();
						System.out.println( "\nA connection established with the remote port " + 
								client.getPort() + " at " + client.getInetAddress() );
						pool.execute( new HttpHandler(this, serverSocket, client) );
					} catch (SocketException e) {
					}
				}
			}finally {
				serverSocket.close();
				System.out.println("Server socket closed.");
			}
		} catch(IOException exception){
			exception.printStackTrace();
		}
		AppInfo.getInstance().save();
	}

	public void setDone() {
		this.done = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pool.shutdown();
	}
	
	private static void setup() {
		AppInfo app = AppInfo.getInstance();
		String[] users = {"a", "b", "c"};
		String[] groups = {"group a", "group b", "group c"};
		String[] html1s = {"/site/a/page1.html", "/site/b/page1.html", "/site/c/page1.html"};
		String[] html2s = {"/site/a/page2.html", "/site/b/page2.html", "/site/c/page2.html"};
		for (int i=0;i<users.length;i++) {
			app.addBasicUser(users[i], users[i]);
			app.addDigestUser(users[i], users[i], "digest realm");
			app.addToGroup(users[i], groups[i]);
			app.setProperty(html1s[i], groups[i]);
			app.setProperty(html2s[i], groups[i]);
		}
		app.addBasicUser("admin", "admin");
		app.addDigestUser("admin", "admin", "digest realm");
		app.addToGroup("admin", "root");
		app.setProperty("/site/admin/page1.html", "root");
		app.setProperty("/site/admin/stop.html", "root");
	}
	
	public static void main(String[] args) {
		Httpd server = new Httpd(args);
		setup();
		server.init();
	}

}