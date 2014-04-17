package edu.umb.cs.threads.basics;

public class ThreadUnsafeFlag
{
	private boolean done = false;
	
	public void setDone()
	{
		done = true;
	}
	
	public void work()
	{
		while( !done )
		{
			System.out.println("#");
		}
		System.out.println("Flag state changed.");
	}
	
	public static void main(String[] args)
	{
		ThreadUnsafeFlag flagObject = new ThreadUnsafeFlag();
		new Thread(flagObject.new Interrupter(flagObject)).start();
		flagObject.work();
	}
	
	public class Interrupter implements Runnable
	{
		private ThreadUnsafeFlag target;
		
		Interrupter(ThreadUnsafeFlag target)
		{
			this.target = target;
		}
		
		public void run()
		{
			try
			{
				Thread.sleep(100);
				target.setDone();
			} catch (InterruptedException e){}
		}
	}
}
