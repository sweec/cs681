package hw12;

import java.util.Random;

public class Sensor implements Runnable {
	private int id;
	private PointController pc;
	private boolean inbound;
	
	public Sensor(PointController pc, int id, boolean value) {
		this.id = id;
		this.pc = pc;
		this.inbound = value;
	}

	public int getID() {
		return id;
	}

	public boolean isInbound() {
		return inbound;
	}
	
	@Override
	public void run() {
		Random g = new Random();
		if (inbound)
			for (int i=0;i<10;i++) {
				pc.inboundSensorSignal(new Train(), this);
				try {
					Thread.sleep(g.nextInt(200));
				} catch (InterruptedException e) {
				}
			}
		else
			for (int i=0;i<30;i++) {
				try {
					Thread.sleep(g.nextInt(200));
				} catch (InterruptedException e) {
				}
				pc.outboundSensorSignal(null, this);
			}
	}

}
