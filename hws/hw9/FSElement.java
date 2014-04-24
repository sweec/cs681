package hw9;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FSElement {

	public static final int DEFAULT_SIZE = 1024;
	private String name = null;
	private String owner = null;
	private Date created = null;
	private Date lastModified = null;
	private int size = 0;
	private Date lastGetSize = null;
	private Directory parent = null;
	
	public FSElement(String name, Directory parent, Date created) {
		this.name = name;
		this.parent = parent;
		this.created = created;
		this.lastModified = created;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setParent(Directory parent) {
		if (this.parent != null)
			this.parent.removeChild(this);
		this.parent = parent;
		//if (parent != null)
			//parent.getFileSystem().addChild(parent, this);
	}
	
	public Directory getParent() {
		return parent;
	}
	
	public boolean isLeaf() {
		return false;
	}
	
	public void accept(FSVisitor v) {}
	
	public int getDiskUtil() {
		return DEFAULT_SIZE;	// default to 1024 bytes for just dir
	}
	
	public String getInfo() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
		return name+"\t"+getClass().getSimpleName().toLowerCase()+"\t"+getSize()+"\t"+owner+
				"\t"+sdf.format(lastModified);
	}
	
	public Date getCreated() {
		return created;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public int getSize() {
		if (lastGetSize != lastModified)
			calculateSize();
		lastGetSize = lastModified;
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void calculateSize() {}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
		if (parent != null)
			parent.setLastModified(lastModified);
	}
	
	public String getPath() {
		String path = "";
		String lastName = name;
		Directory p = parent;
		while (p != null) {
			path = FileSystem.pathSeparator + lastName + path;
			lastName = p.getName();
			p = p.getParent();
		}
		if (path == "")
			path = FileSystem.pathSeparator;
		return path;
	}
	
	public boolean under(Directory p) {
		boolean under = false;
		Directory dir = this.parent;
		if (!isLeaf())
			dir = (Directory)this;
		do {
			if (dir == p) {
				under = true;
				break;
			}
			dir = dir.getParent();
		} while (dir != null);
		return under;
	}

	@Override
	public String toString() {
		if (parent != null)
			return name;
		else
			return FileSystem.pathSeparator;
	}
}
