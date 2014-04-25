package hw10;

import java.util.concurrent.TimeoutException;
 
public interface Pizza
{
	public abstract boolean isReady();
	public abstract String getPizza() throws TimeoutException;
}
