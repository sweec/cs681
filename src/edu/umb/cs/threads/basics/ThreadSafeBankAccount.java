package edu.umb.cs.threads.basics;

import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeBankAccount
{
	private double balance = 0;
	private ReentrantLock lock;
	private ThreadSafeBankAccount account; 
	
	public ThreadSafeBankAccount()
	{
		lock = new ReentrantLock();
		account = this;
	}
	
	public void deposit(double amount)
	{
		lock.lock();
		System.out.println("Lock obtained");
		try
		{
			System.out.print("Current balance (d): " + balance);
			double newBalance = balance + amount;
			System.out.println(", New balance (d): " + newBalance);
			balance = newBalance;
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
			System.out.print("Current balance (w): " + balance);
			double newBalance = balance - amount;
			System.out.println(", New balance (w): " + newBalance);
			balance = newBalance;
		}
		finally
		{
			lock.unlock();
			System.out.println("Lock released");
		}
	}
	
	public static void main(String[] args)
	{
		ThreadSafeBankAccount bankAccount = new ThreadSafeBankAccount();
		
		for(int i = 0; i < 1; i++)
		{
			new Thread( bankAccount.new DepositRunnable() ).start();
			new Thread( bankAccount.new WithdrawRunnable()).start();
		}
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
}
