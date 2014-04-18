package hw5;

import java.util.concurrent.locks.ReentrantLock;

public class CourseRegistration {
	private String courseNum;
	private String name;
	private int id;
	private static CourseRegistration instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	public static CourseRegistration getInstance() {
		if (instance == null) {
			lock.lock();
			if (instance == null) {
				CourseRegistration inst = new CourseRegistration();
				instance = inst;
			}
			lock.unlock();
		}
		return instance;
	}
	
	public boolean register(String courseNum, String name, int id){
		lock.lock();
		this.courseNum = courseNum;
		this.name = name;
		this.id = id;
		try{
			checkNameID();
			checkPrerequisites();
			checkAccountingInfo();
			checkHolds();
		} catch (RegisterException e){
			System.out.println("Error: "+e.getMessage());
			lock.unlock();
			return false;
		}
		doRegister();
		lock.unlock();
		return true;
	}
	private void doRegister() {
		// TODO Auto-generated method stub
		
	}
	private void checkHolds() throws RegisterException {
		// TODO Auto-generated method stub
		
	}
	private void checkAccountingInfo() throws RegisterException {
		// TODO Auto-generated method stub
		
	}
	private void checkPrerequisites() throws RegisterException {
		// TODO Auto-generated method stub
		
	}
	private void checkNameID() throws RegisterException {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		for (int i=0;i<5;i++)
			new Thread(new UserAccess("CN"+i, "name"+i, i)).start();
	}
}
