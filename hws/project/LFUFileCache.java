package project;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LFUFileCache implements FileCache {
	private HashMap<String, CacheStore> cache = new HashMap<String, CacheStore>();
	private final int MinCacheSize = 5;
	private int cacheSize = MinCacheSize;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public LFUFileCache(Integer size) {
		if (size != null && size > MinCacheSize)
			this.cacheSize = size;
	}
	
	@Override
	public byte[] fetch(String targetFile) {
		byte[] content = null;
		lock.writeLock().lock();
		if (!cache.containsKey(targetFile)) {
			content = cacheFile(targetFile);
		} else
			lock.readLock().lock();
		lock.writeLock().unlock();
		if (content == null) {
			content = cache.get(targetFile).getContent();
			lock.readLock().unlock();
		}
		return content;
	}

	private byte[] cacheFile(String targetFile) {
		if (cache.size() >= cacheSize) {
			AccessCounter ac = AccessCounter.getInstance();
			String toDrop = null;
			int min = Integer.MAX_VALUE;
			for (String key:cache.keySet()) {
				int c = ac.getCount(key);
				if (c < min) {
					min = c;
					toDrop = key;
				}
			}
			cache.remove(toDrop);
		}
		byte[] content = FileUtilities.readBinaryFile(targetFile);
		cache.put(targetFile, new CacheStore(content));
		return content;
	}
	
	@Override
	public String toString() {
		return "LFU "+cacheSize;
	}
}
