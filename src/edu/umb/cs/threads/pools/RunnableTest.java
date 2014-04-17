package edu.umb.cs.threads.pools;

public class RunnableTest implements Runnable {
	private String id;

	public RunnableTest(String id) {
		this.id = id;
	}
	
	@Override
	public void run() {
		System.out.println(id+" is running.");
	}
	
}