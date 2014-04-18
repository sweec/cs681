package hw9;

import java.util.Date;

public class File extends FSElement {

	public File(String name, int size, Directory parent, Date created) {
		super(parent, created);
		setName(name);
		setSize(size);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public void calculateSize() {
	}
	
}
