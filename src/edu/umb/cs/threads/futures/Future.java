package edu.umb.cs.threads.futures;
 
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Future implements Pizza
{
	private RealPizza realPizza = null;
	private String pizzaData;
	private ReentrantLock lock;
	private Condition ready;
	
	public Future()
	{
		lock = new ReentrantLock();
		ready = lock.newCondition();
	}
	
	public void setRealPizza( RealPizza real )
	{
		lock.lock();
		try
		{
			if( realPizza != null ){ return; }
			realPizza = real;
			ready.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	public String getPizza()
	{
		lock.lock();
		try
		{
			if( realPizza == null )
			{
				ready.await();
			}
			pizzaData = realPizza.getPizza();
		}
		catch(InterruptedException e){}
		finally
		{
			lock.unlock();
		}
		return pizzaData;
	}
}
