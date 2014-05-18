package project;

public class NullFileCache implements FileCache {

	@Override
	public byte[] fetch(String targetFile) {
		return FileUtilities.readBinaryFile(targetFile);
	}

	@Override
	public String toString() {
		return "None 0";
	}
}
