package hw5;

public class Guest implements Runnable {
	private SecurityGate gate;
	
	public Guest(){
		gate = SecurityGate.getInstance();
	}
	public void run(){
		gate.enter();
		gate.exit();
		System.out.println(
				"Thread "+Thread.currentThread().getId()
				+" read "+gate.getCount());
	}

}
