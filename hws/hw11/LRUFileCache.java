package hw11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUFileCache implements FileCache {
	private HashMap<String, String> cache = new HashMap<String, String>();
	private HashMap<String, Integer> ages = new HashMap<String, Integer>();
	private int MaxCacheSize = 5;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public LRUFileCache() {}
	public LRUFileCache(int size) {
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
		String toDrop = null;
		if (cache.size() >= MaxCacheSize) {
			for (String key:ages.keySet()) {
				if (ages.get(key) == MaxCacheSize) {
					toDrop = key;
					break;
				}
			}
			ages.remove(toDrop);
			cache.remove(toDrop);
		}
		String content = FileUtilities.readFile(targetFile);
		cache.put(targetFile, content);
		update(targetFile);
		return content;
	}
	
	private void update(String targetFile) {
		Integer age = ages.get(targetFile);
		if (age == null)
			age = MaxCacheSize;
		Iterator<Entry<String, Integer>> it = ages.entrySet().iterator();
		Map.Entry<String, Integer> entry;
		while (it.hasNext()) {
			entry = it.next();
			int a = entry.getValue();
			if (a < age)
				entry.setValue(a+1);
		}
		ages.put(targetFile, 1);
	}
}
