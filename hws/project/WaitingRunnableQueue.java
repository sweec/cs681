package project;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WaitingRunnableQueue implements Queue {
	private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
	private ReentrantLock queueLock = new ReentrantLock();
	private Condition runnablesAvailable = queueLock.newCondition();
	private boolean debug;
	private volatile boolean done = false;

	public WaitingRunnableQueue(boolean debug) {
		this.debug = debug;
	}
	
	@Override
	public void clear() {
		done = true;
		queueLock.lock();
		runnables.clear();
		runnablesAvailable.signalAll();
		queueLock.unlock();
	}
	
	@Override
	public int size()
	{
		return runnables.size();
	}

	@Override
	public void put(Runnable obj)
	{
		if (done) return;
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
			while(!done && runnables.isEmpty())
			{
				if (debug) System.out.println("Waiting for a runnable...");
				runnablesAvailable.await();
			}
			if (done) {
				runnablesAvailable.signalAll();
				return null;
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
