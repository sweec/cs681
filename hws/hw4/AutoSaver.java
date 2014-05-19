package hw4;

public class AutoSaver implements Runnable {
	private SimpleFile file;
	
	public AutoSaver(SimpleFile file) {
		this.file = file;
	}

	@Override
	public void run() {
		System.out.println("Autosaver thread id: "+Thread.currentThread().getId());
		while (true) {
			file.save();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println("Autosaver thread is interrupted, stop.");
				break;
			}
		}
	}

}
