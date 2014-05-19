package hw16_17;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map.Entry;

public class HttpExchange {
	private ServerSocket server;
	private Socket client;
	private PrintStream out;
	private String[] requestLine = null;
	private HashMap<String, String> requestHead = new HashMap<String, String>();
	private String requestBody = null;
	private String responseLine = null;
	private HashMap<String, String> responseHead = new HashMap<String, String>();
	private byte[] responseBody = null;
	
	public HttpExchange(Socket client, ServerSocket server, BufferedReader in, PrintStream out) throws Exception {
		this.server = server;
		this.client = client;
		this.out = out;
		client.setSoTimeout(1000);
		String line = in.readLine();
		requestLine = line.split("\\s+");
		System.out.println(line);
		Integer len = 0;
		line = in.readLine();
		while( line != null ) {
			System.out.println(line);
			if(line.isEmpty()) break;
			String[] kv = line.split(":[ ]*", 2);
			if (kv.length > 1) {
				requestHead.put(kv[0], kv[1]);
				if (kv[0].equalsIgnoreCase("Content-Length"))
					len = Integer.parseInt(kv[1]);
			}
			line = in.readLine();
		}
		if (len != null && len > 0) {
			char[] buf = new char[len];
			in.read(buf);
			requestBody = String.valueOf(buf);
			System.out.println(requestBody);
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
		responseLine = "HTTP/1.0 200 OK";
	}
	public void makeErrorResponse(int code) {
		String status = getHttpStatus(code);
		responseLine = "HTTP/1.0 "+code+" "+status;
		setResponseHeader("Server", "Java socket "+System.getProperty("os.name"));
		setResponseHeader("Connection","Keep-Alive");
		setResponseHeader("Content-Length", String.valueOf(status.length()));
		setResponseHeader("Date", HttpUtility.getGMT(System.currentTimeMillis()));
		responseBody = status.getBytes();
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
	public void setResponseBody(byte[] content) {
		responseBody = content;
	}
	public void sendResponse() {
		out.println(responseLine);
		for (Entry<String, String> e:responseHead.entrySet())
			out.println(e.getKey()+": "+e.getValue());
		out.println("");
		if (responseBody != null)
			out.write(responseBody, 0, responseBody.length);
		out.flush();
	}

	public boolean isPersistent() {
		String protocol = getProtocolName();
		if (protocol == null || !protocol.equalsIgnoreCase("HTTP"))
			return true;
		String version = getProtocolVersion();
		if (version == null || !version.equals("1.0"))
			return true;
		String conn = getRequestHeader("Connection");
		if (conn == null || !conn.equalsIgnoreCase("keep-alive"))
			return false;
		return true;
	}

}
