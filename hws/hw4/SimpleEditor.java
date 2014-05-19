package hw4;

public class SimpleEditor implements Runnable {
	private SimpleFile file;
	
	public SimpleEditor(SimpleFile file) {
		this.file = file;
	}

	@Override
	public void run() {
		System.out.println("Editor thread id: "+Thread.currentThread().getId());
		while (true) {
			file.change();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Editor thread is Interrupted, stop.");
				break;
			} finally {
				file.save();
			}
		}
	}

}
