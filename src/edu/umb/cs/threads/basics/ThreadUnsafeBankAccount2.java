package edu.umb.cs.threads.basics;

public class ThreadUnsafeBankAccount2
{
	private ThreadUnsafeBankAccount2 account;
	private double balance = 0;

	public ThreadUnsafeBankAccount2()
	{
		account = this;
	}

	public void deposit(double amount)
	{
		System.out.print("Current balance (d): " + balance);
		balance = balance + amount;
		System.out.println(", New balance (d): " + balance);
	}
	
	public void withdraw(double amount)
	{
		System.out.print("Current balance (w): " + balance);
		balance = balance - amount;
		System.out.println(", New balance (w): " + balance);
	}

	
	public static void main(String[] args)
	{
		ThreadUnsafeBankAccount2 bankAccount = new ThreadUnsafeBankAccount2();
		new Thread( bankAccount.new DepositRunnable() ).start();
		new Thread( bankAccount.new WithdrawRunnable() ).start();
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
