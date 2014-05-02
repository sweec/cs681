package hw12;

import java.util.HashMap;

public class StockQuoteObservable extends Observable {
	private HashMap<String, Double> stocks = new HashMap<String, Double>();
	
	public void changeQuote(String t, double q) {
		lock.lock();
		stocks.put(t,  q);
		setChanged();
		notifyObservers(new StockEvent(t, q));
		lock.unlock();
	}
}
