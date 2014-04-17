package hw2;

import java.util.ArrayList;

public class Fibonacci implements Runnable {
	private int total;
	private ArrayList<Integer> nums;

	public Fibonacci(int total) {
		this.total = total;
	}
	
	public ArrayList<Integer> getNumbers() {
		return nums;
	}
	
	@Override
	public void run() {
		nums = new ArrayList<Integer>();
		calculateNumbers(total);
	}

	private void calculateNumbers(int n) {
		if (n < 1)
			return;
		if (n == 1)
			nums.add(0);
		else if (n == 2) {
			nums.add(0);
			nums.add(1);
		} else {
			calculateNumbers(n-1);
			int size = nums.size();
			nums.add(nums.get(size-1)+nums.get(size-2));
		}
	}
	
	public static void main(String[] args) {
		int total = 3;
		if (args.length > 0)
			total = Integer.parseInt(args[0]);
		Fibonacci f = new Fibonacci(total);
		Thread t = new Thread(f);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Error: "+e.getMessage());
			e.printStackTrace();
		}
		for (int i:f.getNumbers())
			System.out.print(i+" ");
		System.out.println();
	}
}
