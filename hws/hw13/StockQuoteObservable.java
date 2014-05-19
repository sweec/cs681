package hw13;

import java.util.HashMap;
import java.util.Random;

public class StockQuoteObservable extends Observable {
	private HashMap<String, Double> stocks = new HashMap<String, Double>();
	
	public void changeQuote(String t, double q) {
		lock.lock();
		stocks.put(t,  q);
		setChanged();
		notifyObservers(new StockEvent(t, q));
		lock.unlock();
	}
	
	public static void main(String[] args) {
		StockQuoteObservable stock = new StockQuoteObservable();
		stock.addObserver(new PieChartObserver());
		stock.addObserver(new BarChartObserver());
		Random g = new Random();
		for (int i=0;i<5;i++) {
			stock.changeQuote("stock "+g.nextInt(5), g.nextDouble()*3);
		}
	}
}
