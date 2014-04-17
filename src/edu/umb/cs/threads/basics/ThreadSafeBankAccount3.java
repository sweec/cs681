package edu.umb.cs.threads.basics; 

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.ArrayList;

public class ThreadSafeBankAccount3
{
	private double balance = 0;
	private ReentrantLock lock;
	private Condition sufficientFundsCondition;
	private Condition belowUpperLimitFundsCondition;
	private ThreadSafeBankAccount3 account; 
	
	public ThreadSafeBankAccount3()
	{
		lock = new ReentrantLock();
		sufficientFundsCondition = lock.newCondition();
		belowUpperLimitFundsCondition = lock.newCondition();
		account = this;
	}
	
	public void deposit(double amount)
	{
		lock.lock();
		System.out.println("Lock obtained");
		try
		{
			System.out.println(Thread.currentThread().getId() + " (d): current balance: " + balance);
			while(balance >= 300)
			{
				System.out.println(Thread.currentThread().getId() + " (d): await(): Balance exceeds the upper limit.");
				belowUpperLimitFundsCondition.await();
			}
			balance += amount;
			System.out.println(Thread.currentThread().getId() + " (d): new balance: " + balance);
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
		System.out.println("Lock obtained");
		try
		{
			System.out.println(Thread.currentThread().getId() + " (w): current balance: " + balance);
			while(balance < amount)
			{
				System.out.println(Thread.currentThread().getId() + " (w): await(): Insufficient funds");
				sufficientFundsCondition.await();
			}
			balance -= amount;
			System.out.println(Thread.currentThread().getId() + " (w): new balance: " + balance);
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
	
	public double getBalance()
	{
		lock.lock();
		System.out.println("Lock obtained");
		try{
			System.out.println(Thread.currentThread().getId() + " (b): current balance: " + balance);
			return balance;
		}
		finally
		{
			lock.unlock();
			System.out.println("Lock released");
		}
	}

	public static void main(String[] args)
	{
		ThreadSafeBankAccount3 bankAccount = new ThreadSafeBankAccount3();
		ArrayList<Thread> list = new ArrayList<Thread>();
		
		for(int i = 0; i < 2; i++)
		{
			list.add( new Thread( bankAccount.new DepositRunnable() ) );
			list.add( new Thread( bankAccount.new WithdrawRunnable() ) );
		}
		for(int i = 0; i < 6; i++)
		{
			list.add( new Thread( bankAccount.new ReadRunnable() ) );
		}
		long startTime = System.currentTimeMillis();
		for(int i = 0; i < 10; i++)
		{
			list.get(i).start();
		}
		try {
			for(int i = 4; i < 10; i++)
			{
				list.get(i).join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println( endTime - startTime );
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

	private class ReadRunnable implements Runnable
	{
		public void run()
		{
			try
			{
				for(int i = 0; i < 10; i++)
				{
					account.getBalance();
					Thread.sleep(2);
				}
			}catch(InterruptedException exception){}
		}
	}
}
