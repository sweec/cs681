package hw10;
 
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
	
	public boolean isReady() {
		lock.lock();
		try {
			return (realPizza != null);
		} finally {
			lock.unlock();
		}
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

	public String getPizza() throws TimeoutException
	{
		lock.lock();
		try
		{
			if( realPizza == null )
			{
				if (!ready.await(500, TimeUnit.MILLISECONDS))
					throw new TimeoutException("Pizza is not ready after 500 ms");
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
