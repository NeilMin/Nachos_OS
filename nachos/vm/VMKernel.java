package nachos.vm;

import java.util.LinkedList;
import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	// public class InvertedPair {
	// 	private TranslationEntry entry;
	// 	private Boolean pinned = false;
	// 	InvertedPair(TranslationEntry entry){
	// 		this.entry = entry;
	// 		this.pinned = pinned;
	// 	}
	// 	public void setPinned(boolean pinned){
	// 		this.pinned = pinned;
	// 	}
	// 	public boolean getPinned(){
	// 		return this.pinned;
	// 	}
	// 	public void setEntry(TranslationEntry entry){
	// 		this.entry = entry;
	// 	}
	// 	public TranslationEntry getEntry(){
	// 		return this.entry;
	// 	}
		
	// }
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Initialize this kernel.
	 */
 @Override
	public void initialize(String[] args) {
		super.initialize(args);
		int numPhys = Machine.processor().getNumPhysPages();

		for(int i = 0; i < numPhys; i++) {
			pinned[i] = false;
		}
		swapFile = ThreadedKernel.fileSystem.open("pa3Swap", true);
	}

	/**
	 * Test this kernel.
	 */
 @Override
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
 @Override
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
 @Override
	public void terminate() {
		super.terminate();
		ThreadedKernel.fileSystem.remove("pa3Swap");
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';
	public static OpenFile swapFile;
	private static LinkedList<TranslationEntry> swapPage = new LinkedList<TranslationEntry>();
	//refBit(TranslationEntry, pinned)
	public static TranslationEntry[] invertedPageTable = new TranslationEntry[Machine.processor().getNumPhysPages()];
	public static boolean[] pinned = new boolean[Machine.processor().getNumPhysPages()];
	public static int currentClock = 0;
	public static int freeSpn = 0;

	public static void resetSwap(){

		for(int i = 0; i<swapPage.size(); i++){
			if(swapPage.get(i) != null){
				swapPage.set(i, null);
			}
		}

	}
	public static void setPinned(int index, boolean isPinned){
		lock.acquire();
		pinned[index] = isPinned;
		lock.release();

	}
	public static boolean getPinned(int index){
		lock.acquire();
		boolean isPinned = pinned[index];
		lock.release();
		return isPinned;

	}
	public static void setClock(int current){
		lock.acquire();
		currentClock = current;
		lock.release();

	}
	public static int getClock(){
		
		lock.acquire();
		int clock = currentClock;
		lock.release();
		return clock;

	}
	public static void setInverted(int index, TranslationEntry entry){
		lock.acquire();
		invertedPageTable[index] = entry;
		lock.release();

	}

	public static TranslationEntry getInverted(int index){
		lock.acquire();
		TranslationEntry getEntry = invertedPageTable[index];
		lock.release();

		if(getEntry == null){
			return null;
		}
		return getEntry;
	}
	public static int getInvertedSize(){
		lock.acquire();
		int size = invertedPageTable.length;
		lock.release();
		return size;
	}
	
	
	public static TranslationEntry removeSwap() {
		lock.acquire();
		TranslationEntry removed = swapPage.removeFirst();
		lock.release();
		return removed;

	}

	public static void addSwap(TranslationEntry page) {
		lock.acquire();
		swapPage.add(page);
		lock.release();
	}

	public static int getSwap(TranslationEntry page) {
		lock.acquire();
		int index = swapPage.indexOf(page);
		lock.release();
		return  index;
		// if(index == -1){
		// 	return null;
		// }
		// return swapPage.get(index);
	}

	public static int getSizeSwap() {
		return swapPage.size();
	}

	public static Boolean swapContains(TranslationEntry entry) {
		lock.acquire();
		boolean contains = swapPage.contains(entry);
		lock.release();

		return contains;
	}


}
