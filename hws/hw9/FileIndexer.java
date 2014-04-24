package hw9;

public class FileIndexer implements Runnable {
	private FileQueue queue;
	private volatile boolean done = false;
	
	public FileIndexer(FileQueue queue) {
		this.queue = queue;
	}
	
	public void setDone(boolean done) {
		this.done = done;
		queue.setDone(done);
	}
	
	public void indexFile(File file) {
		if (file != null)
			System.out.println(file.getInfo()+" "+"Thread "+Thread.currentThread().getId()
					+"\t"+file.getPath());
	}

	@Override
	public void run() {
		while (!done)
			indexFile(queue.get());
	}
}
