package hw9;

import java.util.ArrayList;
import java.util.Date;

public class Directory extends FSElement {
	
	private FileSystem fileSystem = null;
	private ArrayList<FSElement> children = new ArrayList<FSElement>();

	public Directory(String name, Directory parent, Date created) {
		super(name, parent, created);
	}

	public FileSystem getFileSystem() {
		if (fileSystem == null)
			fileSystem = FileSystem.getInstance();
		return fileSystem;
	}
	
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public void calculateSize() {
		int size = super.getDiskUtil();
		if (children != null)
			for (FSElement e:children)
				size += e.getSize();
		setSize(size);
	}
	
	@Override
	public void accept(FSVisitor v) {
		v.visit(this);
		for (FSElement e:children)
			if (e != null)
				e.accept(v);
	}
	
	public ArrayList<FSElement> getChildren() {
		return children;
	}
	
	public void appendChild(FSElement child, int index) {
		if (children == null)
			children = new ArrayList<FSElement>();
		if (!children.contains(child))
			children.add(index, child);
		if (child.getParent() != this)
			child.setParent(this);
		setLastModified(new Date());
	}
	
	public void removeChild(FSElement child) {
		if (children != null)
			children.remove(child);
		setLastModified(new Date());
	}

}
