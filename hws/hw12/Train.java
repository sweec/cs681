package hw12;

public class Train {
	int id = TrainID.generate();
	
	public void stop() {
		System.out.println("Thread "+Thread.currentThread().getId()+": Train "+id+" stopped");
	}
	
	public void run() {
		System.out.println("Thread "+Thread.currentThread().getId()+": Train "+id+" runs");
	}
}
