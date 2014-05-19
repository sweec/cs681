package hw15;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;

public class BankServer
{
	private static final int BANKPORT = 8888;
	private BankAccount account;
	private ServerSocket serverSocket;
	
	public BankServer()
	{
		account = new BankAccount();
	}
	
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
					new Thread( new Worker(socket) ).start();
				}
			} catch (SocketException e) {
				System.out.println("Interrupted, stop.");
			} finally {
				serverSocket.close();
			}
		}
		catch(IOException exception){}
	}
	
	private class Worker implements Runnable {
		private Socket socket;
		
		public Worker(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			executeCommand(socket);
		}
		
	}
	
	private void executeCommand( Socket socket )
	{
		try
		{
			try
			{
				Scanner in = new Scanner( socket.getInputStream() );
				PrintWriter out = new PrintWriter( socket.getOutputStream() );
				System.out.println( "I/O setup done" );
				
				while(true)
				{
					if( in.hasNext() )
					{
						String command = in.next();
						if( command.equals("QUIT") )
						{
							System.out.println( "QUIT: Connection being closed." );
							out.println( "QUIT accepted. Connection being closed." );
							out.close();
							return;
						}
						accessAccount( command, in, out );
					}
				}
			}	
			finally
			{
				socket.close();
				System.out.println( "A connection is closed." );
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private void accessAccount( String command, Scanner in, PrintWriter out )
	{
		double amount;
		if( command.equals("DEPOSIT") )
		{
			amount = in.nextDouble();
			account.deposit( amount );
			System.out.println( "DEPOSIT: Current balance: " + account.getBalance() );
			out.println( "DEPOSIT Done. Current balance: " + account.getBalance() );
		}
		else if( command.equals("WITHDRAW") )
		{
			amount = in.nextDouble();
			account.withdraw( amount );
			System.out.println( "WITHDRAW: Current balance: " + account.getBalance() );
			out.println( "WITHDRAW Done. Current balance: " + account.getBalance() );
		}
		else if( command.equals("BALANCE") )
		{
			System.out.println( "BALANCE: Current balance: " + account.getBalance() );
			out.println( "BALANCE accepted. Current balance: " + account.getBalance() );
		}
		else
		{
			System.out.println( "Invalid Command" );
			out.println( "Invalid Command. Try another command." );
		}
		out.flush();
	}
	
	public static void main(String[] args) {
		final BankServer server = new BankServer();
		
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
					Thread.sleep(3100);
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
		for (int i=0;i<3;i++)
		new Thread(new Runnable() {

			@Override
			public void run() {
				Random g = new Random();
				String serverip = "localhost";
				BankClient client = new BankClient();
				client.init(serverip);
				for (int i=0;i<10;i++) {
					client.sendCommand( "BALANCE\n" );
					System.out.println( client.getResponse() );

					client.sendCommand( "DEPOSIT 100\n" );
					System.out.println( client.getResponse() );

					client.sendCommand( "WITHDRAW 90\n" );
					System.out.println( client.getResponse() );
					try {
						Thread.sleep(g.nextInt(100));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				client.sendCommand( "QUIT\n" );
			}
			
		}).start();
	}
}
