package project;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class HttpUtility {
	
	static Pattern getP = Pattern.compile("^GET.*");
	static Pattern headP = Pattern.compile("^HEAD.*");
	static Pattern postP = Pattern.compile("^POST.*");
	static Pattern htmlP = Pattern.compile(".*html$");
	static Pattern jpgP = Pattern.compile(".*jpg$");
	static Pattern pngP = Pattern.compile(".*png$");
	
	public static boolean isGetCommand(String command) {
		if (command != null && getP.matcher(command).matches())
			return true;
		return false;
	}
	
	public static boolean isHeadCommand(String command) {
		if (command != null && headP.matcher(command).matches())
			return true;
		return false;
	}
	
	public static boolean isPostCommand(String command) {
		if (command != null && postP.matcher(command).matches())
			return true;
		return false;
	}
	
	public static String getFileType(String name) {
		if (htmlP.matcher(name).matches())
			return "text/html";
		else if (jpgP.matcher(name).matches())
			return "image/jpg";
		else if (pngP.matcher(name).matches())
			return "image/png";
		else
			return null;
	}

	private static SimpleDateFormat GMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	static {
		GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	public static String getGMT(long time) {
		return GMT.format(time);
	}

	public static String toMD5(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
		    byte byteData[] = md.digest(message.getBytes());
		    StringBuffer sb = new StringBuffer();
		    for (int i = 0; i < byteData.length; i++)
		        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("failed to get MessageDigest instance");
			e.printStackTrace();
		}
		return null;
	}
}
