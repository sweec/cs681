package hw12;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PointController {
	private Point p;
	private ReentrantLock lock = new ReentrantLock();
	private Condition pointEmpty = lock.newCondition();
	
	public PointController(Point p) {
		this.p = p;
	}
	
	public void inboundSensorSignal(Train t, Sensor s) {
		lock.lock();
		if (p.isOccupied())
			t.stop();
		do {
			try {
				pointEmpty.await();
			} catch (InterruptedException e) {
				System.out.println("Interrupted.");
				break;
			}
		} while (p.isOccupied());
		p.changeState(s.getID());
		t.run();
		lock.unlock();
	}

	public void outboundSensorSignal(Train t, Sensor s) {
		lock.lock();
		//System.out.println("Train pass");
		p.clear();
		pointEmpty.signalAll();
		lock.unlock();
	}
	
	public static void main(String[] args) {
		PointController pc = new PointController(new Point());
		new Thread(new Sensor(pc, 1, true)).start();
		new Thread(new Sensor(pc, 2, true)).start();
		new Thread(new Sensor(pc, 3, false)).start();
	}
}
