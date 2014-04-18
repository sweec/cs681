package hw9;

import java.util.Date;

public abstract class FSElement {

	private String name = null;
	private String owner = null;
	private Date created = null;
	private Date lastModified = null;
	private int size = 0;
	private Date lastGetSize = null;
	private Directory parent = null;
	
	public FSElement(Directory parent, Date created) {
		this.parent = parent;
		this.created = created;
		this.lastModified = created;
		if (parent != null)
			parent.appendChild(this);
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
		if (parent != null)
			parent.appendChild(this);
	}
	
	public Directory getParent() {
		return parent;
	}
	
	public abstract boolean isLeaf();
	
	public String getInfo() {
		return name+", "+((isLeaf())?"file":"directory")+", "+getSize()+", "+owner;
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
	
	public abstract void calculateSize();
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
		if (parent != null)
			parent.setLastModified(lastModified);
	}
	
}
