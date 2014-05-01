package hw11;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ThreadSafeBankAccountSolution1
{
	private double balance = 0;
	private static ReentrantLock lock = new ReentrantLock();;
	private Condition sufficientFundsCondition, belowUpperLimitFundsCondition;
	private ThreadSafeBankAccountSolution1 account;
	
	public ThreadSafeBankAccountSolution1()
	{
		sufficientFundsCondition = lock.newCondition();
		belowUpperLimitFundsCondition = lock.newCondition();
		account =  this;
	}
	
	public void deposit(double amount)
	{
		lock.lock();
		try
		{
			System.out.println("Lock obtained");
			System.out.println(Thread.currentThread().getId() + 
					" (d): current balance: " + balance);
			while(balance >= 3500)
			{
				System.out.println(Thread.currentThread().getId() + 
						" (d): await(): Balance exceeds the upper limit.");
				belowUpperLimitFundsCondition.await();
			}
			balance += amount;
			System.out.println(Thread.currentThread().getId() + 
					" (d): new balance: " + balance);
			sufficientFundsCondition.signalAll();
		}
		catch (InterruptedException exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			lock.unlock();
			System.out.println("Lock released");
		}
	}
	
	public void withdraw(double amount)
	{
		lock.lock();
		try
		{
			System.out.println("Lock obtained");
			System.out.println(Thread.currentThread().getId() + 
					" (w): current balance: " + balance);
			while(balance < 0)
			{
				System.out.println(Thread.currentThread().getId() + 
						" (w): await(): Insufficient funds");
				sufficientFundsCondition.await();
			}
			balance -= amount;
			System.out.println(Thread.currentThread().getId() + 
					" (w): new balance: " + balance);
			belowUpperLimitFundsCondition.signalAll();
		}
		catch (InterruptedException exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			lock.unlock();
			System.out.println("Lock released");
		}
	}
	
	public void transfer(ThreadSafeBankAccountSolution1 destination, double amount){
		lock.lock();
		if( this.balance < amount )
			System.out.println("Source account balance is not enough for withdraw amount");
		else{
			this.withdraw(amount); // Nested locking
			destination.deposit(amount); // Nested locking
		}
		lock.unlock();
	}

	public static void main(String[] args)
	{
		ThreadSafeBankAccountSolution1 bankAccount = new ThreadSafeBankAccountSolution1();
		ThreadSafeBankAccountSolution1 dest = new ThreadSafeBankAccountSolution1();
		Thread dts[] = new Thread[5];
		for(int i = 0; i < 5; i++)
		{
			dts[i] = new Thread( bankAccount.new DepositRunnable() );
			dts[i].start();
		}
		Thread wt = new Thread( bankAccount.new WithdrawRunnable() );
		wt.start();
		Thread tt = new Thread( bankAccount.new TransferRunnable(dest) );
		tt.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (Thread th:dts)
			th.interrupt();
		wt.interrupt();
		tt.interrupt();
	}

	private class DepositRunnable implements Runnable
	{
		public void run()
		{
			try
			{
				for(int i = 0; i < 10; i++)
				{
					account.deposit(100);
					Thread.sleep(2);
				}
			}catch(InterruptedException exception){}
		}
	}
	
	private class WithdrawRunnable implements Runnable
	{
		public void run()
		{
			try
			{
				for(int i = 0; i < 10; i++)
				{
					account.withdraw(100);
					Thread.sleep(2);
				}
			}catch(InterruptedException exception){}
		}
	}
	
	private class TransferRunnable implements Runnable {
		ThreadSafeBankAccountSolution1 destination;
		public TransferRunnable(ThreadSafeBankAccountSolution1 dest) {
			this.destination = dest;
		}
		
		public void run()
		{
			try
			{
				for(int i = 0; i < 10; i++)
				{
					account.transfer(destination, 100);
					Thread.sleep(2);
				}
			}catch(InterruptedException exception){
			}
		}
	}
}
