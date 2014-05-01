package hw11;

public interface Queue {
	public abstract int size();
	public abstract void put(Runnable obj);
	public abstract Runnable get();
}
