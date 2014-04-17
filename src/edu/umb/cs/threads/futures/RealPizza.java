package edu.umb.cs.threads.futures;
 
public class RealPizza implements Pizza
{
	private String realPizza;
	
	public RealPizza()
	{
		try
		{
			Thread.sleep(10);
		}
		catch(InterruptedException e){}
		System.out.println("A real pizza is made!");
		realPizza = "REAL PIZZA!";
	}

	public String getPizza()
	{
		return realPizza;
	}
}
