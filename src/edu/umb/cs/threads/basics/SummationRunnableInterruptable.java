package edu.umb.cs.threads.basics;

public class SummationRunnableInterruptable implements Runnable
{
	private int upperBound;
	
	public SummationRunnableInterruptable(int n)
	{
		upperBound = n;
	}
	
	public void run()
	{
		try
		{
			while( !Thread.interrupted() )
			{
				System.out.println(upperBound);
				upperBound--;
				Thread.sleep(1000);
				if(upperBound<0)
				{
					System.out.println("print done");
					return;
				}
			}
		} catch (InterruptedException e)
		{
			System.out.println("interrupted by main()!");
		}
	}
	
	public static void main(String[] args)
	{
		SummationRunnableInterruptable sRunnable = new SummationRunnableInterruptable(10);
		Thread thread = new Thread(sRunnable);
		thread.start();
		
		for(int i=0; i<5; i++)
		{
			System.out.println("#");
		}
		thread.interrupt();
		try
		{
			thread.join();
			new Thread(sRunnable).start();
		} catch (InterruptedException e)
		{
		}
	}
}