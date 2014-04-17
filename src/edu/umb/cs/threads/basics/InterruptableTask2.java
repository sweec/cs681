package edu.umb.cs.threads.basics;

public class InterruptableTask2 implements Runnable
{
	public void run()
	{
		while( !Thread.interrupted() )
		{
			System.out.println(1);
		}
		System.out.println(4);
	}
	
	public static void main(String[] args)
	{
		Thread t = new Thread(new InterruptableTask2());
		t.start();
		try
		{
			Thread.sleep(2000);
			t.interrupt();
		} catch (InterruptedException e)
		{
		}
	}

}
