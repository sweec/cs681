package project;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.IOException;

public class Httpd {
	private int PORT = 8888;
	private ServerSocket serverSocket;
	private StaticThreadPool pool = null;
	private int poolSize = 6;
	private Authenticator authenticator = null;
	private FileCache fileCache = null;
	private volatile boolean done = false;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public Httpd(String[] args) {
		AppInfo.getInstance().load();
		
		pool = new StaticThreadPool(poolSize, new WaitingRunnableQueue(false), false);
		
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
		
		if (args.length > 2) {
			if (args[2].equalsIgnoreCase("LFU"))
				fileCache = new LFUFileCache(5);
			else if (args[2].equalsIgnoreCase("LRU"))
				fileCache = new LRUFileCache(5);
		}
		if (fileCache == null)
			fileCache = new NullFileCache();
	}
	
	public int getPoolSize() {
		return poolSize;
	}
	
	public FileCache getFileCache() {
		lock.readLock().lock();
		try {
			return fileCache;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Authenticator getAuthenticator() {
		lock.readLock().lock();
		try {
			return authenticator;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void setFileCache(String policy, Integer size) {
		if (!getFileCache().toString().contains(policy)) {
			lock.writeLock().lock();
			if (policy.equalsIgnoreCase("LFU"))
				fileCache = new LFUFileCache(size);
			else if (policy.equalsIgnoreCase("LRU"))
				fileCache = new LRUFileCache(size);
			else
				fileCache = new NullFileCache();
			lock.writeLock().unlock();
		}
	}
	
	public void setAuthenticator(String policy) {
		if (!getAuthenticator().toString().contains(policy)) {
			lock.writeLock().lock();
			if (policy.equalsIgnoreCase("BASIC"))
				authenticator = new BasicAuthenticator();
			else if (policy.equalsIgnoreCase("DIGEST"))
				authenticator = new DigestAuthenticator();
			else
				authenticator = new NullAuthenticator();
			lock.writeLock().unlock();
		}
	}
	
	public void run() {
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
	
	public static void setup() {
		AppInfo app = AppInfo.getInstance();
		String[] users = {"a", "b", "c"};
		String[] groups = {"group a", "group b", "group c"};
		String[] html1s = {"/site/a/page1.html", "/site/b/page1.html", "/site/c/page1.html"};
		String[] html2s = {"/site/a/page2.html", "/site/b/page2.html", "/site/c/page2.html"};
		for (int i=0;i<users.length;i++) {
			app.addBasicUser(users[i], users[i]);
			app.addDigestUser(users[i], users[i], "digest realm");
			app.addToRealm(users[i], groups[i]);
			app.setProperty(html1s[i], groups[i]);
			app.setProperty(html2s[i], groups[i]);
		}
		app.addBasicUser("admin", "admin");
		app.addDigestUser("admin", "admin", "digest realm");
		app.addToRealm("admin", "root");
		app.setProperty("/site/admin/page1.html", "root");
		app.setProperty("/site/admin/stop.html", "root");
	}
	
	public static void main(String[] args) {
		Httpd server = new Httpd(args);
		setup();
		server.run();
	}

}