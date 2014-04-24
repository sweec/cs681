package hw9;

import java.util.Date;

public class File extends FSElement {

	public File(String name, int size, Directory parent, Date created) {
		super(name, parent, created);
		setSize(size);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public void accept(FSVisitor v) {
		v.visit(this);
	}
	
	@Override
	public int getDiskUtil() {
		return getSize();
	}
}
