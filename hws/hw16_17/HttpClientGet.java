package hw16_17;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

public class HttpClientGet {
	public static void get(String path) {
		HttpURLConnection conn = null;
		BufferedReader in = null;

		try {
			URL url = new URL(path);
			conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			System.out.println( "Connection established." );

			System.out.println( conn.getResponseCode() + conn.getResponseMessage() );
			System.out.println( conn.getHeaderFields() );

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );  
				System.out.println( "I/O setup done." );

				String line = in.readLine();
				int len = 0;
				while ( in.ready() && line != null ) {                  
					System.out.println(line);
					if (line.startsWith("Content-Length: "))
						len = Integer.parseInt(line.substring(line.indexOf(" ")+1));
					if (line.equals(""))
						break;
					line = in.readLine();
				}
				if (len>0) {
					char[] buf = new char[len];
					in.read(buf);
					System.out.println(String.valueOf(buf));
				}
			}
		} catch (SocketException e) {
			System.out.println("Interrupted, stop");
			return;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if (in != null)
					in.close();
				System.out.println( "I/O closed." );
			} catch (IOException e) {
				e.printStackTrace();
			}
			conn.disconnect();
			System.out.println( "Connection closed." );
		}	
	}

	public static void main(String[] args) {
		String[] paths = {
				"http://localhost:8888/",
				"http://localhost:8888/a.jpg",
				"http://localhost:8888/b.png",
				"http://localhost:8888/fakefile",
				"http://localhost:8888/fakeDir/a.jpg"
		};
		for (String path:paths)
			get(path);
	}
}
