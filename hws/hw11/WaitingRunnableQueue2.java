package hw11;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WaitingRunnableQueue2 implements Queue {
	private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
	private ReentrantLock queueLock = new ReentrantLock();
	private Condition runnablesAvailable = queueLock.newCondition();
	private boolean debug;

	public WaitingRunnableQueue2(boolean debug) {
		this.debug = debug;
	}
	
	@Override
	public int size()
	{
		return runnables.size();
	}

	@Override
	public void put(Runnable obj)
	{
		queueLock.lock();
		try
		{
			runnables.add(obj);
			if (debug) System.out.println("A runnable queued.");
			runnablesAvailable.signalAll();
		}
		finally
		{
			queueLock.unlock();
		}
	}

	@Override
	public Runnable get()
	{
		queueLock.lock();
		try
		{
			while(runnables.isEmpty())
			{
				if (debug) System.out.println("Waiting for a runnable...");
				runnablesAvailable.await();
			}
			if (debug) System.out.println("A runnable dequeued.");
			return runnables.remove(0);
		}
		catch(InterruptedException ex)
		{
			return null;
		}
		finally
		{
			queueLock.unlock();
		}
	}
}
