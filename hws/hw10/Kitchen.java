package hw10;

public class Kitchen implements Runnable {
	private Future future;
	
	public Kitchen(Future future) {
		this.future = future;
	}
	
	@Override
	public void run() {
		RealPizza realPizza = new RealPizza();
		future.setRealPizza( realPizza );
	}

}
