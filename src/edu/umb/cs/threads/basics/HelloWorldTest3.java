package edu.umb.cs.threads.basics;

public class HelloWorldTest3
{
	public static void main(String[] args)
	{
		GreetingRunnable runnable1 = new GreetingRunnable("Hello World");
		GreetingRunnable runnable2 = new GreetingRunnable("Goodbye World");
		Thread thread1 = new Thread(runnable1);
		Thread thread2 = new Thread(runnable2);
		
		System.out.println(thread1.getState());
		System.out.println(thread2.getState());
		
		System.out.println(thread1.isAlive());
		System.out.println(thread1.isAlive());
		
		thread1.start();
		thread2.start();

		System.out.println(thread1.getState());
		System.out.println(thread2.getState());
		System.out.println(thread1.isAlive());
		System.out.println(thread1.isAlive());
		
		try
		{
			thread1.join();
			thread2.join();
		} catch (InterruptedException e)
		{
		}
		System.out.println(thread1.getState());
		System.out.println(thread2.getState());
		System.out.println(thread1.isAlive());
		System.out.println(thread2.isAlive());
	}

}
