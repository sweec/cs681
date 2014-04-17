package cs681.net;

public class BankAccount
{
	private double balance = 0;
	
	public double getBalance()
	{
		return balance;
	}
	
	public double deposit( double amount)
	{
		balance += amount;
		return balance;
	}
	
	public double withdraw( double amount)
	{
		balance -= amount;
		return balance;
	}
	
}
