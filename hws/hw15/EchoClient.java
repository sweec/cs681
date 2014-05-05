package hw15;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;

public class EchoClient
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
		System.out.print( command );
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
		String server = "localhost";
		if (args.length > 0)
			server = args[0];
		EchoClient client = new EchoClient();
		client.init(server);
		
		client.sendCommand( "BALANCE\n" );
		System.out.println( client.getResponse() );
		
		client.sendCommand( "DEPOSIT 100\n" );
		System.out.println( client.getResponse() );
		
		client.sendCommand( "WITHDRAW 50\n" );
		System.out.println( client.getResponse() );
		
		client.sendCommand( "QUIT\n" );
	}
}
