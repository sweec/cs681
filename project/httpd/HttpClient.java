package httpd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpClient {
	public static void head(String path) {
		HttpURLConnection conn = null;
		BufferedReader in = null;

		try {
			URL url = new URL(path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.connect();

			System.out.println( conn.getResponseCode() + conn.getResponseMessage() );
			System.out.println( conn.getHeaderFields() );

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );  
				System.out.println( "I/O setup done." );

				String line = in.readLine();
				while ( in.ready() && line != null ) {                  
					System.out.println(line);
					line = in.readLine();
				}
				if (line != null)
					System.out.println(line);
			}
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
			head(path);
	}
}
