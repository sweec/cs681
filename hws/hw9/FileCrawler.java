package hw9;

public class FileCrawler implements Runnable {
	private Directory root;
	private FileQueue queue;
	private volatile boolean done = false;
	
	public FileCrawler(Directory root, FileQueue queue) {
		this.root = root;
		this.queue = queue;
	}
	
	public void setDone(boolean done) {
		this.done = done;
		queue.setDone(done);
	}
	
	public void crawl(Directory dir) {
		for (FSElement e:dir.getChildren()) {
			if (done) break;
			if (e instanceof File)
				queue.put((File)e);
			else if (!(e instanceof Link))
				crawl((Directory)e);
		}
	}

	@Override
	public void run() {
		crawl(root);
	}
}
