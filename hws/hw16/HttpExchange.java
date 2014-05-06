package hw16;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class HttpExchange {
	private ServerSocket server;
	private Socket client;
	private BufferedReader in;
	private PrintStream out;
	private String[] requestLine;
	private HashMap<String, String> requestHead = new HashMap<String, String>();
	private String requestBody = null;
	private String responseLine = null;
	private HashMap<String, String> responseHead = new HashMap<String, String>();
	private String responseBody = null;
	
	static Pattern getP = Pattern.compile("^GET.*");
	static Pattern headP = Pattern.compile("^HEAD.*");
	static Pattern postP = Pattern.compile("^POST.*");
	static Pattern htmlP = Pattern.compile(".*html$");
	static Pattern jpgP = Pattern.compile(".*jpg$");
	static Pattern pngP = Pattern.compile(".*png$");
	
	public HttpExchange(Socket client, ServerSocket server) {
		this.server = server;
		this.client = client;
		try {
			in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
			out = new PrintStream( client.getOutputStream() );  
			client.setSoTimeout(30000);
			System.out.println( "I/O setup done" );

			String line = in.readLine();
			requestLine = line.split("\\s+");
			System.out.println(line);
			line = in.readLine();
			while( line != null ) {
				System.out.println(line);
				if(line.equals("")) break;
				String[] kv = line.split(":[ ]*", 2);
				if (kv.length > 1)
					requestHead.put(kv[0], kv[1]);
				line = in.readLine();
			}
			if (getRequestCommand().equals("POST")) {
				requestBody = in.readLine();
				System.out.println(requestBody);
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}

	}
	
	public SocketAddress getLocalAddress() {
		return server.getLocalSocketAddress();
	}
	public InetAddress getRemoteAddress() {
		return client.getInetAddress();
	}
	public String getRequestCommand() {
		if (requestLine == null || requestLine.length < 1)
			return null;
		return requestLine[0];
	}
	public String getRrequestURI() {
		if (requestLine == null || requestLine.length < 2)
			return null;
		return requestLine[1];
	}
	public String getProtocolName() {
		if (requestLine == null || requestLine.length < 3)
			return null;
		String[] protocols = requestLine[2].split("/");
		return protocols[0];
	}
	public String getProtocolVersion() {
		if (requestLine == null || requestLine.length < 3)
			return null;
		String[] protocols = requestLine[2].split("/");
		if (protocols.length < 2)
			return null;
		return protocols[1];
	}
	public HashMap<String, String> getRequestHeaders() {
		return requestHead;
	}
	public String getRequestHeader(String key) {
		return requestHead.get(key);
	}
	public String getRequestBody() {
		return requestBody;
	}
	public void makeSuccessfulResponse() {
		responseLine = "HTTP/1.1 200 OK";
	}
	public void makeErrorResponse(int code) {
		responseLine = "HTTP/1.1 "+code+" "+getHttpStatus(code);
	}
	private static final HashMap<Integer, String> httpStatus = new HashMap<Integer, String>();
	static {
		httpStatus.put(HttpURLConnection.HTTP_OK, "OK");
		httpStatus.put(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request");
		httpStatus.put(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized (authorization required)");
		httpStatus.put(HttpURLConnection.HTTP_NOT_FOUND, "Not Found");
		httpStatus.put(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error");
		httpStatus.put(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "Not Implemented");
		httpStatus.put(HttpURLConnection.HTTP_UNAVAILABLE, "Service Unavailable");
	}
	private static String getHttpStatus(int code) {
		return httpStatus.get(code);
	}
	public void setResponseHeader(String key, String value) {
		responseHead.put(key, value);
	}
	public String getResponseHeader(String key) {
		return responseHead.get(key);
	}
	public HashMap<String, String> getResponseHeaders() {
		return responseHead;
	}
	public void setResponseBody(String content) {
		responseBody = content;
	}
	public void sendResponse() {
		out.println(responseLine);
		for (Entry<String, String> e:responseHead.entrySet())
			out.println(e.getKey()+": "+e.getValue());
		out.println("");
		if (responseBody != null)
			out.println(responseBody);
	}

}