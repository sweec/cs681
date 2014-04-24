package hw9;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class FileSystem {
	public static String pathSeparator = "/";
	private static String parentPath = "..";
	private static String currentPath = ".";
	
	/**
	 * output format
	 */
	private static String DirectoryStart = "|";
	private static String indentString = " ";
	private static String DirectoryString = "_";
	private static int indentStep = 2;

	private ArrayList<Directory> drives = new ArrayList<Directory>();
	private Directory rootDir =	new Directory("root", null, new Date());;
	private Directory current = rootDir;
	
	private FileSystem(){}
	private static FileSystem instance = null;
	private static ReentrantLock lock = new ReentrantLock();
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
	
	public void addDrive(Directory dir) {
		if (!drives.contains(dir)) {
			drives.add(dir);
			dir.setParent(rootDir);
		}
	}
	
	public void removeDrive(Directory dir) {
		drives.remove(dir);
		if (current.under(dir))
			current = rootDir;
	}
	
	public Directory getRootDir() {
		return rootDir;
	}
	
	public Directory getCurrent() {
		return current;
	}
	
	public void setCurrent(FSElement e) {
		if (!e.under(rootDir))
			return;
		current = e.getParent();
		if (!e.isLeaf())
			current = (Directory) e;
	}
	
	public void changeDirectory(Directory dest) {
		current = dest;
	}
	
	public int getInsertionLocation(Directory dir, FSElement element) {
		ArrayList<FSElement> children = dir.getChildren();
		int i = 0, size = children.size();
		for (;i<size;i++)
			if (element.getName().compareToIgnoreCase(children.get(i).getName()) < 0)
				break;
		return i;
	}
	
	public void addChild(Directory parent, FSElement child) {
		parent.appendChild(child, getInsertionLocation(parent, child));
	}
	
	public ArrayList<FSElement> getChildren(Directory current) {
		return current.getChildren();
	}
	
	public boolean listChildren(String dest, boolean detail) {
		FSElement e;
		if (dest == null || dest.isEmpty())
			e = current;
		else
			e = getPathDir(dest);
		if (e == null)
			return false;
		if (e.isLeaf())
			System.out.println((detail)?e.getInfo():e.getName());
		else {
			ArrayList<FSElement> children = ((Directory) e).getChildren();
			for (FSElement child:children) {
				if (detail)
					System.out.println(child.getInfo());
				else
					System.out.print(child.getName()+" ");
			}
			if (!detail && !children.isEmpty())
				System.out.println();
		}
		return true;
	}
	
	public String getAbsolutePath(FSElement element) {
		if (element == null)
			return null;
		else if (element == rootDir)
			return pathSeparator;
		String path = element.getName();
		do {
			element = element.getParent();
			if (element == rootDir) break;
			path = element.getName()+pathSeparator+path;
		} while (true);
		return pathSeparator+path;
	}
	
	public FSElement getPathDir(String path) {
		if (path == null)
			return null;
		FSElement dir = current;
		if (path.startsWith(pathSeparator))
			dir = rootDir;
		String[] names = path.split(pathSeparator);
		for (String name:names) {
			if (name.isEmpty()) continue;
			if (dir.isLeaf()) return null;	// file can not change directory
			if (name.equals(parentPath)) {
				if (dir != rootDir)
					dir = dir.getParent();
			} else if (!dir.equals(currentPath)) {
				boolean failed = true;
				ArrayList<FSElement> children = ((Directory) dir).getChildren();
				for (FSElement child:children) {
					if (child.getName().equalsIgnoreCase(name)) {
						dir = child;
						failed = false;
						break;
					}
				}
				if (failed) return null;	// invalid path
			}
		}
		return dir;
	}
	
	public String pwd() {
		String path = getAbsolutePath(current);
		System.out.println(path);
		return path;
	}
	
	public boolean changeDirectory(String dest) {
		if (dest == null || dest.isEmpty()) {
			current = rootDir;
			return true;
		}
		FSElement e = getPathDir(dest);
		if (e == null || e.isLeaf())
			return false;
		current = (Directory) e;
		return true;
	}
	
	public boolean makeDirectory(String path, String owner) {
		if (path == null || path.isEmpty())
			return false;
		FSElement element = getPathDir(path);
		if (element != null)	// already exists
			return false;
		// trim "/" at the end
		while (path.endsWith(pathSeparator) && path.length()>=2)
			path = path.substring(0, path.length()-1);
		FSElement parent = current;
		int index = path.lastIndexOf(pathSeparator);
		if (index>=0) {
			parent = getPathDir(path.substring(0, index+1));
			if (parent == null || parent.isLeaf())
				return false;
			path = path.substring(index+1, path.length());
		}
		if (path.startsWith(currentPath))	// a trivial check of dir name
			return false;
		Directory child = new Directory(path, (Directory) parent, new Date());
		child.setOwner(owner);
		addChild((Directory) parent, child);
		return true;
	}
	
	public boolean removeDirectory(String path, boolean force) {
		if (path == null || path.isEmpty())
			return false;
		FSElement dir = getPathDir(path);
		if (dir == null || dir.isLeaf())	// not a directory
			return false;
		if (dir == rootDir) {
			System.out.println("root directory cannot be removed");
			return false;
		}
		ArrayList<FSElement> children = ((Directory) dir).getChildren();
		if (children != null && !children.isEmpty() && !force && !(dir instanceof Link)) {	// not empty
			System.out.println("Directory not empty. Not removed.");
			return false;
		}
		boolean under = current.under((Directory) dir);
		Directory parent = dir.getParent();
		parent.removeChild(dir);
		if (under)	// whether current is removed
			current = parent;
		return true;
	}
	
	public boolean removeLeaf(String path) {
		if (path == null || path.isEmpty())
			return false;
		FSElement child = getPathDir(path);
		if (child == null || !child.isLeaf())	// not a directory
			return false;
		child.getParent().removeChild(child);
		return true;
	}
	
	public boolean newFile(String path, String owner, int size) {
		if (path == null || path.isEmpty())
			return false;
		FSElement element = getPathDir(path);
		if (element != null)	// already exists
			return false;
		// trim "/" at the end
		while (path.endsWith(pathSeparator) && path.length()>=2)
			path = path.substring(0, path.length()-1);
		FSElement parent = current;
		int index = path.lastIndexOf(pathSeparator);
		if (index>=0) {
			parent = getPathDir(path.substring(0, index+1));
			if (parent == null || parent.isLeaf())
				return false;
			path = path.substring(index+1, path.length());
		}
		if (path.equalsIgnoreCase(currentPath) ||
				path.equalsIgnoreCase(parentPath))	// a trivial check of dir name
			return false;
		File child = new File(path, size, (Directory) parent, new Date());
		child.setOwner(owner);
		addChild((Directory) parent, child);
		return true;
	}
	
	public boolean createLink(String target, String link) {
		FSElement s = getPathDir(target);
		if (s == null)
			return false;
		if (link.endsWith(pathSeparator))
			return false;
		FSElement g = getPathDir(link);
		if (g != null)
			return false;
		FSElement parent = current;
		int index = link.lastIndexOf(pathSeparator);
		if (index>=0) {
			parent = getPathDir(link.substring(0, index+1));
			if (parent == null || parent.isLeaf())
				return false;
			link = link.substring(index+1, link.length());
		}
		Link l = new Link(link, (Directory) parent, new Date(), s);
		addChild((Directory) parent, l);
		return true;
	}
	
	private static void println(ArrayList<Integer> parents, String message) {
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
	
	private static void showElement(FSElement e, int layer, ArrayList<Integer> parents, boolean hasNext) {
		println(parents, e.getInfo());
		if (!hasNext && parents != null)
			parents.remove(new Integer((indentStep+1)*(layer-1)));
		if (!e.isLeaf()) {
			ArrayList<FSElement> children = ((Directory) e).getChildren();
			if (children != null && !children.isEmpty()) {
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
		if (rootDir == null)
			System.out.println("File system does not exist.");
		showElement(rootDir, 0, null, false);
	}
	
	public static void initNewDrive(FileSystem fs, Directory rootDir) {
		String root = "root", User = "User";
		fs.addDrive(rootDir);
		Directory Windows = new Directory("Windows", rootDir, new Date());
		fs.addChild(rootDir, Windows);
		Windows.setOwner(root);
		File a = new File("a.txt", 127, Windows, new Date());a.setOwner(root);
		fs.addChild(Windows, a);
		File b = new File("b.dat", 13765, Windows, new Date());b.setOwner(root);
		fs.addChild(Windows, b);
		File c = new File("c.txt", 448, Windows, new Date());c.setOwner(root);
		fs.addChild(Windows, c);
		Directory MyDocument = new Directory("Document", rootDir, new Date());
		fs.addChild(rootDir, MyDocument);
		MyDocument.setOwner(User);
		File d = new File("d.exe", 24680, MyDocument, new Date());
		fs.addChild(MyDocument, d);
		d.setOwner(User);
		Link x = new Link("x", MyDocument, new Date(), d);
		fs.addChild(MyDocument, x);
		x.setOwner(User);
		Directory MyPictures = new Directory("Pictures", MyDocument, new Date());
		fs.addChild(MyDocument, MyPictures);
		MyPictures.setOwner(User);
		File e = new File("e.ppt", 159782, MyPictures, new Date());e.setOwner(User);
		fs.addChild(MyPictures, e);
		File f = new File("f.doc", 3095892, MyPictures, new Date());f.setOwner(User);
		fs.addChild(MyPictures, f);
		Link y = new Link("y", MyPictures, new Date(), Windows);
		fs.addChild(MyPictures, y);
		y.setOwner(User);
	}
	
	public static void main(String[] args) {
		FileSystem fs = FileSystem.getInstance();
		String[] driveNames = {"driveA", "driveB", "driveC"};
		for (String name:driveNames) {
			Directory rootDir = new Directory(name, null, new Date());
			rootDir.setOwner("root");
			initNewDrive(fs, rootDir);
		}
		
		FileQueue queue = new FileQueue();
		Thread[] cths = new Thread[3];
		Thread[] iths = new Thread[3];
		FileCrawler crawlers[] = new FileCrawler[3];
		FileIndexer indexers[] = new FileIndexer[3];
		for (int i=0;i<3;i++) {
			crawlers[i] = new FileCrawler(fs.drives.get(i), queue);
			indexers[i] = new FileIndexer(queue);
			cths[i] = new Thread(crawlers[i]); cths[i].start();
			iths[i] = new Thread(indexers[i]); iths[i].start();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		for (int i=0;i<3;i++) {
			crawlers[i].setDone(true);
			cths[i].interrupt();
			indexers[i].setDone(true);
			iths[i].interrupt();
		}
	}
}
