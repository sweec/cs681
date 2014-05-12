package project;

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
}
