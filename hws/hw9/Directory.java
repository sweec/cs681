package hw9;

import java.util.ArrayList;
import java.util.Date;

public class Directory extends FSElement {
	
	private FileSystem fileSystem = null;
	private ArrayList<FSElement> children = null;

	public Directory(String name, Directory parent, Date created) {
		super(parent, created);
		setName(name);
	}

	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
	
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public void calculateSize() {
		int size = 0;
		if (children != null)
			for (FSElement e:children)
				size += e.getSize();
		setSize(size);
	}

	public ArrayList<FSElement> getChildren() {
		return children;
	}
	
	public void appendChild(FSElement child) {
		if (children == null)
			children = new ArrayList<FSElement>();
		if (!children.contains(child))
			children.add(child);
		if (!child.isLeaf())
			((Directory) child).setFileSystem(fileSystem);
	}
	
	public void removeChild(FSElement child) {
		if (children != null)
			children.remove(child);
	}

	public void addChild(FSElement child, int index) {
		// TODO Auto-generated method stub

	}
}
