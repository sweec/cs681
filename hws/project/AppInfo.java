package project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.DatatypeConverter;

public class AppInfo {
	private Properties applicationProps = null;
	private HashMap<String, String> basicUsers = new HashMap<String, String>();
	private HashMap<String, String> digestUsers = new HashMap<String, String>();
	private AppInfo(){}
	private ReentrantReadWriteLock basiclock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock digestlock = new ReentrantReadWriteLock();
	private static AppInfo instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	public static AppInfo getInstance() {
		if (instance == null) {
			lock.lock();
			if (instance == null) {
				AppInfo inst = new AppInfo();
				instance = inst;
			}
			lock.unlock();
		}
		return instance;
	}
	
	public void load() {
		// create and load default properties
		Properties defaultProps = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream("defaultProperties");
			defaultProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("No default setting");
		} catch (IOException e) {
			System.out.println("No default setting");
		}

		// create application properties with default
		applicationProps = new Properties(defaultProps);

		// now load properties 
		// from last invocation
		try {
			in = new FileInputStream("appProperties");
			applicationProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("No application setting");
		} catch (IOException e) {
			System.out.println("No application setting");
		}
		
		loadUsers(basicUsers, "basic");
		loadUsers(digestUsers, "digest");
	}
	
	private void loadUsers(HashMap<String, String> map, String type) {
		String users = applicationProps.getProperty(type);
		if (users != null) {
			for (String userinfo:users.split(";")) {
				int loc = userinfo.indexOf(":");
				if (loc >= 0)
					map.put(userinfo.substring(0, loc), userinfo.substring(loc+1));
			}
		}
	}
	
	public void save() {
		saveUsers(basicUsers, "basic");
		saveUsers(digestUsers, "digest");
		FileOutputStream out;
		try {
			out = new FileOutputStream("appProperties");
			applicationProps.store(out, "---No Comment---");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveUsers(HashMap<String, String> map, String type) {
		StringBuilder users = new StringBuilder();
		for (Entry<String, String> e:map.entrySet())
			users.append(e.getKey()+":"+e.getValue()+";");
		int len = users.length();
		if (len > 0)
			users.setLength(len-1);
		applicationProps.setProperty(type, users.toString());
	}
	
	public void addToRealm(String username, String realm) {
		if (!hasRealmUser(realm, username)) {
			String users = applicationProps.getProperty(realm);
			if (users == null)
				applicationProps.setProperty(realm, username);
			else
				applicationProps.setProperty(realm, users+","+username);
		}
	}
	
	public boolean hasRealmUser(String realm, String username) {
		String users = applicationProps.getProperty(realm);
		if (users == null)
			return false;
		for (String user:users.split(","))
			if (user.equals(username))
				return true;
		return false;
	}
	
	public String getProperty(String key) {
		return applicationProps.getProperty(key);
	}
	
	public void setProperty(String key, String value) {
		applicationProps.setProperty(key, value);
	}
	
	public void addBasicUser(String username, String password) {
		basiclock.writeLock().lock();
		String info = username+":"+password;
		basicUsers.put(DatatypeConverter.printBase64Binary(info.getBytes()), username);
		basiclock.writeLock().unlock();
	}
	
	public String getBasicUser(String info) {
		basiclock.readLock().lock();
		try {
			return basicUsers.get(info);
		} finally {
			basiclock.readLock().unlock();
		}
	}
	
	public void addDigestUser(String username, String password, String realm) {
		digestlock.writeLock().lock();
		String info = username+":"+realm+":"+password;
		digestUsers.put(username, HttpUtility.toMD5(info));
		digestlock.writeLock().unlock();
	}
	
	public String getDigestUser(String username) {
		digestlock.readLock().lock();
		try {
			return digestUsers.get(username);
		} finally {
			digestlock.readLock().unlock();
		}
	}
}
