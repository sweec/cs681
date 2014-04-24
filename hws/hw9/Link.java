package hw9;

import java.util.ArrayList;
import java.util.Date;

public class Link extends Directory {
	private FSElement target;
	
	public Link(String name, Directory parent, Date created, FSElement target) {
		super(name, parent, created);
		this.target = target;
	}
	
	@Override
	public boolean isLeaf() {
		if (target != null)
			return target.isLeaf();
		else
			return true;
	}
	
	@Override
	public void accept(FSVisitor v) {
		v.visit(this);
	}
	
	@Override
	public String getInfo() {
		String info = super.getInfo();
		if (target != null)
			return info+"\n      --> "+target.getPath();
		else
			return info+" --> null, orphan link";
	}
	
	@Override
	public int getSize() {
		return super.getDiskUtil();
	}
	
	public ArrayList<FSElement> getChildren() {
		if (target != null && !target.isLeaf())
			return ((Directory) target).getChildren();
		else
			return null;
	}
	
	public void addChild(FSElement child) {
		if (target != null && !target.isLeaf()) {
			Directory parent = (Directory) target;
			parent.getFileSystem().addChild(parent, child);
		}
	}
	
	public void removeChild(FSElement child) {
		if (target != null && !target.isLeaf())
			((Directory) target).removeChild(child);
	}
	
	public FileSystem getFileSystem() {
		if (target != null && !target.isLeaf())
			return ((Directory) target).getFileSystem();
		else
			return null;
	}
}
