package hw11;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ThreadSafeBankAccountSolution3a
{
	private double balance = 0;
	private ReentrantLock lock = new ReentrantLock();;
	private Condition sufficientFundsCondition, belowUpperLimitFundsCondition;
	private ThreadSafeBankAccountSolution3a account;
	private int acctNum = AccountNumber.generate();
	
	public ThreadSafeBankAccountSolution3a()
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
	
	public void transfer(ThreadSafeBankAccountSolution3a destination, double amount){
		int sourceID = System.identityHashCode(account);
		int destID = System.identityHashCode(destination);
		if (sourceID < destID) {
			lock.lock();
			destination.getLock().lock();
			if( this.balance < amount )
				System.out.println("Source account balance is not enough for withdraw amount");
			else{
				this.withdraw(amount); // Nested locking
				destination.deposit(amount); // Nested locking
			}
			destination.getLock().unlock();
			lock.unlock();
		} else if (sourceID > destID) {
			destination.getLock().lock();
			lock.lock();
			if( this.balance < amount )
				System.out.println("Source account balance is not enough for withdraw amount");
			else{
				this.withdraw(amount); // Nested locking
				destination.deposit(amount); // Nested locking
			}
			lock.unlock();
			destination.getLock().unlock();
		}
	}

	private ReentrantLock getLock() {
		return lock;
	}

	public static void main(String[] args)
	{
		ThreadSafeBankAccountSolution3a bankAccount = new ThreadSafeBankAccountSolution3a();
		ThreadSafeBankAccountSolution3a dest = new ThreadSafeBankAccountSolution3a();
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
		ThreadSafeBankAccountSolution3a destination;
		public TransferRunnable(ThreadSafeBankAccountSolution3a dest) {
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
