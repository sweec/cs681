package edu.umb.cs.threads.basics;
 
public class HelloWorldTest
{
	public static void main(String[] args)
	{
		GreetingRunnable runnable1 = new GreetingRunnable("Hello World");
		Thread thread1 = new Thread(runnable1);
		thread1.start();
	}

}
