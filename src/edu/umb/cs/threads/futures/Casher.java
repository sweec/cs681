package edu.umb.cs.threads.futures;

 
public class Casher
{
	public Pizza order()
	{
		System.out.println("An order is made.");
		final Future future = new Future();
		new Thread()
		{
			public void run()
			{
				RealPizza realPizza = new RealPizza();
				future.setRealPizza( realPizza );
			}
		}.start();
		return future;
	}

	public static void main(String[] args)
	{
		Casher casher = new Casher();
		
		System.out.println("Ordering pizzas at a casher counter.");
		Pizza p1 = casher.order();
		Pizza p2 = casher.order();
		Pizza p3 = casher.order();
		
		System.out.println("Doing something, reading newspapers, magazines, etc., " +
				"until pizzas are ready to pick up...");
		try
		{
			Thread.sleep(2000);
		}
		catch(InterruptedException e){}
		
		System.out.println("Let's see if pizzas are ready to pick up...");
		System.out.println( p1.getPizza() );
		System.out.println( p2.getPizza() );
		System.out.println( p3.getPizza() );
	}
}
