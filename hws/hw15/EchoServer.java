package hw15;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;

public class EchoServer
{
	private static final int BANKPORT = 8888;
	private ServerSocket serverSocket;
	
	public void init()
	{
		try
		{
			try
			{
				serverSocket = new ServerSocket(BANKPORT);
				System.out.println("Socket created.");
			
				while(true)
				{	
					System.out.println( "Listening for a connection on the local port " +
										serverSocket.getLocalPort() + "..." );
					Socket socket = serverSocket.accept();
					System.out.println( "\nA connection established with the remote port " + 
										socket.getPort() + " at " +
										socket.getInetAddress().toString() );
					echoInput( socket );
				}
			} catch (SocketException e) {
				System.out.println("Interrupted, stop.");
			} finally {
				serverSocket.close();
			}
		}
		catch(IOException exception){}
	}
	
	private void echoInput( Socket socket )
	{
		try
		{
			try
			{
				Scanner in = new Scanner(socket.getInputStream());
				PrintWriter out = new PrintWriter( socket.getOutputStream() );
				System.out.println( "I/O setup done" );
				String line;
				while (!(line = in.nextLine()).equals("QUIT")) {
					System.out.println(line);
					out.println(line);
					out.flush();
				}
			} finally {
				socket.close();
				System.out.println( "A connection is closed." );
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final EchoServer server = new EchoServer();
		
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
				String serverip = "localhost";
				EchoClient client = new EchoClient();
				client.init(serverip);
				
				client.sendCommand( "BALANCE\n" );
				System.out.println( client.getResponse() );
				
				client.sendCommand( "DEPOSIT 100\n" );
				System.out.println( client.getResponse() );
				
				client.sendCommand( "WITHDRAW 50\n" );
				System.out.println( client.getResponse() );
				
				client.sendCommand( "QUIT\n" );
			}
			
		}).start();
	}
}
