package hw15;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.io.*;

public class BankClient
{
	private static final int BANKPORT = 8888;
	private Socket socket;
	private Scanner in;
	private PrintWriter out;

	public void init(String server)
	{
		try
		{
			socket = new Socket( server, BANKPORT );
			System.out.println( "Socket created on the local port " +
								socket.getLocalPort() );
			System.out.println( "A connection established with the remote port " +
								socket.getPort() + " at " +
								socket.getInetAddress().toString() );

			in = new Scanner( socket.getInputStream() );
			out = new PrintWriter( socket.getOutputStream() );
			System.out.println( "I/O setup done." );
		}
		catch(IOException exception){}
	}
	
	public void sendCommand( String command )
	{
		System.out.print( "Sending " + command );
		out.print( command );
		out.flush();
		if( command.equals("QUIT") )
		{
			try
			{
				socket.close();
				System.out.println( "A connection closed." );
			}
			catch (IOException exception)
			{
				exception.printStackTrace();
			}
		}
	}
	
	public String getResponse()
	{
		return in.nextLine();
	}

	public static void main(String[] args)
	{
		Random g = new Random();
		String server = "localhost";
		if (args.length > 0)
			server = args[0];
		BankClient client = new BankClient();
		client.init(server);
		for (int i=0;i<20;i++) {
			client.sendCommand( "BALANCE\n" );
			System.out.println( client.getResponse() );

			client.sendCommand( "DEPOSIT 100\n" );
			System.out.println( client.getResponse() );

			client.sendCommand( "WITHDRAW 90\n" );
			System.out.println( client.getResponse() );
			try {
				Thread.sleep(g.nextInt(500));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		client.sendCommand( "QUIT\n" );
	}
}
