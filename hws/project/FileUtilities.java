package project;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileUtilities {

	public static String readTextFile( String file ) {
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");
	    BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader (file));
		    while( ( line = reader.readLine() ) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	    return stringBuilder.toString();
	}
	
	public static byte[] readBinaryFile(String path) {
		DataInputStream fin = null;
		try{
			File file = new File(path);
			int len = (int) file.length();
			fin = new DataInputStream(new FileInputStream(file));
			byte[] buf = new byte[len];
			fin.readFully(buf);
			return buf;
		} catch (FileNotFoundException e) {
			System.out.println(path+": file not exist");
			return null;
		} catch (IOException e) {
			System.out.println(path+" read error");
			return null;
		} finally {
			try {
				if (fin != null)
					fin.close();
			} catch (IOException e) {
				System.out.println(path+" close error");
			}
		}
	}
}
