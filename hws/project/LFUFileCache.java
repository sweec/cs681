package project;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LFUFileCache implements FileCache {
	private HashMap<String, String> cache = new HashMap<String, String>();
	private HashMap<String, Integer> count = new HashMap<String, Integer>();
	private int MaxCacheSize = 5;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public LFUFileCache(int size) {
		this.MaxCacheSize = size;
	}
	
	@Override
	public String fetch(String targetFile) {
		String content = null;
		lock.writeLock().lock();
		if (!cache.containsKey(targetFile)) {
			content = cacheFile(targetFile);
		} else
			lock.readLock().lock();
		update(targetFile);
		lock.writeLock().unlock();
		if (content == null) {
			content = cache.get(targetFile);
			lock.readLock().unlock();
		}
		return content;
	}

	private String cacheFile(String targetFile) {
		if (cache.size() >= MaxCacheSize) {
			String toDrop = null;
			int min = Integer.MAX_VALUE;
			for (String key:count.keySet()) {
				int c = count.get(key);
				if (c < min) {
					min = c;
					toDrop = key;
				}
			}
			count.remove(toDrop);
			cache.remove(toDrop);
		}
		String content = FileUtilities.readFile(targetFile);
		cache.put(targetFile, content);
		update(targetFile);
		return content;
	}
	
	private void update(String targetFile) {
		Integer c = count.get(targetFile);
		if (c == null)
			count.put(targetFile, 1);
		else
			count.put(targetFile, c+1);
	}
}
