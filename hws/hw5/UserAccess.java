package hw5;

public class UserAccess implements Runnable {
	CourseRegistration reg;
	private String courseNum;
	private String name;
	private int id;
	
	public UserAccess(String courseNum, String name, int id){
		reg = CourseRegistration.getInstance();
		this.courseNum = courseNum;
		this.name = name;
		this.id = id;
	}

	@Override
	public void run() {
		if (reg.register(courseNum, name, id))
			System.out.println(courseNum+" "+name+" "+id+" registered.");
		else
			System.out.println(courseNum+" "+name+" "+id+" register failed.");
	}

}
