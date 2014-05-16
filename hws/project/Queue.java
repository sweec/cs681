package project;

public interface Queue {
	public abstract int size();
	public abstract void put(Runnable obj);
	public abstract Runnable get();
	public abstract void clear();
}
