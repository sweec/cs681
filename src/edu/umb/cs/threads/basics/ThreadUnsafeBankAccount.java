package edu.umb.cs.threads.basics;

public class ThreadUnsafeBankAccount
{
	private ThreadUnsafeBankAccount account;
	private double balance = 0;
	
	public ThreadUnsafeBankAccount()
	{
		account = this;
	}
	
	public void deposit(double amount)
	{
		System.out.print("Current balance (d): " + balance);
		double newBalance = balance + amount;
		System.out.println(", New balance (d): " + newBalance);
		balance = newBalance;
	}
	
	public void withdraw(double amount)
	{
		System.out.print("Current balance (w): " + balance);
		double newBalance = balance - amount;
		System.out.println(", New balance (w): " + newBalance);
		balance = newBalance;
	}
	
	public static void main(String[] args)
	{
		ThreadUnsafeBankAccount bankAccount = new ThreadUnsafeBankAccount();
		new Thread( bankAccount.new DepositRunnable() ).start();
		new Thread( bankAccount.new  WithdrawRunnable() ).start();
	}
	
	public class DepositRunnable implements Runnable
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
	
	public class WithdrawRunnable implements Runnable
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
