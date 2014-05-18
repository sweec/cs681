package project;

public interface FileCache {
	public abstract byte[] fetch(String targetFile);
}
