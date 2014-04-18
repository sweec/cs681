package hw9;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class FileSystem {

	private static String DirectoryStart = "|";
	private static String indentString = " ";
	private static String DirectoryString = "_";
	private static int indentStep = 2;
	private HashMap<String, Directory> drives = new HashMap<String, Directory>();
	private static FileSystem instance = null;
	private static ReentrantLock lock = new ReentrantLock();
	
	private FileSystem() {}
	public static FileSystem getInstance() {
		if (instance == null) {
			lock.lock();
			if (instance == null) {
				FileSystem inst = new FileSystem();
				instance = inst;
			}
			lock.unlock();
		}
		return instance;
	}

	public void addDrive(String name, Directory rootDir) {
		drives.put(name, rootDir);
	}
	
	public Directory getRootDir(String name) {
		return drives.get(name);
	}
	
	private void println(ArrayList<Integer> parents, String message) {
		if (parents != null) {
			int j = 0;
			for (int parent:parents) {
				for (;j<parent;j++)
					System.out.print(indentString);
				System.out.print(DirectoryStart);
				j++;
			}
			for (int i=0;i<indentStep;i++)
				System.out.print(DirectoryString);
		}
		System.out.println(message);
	}
	
	private void showElement(FSElement e, int layer, ArrayList<Integer> parents, boolean hasNext) {
		println(parents, e.getInfo());
		if (!hasNext && parents != null)
			parents.remove(new Integer((indentStep+1)*(layer-1)));
		if (!e.isLeaf()) {
			ArrayList<FSElement> children = ((Directory) e).getChildren();
			if (children != null) {
				if (parents == null)
					parents = new ArrayList<Integer>();
				parents.add((indentStep+1)*layer);
				layer++;
				int i = 0;
				for (;i<children.size()-1;i++)
					showElement(children.get(i), layer, parents, true);
				showElement(children.get(i), layer, parents, false);
			}
		}
	}
	
	public void showAllElements() {
		for (Directory rootDir:drives.values())
			showElement(rootDir, 0, null, false);
	}
	
	public static void main(String[] args) {
		String root = "root", User = "TestUser";
		FileSystem fs = getInstance();
		Directory rootDir = new Directory("root", null, new Date());
		rootDir.setOwner("root");
		fs.addDrive("C", rootDir);
		Directory Windows = new Directory("Windows", rootDir, new Date());
		Windows.setOwner(root);
		File a = new File("a", 127, Windows, new Date());a.setOwner(root);
		File b = new File("b", 13765, Windows, new Date());b.setOwner(root);
		File c = new File("c", 448, Windows, new Date());c.setOwner(root);
		Directory MyDocument = new Directory("MyDocument", rootDir, new Date());
		MyDocument.setOwner(User);
		File d = new File("d", 24680, MyDocument, new Date());
		d.setOwner(User);
		Directory MyPictures = new Directory("MyPictures", MyDocument, new Date());
		MyPictures.setOwner(User);
		File e = new File("e", 159782, MyPictures, new Date());e.setOwner(User);
		File f = new File("f", 3095892, MyPictures, new Date());f.setOwner(User);
		System.out.println("The root direcotry has a size of "+rootDir.getSize()+".");
		System.out.println("\nIt has a tree structure:\n");
		fs.showAllElements();
	}
}
