package project;

public class CacheStore {
	private byte[] content;
	
	public CacheStore(byte[] content) {
		this.content = content;
	}
	
	public byte[] getContent() {
		return content;
	}
}
