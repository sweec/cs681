package project;

import java.util.Vector;

public final class StaticThreadPool
{
	private boolean debug = false;
	private Queue queue = null;
	private Vector<ThreadPoolThread> availableThreads = null;

	public StaticThreadPool(int maxThreadNum, Queue queue, boolean debug)
	{
		this.debug = debug;
		if (queue == null)
			this.queue = new WaitingRunnableQueue(debug);
		else
			this.queue = queue;
		availableThreads = new Vector<ThreadPoolThread>();
		for(int i = 0; i < maxThreadNum; i++)
		{
			ThreadPoolThread th = new ThreadPoolThread(this, queue, i);
			availableThreads.add(th);
			th.start();
		}
	}

	public void shutdown() {
		for(ThreadPoolThread th:availableThreads) {
			th.setStopped(true);
			th.interrupt();
		}
	}
	
	public void execute(Runnable runnable)
	{
		queue.put(runnable);
	}

	public int getWaitingRunnableQueueSize()
	{
		return queue.size();
	}

	public int getThreadPoolSize()
	{
		return availableThreads.size();
	}

	private class ThreadPoolThread extends Thread
	{
		private StaticThreadPool pool;
		private Queue queue;
		private int id;
		private volatile boolean stopped = false;

		public ThreadPoolThread(StaticThreadPool pool, Queue queue, int id)
		{
			this.pool = pool;
			this.queue = queue;
			this.id = id;
		}

		public void setStopped(boolean stopped) {
			this.stopped = stopped;
		}
		
		public void run()
		{
			if(pool.debug==true) System.out.println("Thread " + id + " starts.");
			while(!stopped)
			{
				Runnable runnable = queue.get();
				if(runnable==null)
				{
					if(pool.debug==true)
						System.out.println("Thread " + this.id + " is being stopped due to an InterruptedException.");
					continue;
				}
				else
				{
					if(pool.debug==true) System.out.println("Thread " + id + " executes a runnable.");
					runnable.run();
					if(pool.debug == true)
						System.out.println("ThreadPoolThread " + id + " finishes executing a runnable.");
				}
			}
			if(pool.debug==true) System.out.println("Thread " + id + " stops.");
		}
	}
	
}
