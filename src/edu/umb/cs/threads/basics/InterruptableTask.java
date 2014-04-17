package edu.umb.cs.threads.basics;

public class InterruptableTask implements Runnable
{
	public void run()
	{
		try
		{
			System.out.println(1);
			Thread.sleep(1000);
			System.out.println(2);
		} catch (InterruptedException e)
		{
			System.out.println(3);
		}
		System.out.println(4);
	}
	
	public static void main(String[] args)
	{
		Thread t = new Thread(new InterruptableTask());
		t.start();
		t.interrupt();
	}

}
