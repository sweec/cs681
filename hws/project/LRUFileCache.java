package project;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUFileCache implements FileCache {
	private HashMap<String, CacheStore> cache = new HashMap<String, CacheStore>();
	private final int MinCacheSize = 5;
	private int cacheSize = MinCacheSize;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public LRUFileCache() {}
	public LRUFileCache(Integer size) {
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
		String toDrop = null;
		if (cache.size() >= cacheSize) {
			AccessCounter ac = AccessCounter.getInstance();
			long old = Long.MAX_VALUE;
			for (String key:cache.keySet()) {
				long cur = ac.getAge(key);
				if (cur < old) {
					old = cur;
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
		return "LRU "+cacheSize;
	}
}
