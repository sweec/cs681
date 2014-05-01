package hw11;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ThreadSafeBankAccountSolution4
{
	private double balance = 0;
	private ReentrantLock lock = new ReentrantLock();;
	private Condition sufficientFundsCondition, belowUpperLimitFundsCondition;
	private ThreadSafeBankAccountSolution4 account;
	
	public ThreadSafeBankAccountSolution4()
	{
		sufficientFundsCondition = lock.newCondition();
		belowUpperLimitFundsCondition = lock.newCondition();
		account =  this;
	}
	
	public void deposit(double amount) throws InterruptedException
	{
		try {
			lock.lock();
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
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
			throw e;
		} finally {
			lock.unlock();
			System.out.println("Lock released");
		}
	}
	
	public void withdraw(double amount) throws InterruptedException
	{
		try {
			lock.lock();
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
		catch (InterruptedException exception) {
			System.out.println("Interrupted");
			throw exception;
		} finally {
			lock.unlock();
			System.out.println("Lock released");
		}
	}
	
	public void transfer(ThreadSafeBankAccountSolution4 destination, double amount) throws InterruptedException{
		Random random = new Random();
		while(true) {
			if (lock.tryLock()) {
				try {
					if (destination.getLock().tryLock()) {
						try {
							if( this.balance < amount )
								System.out.println("Source account balance is not enough for withdraw amount");
							else{
								this.withdraw(amount); // Nested locking
								destination.deposit(amount); // Nested locking
							}
							return;
						} finally {
							destination.getLock().unlock();
						}
					}
				} finally {
					lock.unlock();
				}
			}
			try {
				Thread.sleep(random.nextInt(1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private ReentrantLock getLock() {
		return lock;
	}

	public static void main(String[] args)
	{
		ThreadSafeBankAccountSolution4 bankAccount = new ThreadSafeBankAccountSolution4();
		ThreadSafeBankAccountSolution4 dest = new ThreadSafeBankAccountSolution4();
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
			}catch(InterruptedException exception){
			}
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
			}catch(InterruptedException exception){
			}
		}
	}
	
	private class TransferRunnable implements Runnable {
		ThreadSafeBankAccountSolution4 destination;
		public TransferRunnable(ThreadSafeBankAccountSolution4 dest) {
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
