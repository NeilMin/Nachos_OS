package nachos.vm;

import nachos.machine.*;
import nachos.userprog.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
 @Override
	public void saveState() {
		super.saveState();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
 @Override
	public void restoreState() {
		super.restoreState();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
 @Override
	protected boolean loadSections() {
		UserKernel.lock.acquire();
	
		// Map virtual to physical
		//no phyial page check
		pageTable = new TranslationEntry[numPages];
		for (int i = 0; i < numPages; i++) {
			TranslationEntry entry = new TranslationEntry(i, -1, false, false, false, false);
			pageTable[i] = entry;
		}
		UserKernel.lock.release();

		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>. give back spn
	 */
 @Override
	protected void unloadSections() {
		super.unloadSections();
		UserKernel.lock.acquire();

		VMKernel.resetSwap();
		VMKernel.resetFreePhyPages();
		UserKernel.lock.release();


		//add back the spn pages 
	}
	
	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
@Override
	public void handleException(int cause) {
		Processor processor = Machine.processor();
		switch (cause) {
			case Processor.exceptionPageFault:
				int virtualaddr = Machine.processor().readRegister(Processor.regBadVAddr);
				Lib.debug(dbgVM, "handlefault from handleexcption");

				handlePageFault(virtualaddr); 
				break;
		default:
			super.handleException(cause);
			break;
		}
	}
	@Override
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		//set used

		//set dirty
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		if (vaddr < 0 || vaddr >=  numPages*pageSize)
			return 0;
		// check if virtual page is valid
		int vpnValid = Machine.processor().pageFromAddress(vaddr);
		TranslationEntry entryValid = pageTable[vpnValid];
		if(entryValid.valid == false){
			// VMKernel.lock.release();
			Lib.debug(dbgVM, "handlefault from WRITEvirtualmemtps");

			handlePageFault(vaddr);
			// VMKernel.lock.acquire();

		}
		//loop 
		int totalBytes = length;
		int currentAddr = vaddr;
		int amountWrite = 0;

		while(totalBytes > 0) {
			// System.out.println("while in writevirtualmemory currentaddr "+currentAddr+ " totlabytes"+ totalBytes + " amoungwrite "+ amountWrite);
			int vpn = Machine.processor().pageFromAddress(currentAddr);
			int off = Machine.processor().offsetFromAddress(currentAddr);

			int ppn = -1;
			for(int i = 0; i < numPages; i++) {
				if(pageTable[i].vpn == vpn) {
					// if(pageTable[i].readOnly) {
					// 	return amountWrite;
					// }
					ppn = pageTable[i].ppn;
				}
			}

			if(ppn == -1) {
				return amountWrite;
			}

			// set pin
			// VMKernel.lock.acquire();
			// VMKernel.pinned[ppn] = true;
			// VMKernel.lock.release();
			VMKernel.setPinned(ppn, true);


			
			int paddr = pageSize * ppn + off;
			int amount = Math.min(totalBytes, pageSize - off);
			// System.out.println("amount got is "+amount);

			System.arraycopy(data, amountWrite + offset, memory, paddr, amount);

			// unpin
			// VMKernel.lock.acquire();
			// VMKernel.pinned[ppn] = false;
			// VMKernel.lock.release();
			VMKernel.setPinned(ppn, false);


			//set used and dirty for write 
			entryValid.dirty = true;
			entryValid.used = true;

			currentAddr += amount;
			totalBytes -= amount;
			amountWrite += amount;
			// System.out.println("amountwrite is "+amountWrite);

		}

		return amountWrite;
	}

	@Override
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		//set used
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		if (vaddr < 0 || vaddr >= numPages*pageSize)
			return 0;
			
		// check if virtual page is valid
		int vpnValid = Machine.processor().pageFromAddress(vaddr);
		TranslationEntry entryValid = pageTable[vpnValid];
		if(entryValid.valid == false){
			// VMKernel.lock.release();
			Lib.debug(dbgVM, "handlefault from READvirtualmemtps");
			handlePageFault(vaddr);
			// VMKernel.lock.acquire();

		}


		int totalBytes = length;
		int currentAddr = vaddr;
		int amountRead = 0;

		while(totalBytes > 0) {
			int vpn = Machine.processor().pageFromAddress(currentAddr);
			int off = Machine.processor().offsetFromAddress(currentAddr);

			int ppn = -1;
			for(int i = 0; i < numPages; i++) {
				if(pageTable[i].vpn == vpn) {
					ppn = pageTable[i].ppn;
				}
			}

			if(ppn == -1) {
				return amountRead;
			}

			// set pin
			// VMKernel.lock.acquire();
			// VMKernel.pinned[ppn] = true;
			// VMKernel.lock.release();
			VMKernel.setPinned(ppn, true);


			int paddr = pageSize * ppn + off;
			int amount = Math.min(totalBytes, pageSize - off);

			System.arraycopy(memory, paddr, data, amountRead + offset, amount);

			//unpin
			// VMKernel.lock.acquire();
			// VMKernel.pinned[ppn] = false;
			// VMKernel.lock.release();
			VMKernel.setPinned(ppn, false);

			
			entryValid.used = true;


			currentAddr += amount;
			totalBytes -= amount;
			amountRead += amount;
		}

		return amountRead;
	}
	public void handlePageFault(int virtualaddr){
		//intialize inverted page
		//virtual address that causes page fault
		// int virtualaddr = Machine.processor().readRegister(Processor.regBadVAddr);
		//swap, fault entry need ppn, victim gives ppn, find a place to save vicitumm info into swap
		int faultVpn = Machine.processor().pageFromAddress(virtualaddr);
		TranslationEntry faultEntry = pageTable[faultVpn];
		//trnaslationentry victim
		TranslationEntry victim = null; 
		int indexToSave = 0;
		// System.out.println("page fault vpn is "+faultVpn);
		Lib.debug(dbgVM,"page fault vpn is "+faultVpn);

		//no free phy page
		if(VMKernel.getSize() == 0){
			Lib.debug(dbgVM,"no free phy page");
			int numPhyPage = Machine.processor().getNumPhysPages();
			//while loop intil find a phuscial page
			while(true){
				int current = (VMKernel.getClock())%numPhyPage;
				//check current ppn is pinned
				if(VMKernel.pinned[current] == true){
					VMKernel.setClock(VMKernel.getClock() + 1);
					continue;
				}
				//if the bit is off
				if(VMKernel.getInverted(current).used == false){
					victim = VMKernel.getInverted(current);
					// System.out.println("the bit is off ");
					Lib.debug(dbgVM,"the bit is off");
					//ref bit is off, evict page and check if dirty
					if(victim.dirty){
						Lib.debug(dbgVM,"the page is dirty vpn is "+faultVpn);
						// System.out.println("the page is dirty ");
						
						//check if entry has beenn stored in swap file before
						if(VMKernel.getSwap(victim) != -1){
							indexToSave = VMKernel.getSwap(victim);

						}else{
							//add vicu to swap
							indexToSave = VMKernel.getSizeSwap();
							VMKernel.addSwap(victim);


						}
					
						faultEntry.ppn = current;
						victim.valid = false;
						Lib.debug(dbgVM,"swap faltuentry's ppn is "+faultEntry.ppn+" victim 's ppn is "+victim.ppn);
						
						Lib.debug(dbgVM,"vicitm vpn" + victim.vpn + "vicitm ppn" + victim.ppn +"spn "+indexToSave+ " mssinbg ppn "+faultEntry.ppn+ " missing vpn is "+faultVpn);

						// System.out.println("vicitm is "+current+" faltuentry's ppn is "+faultEntry.ppn);					
						//update inverted pagetable
						VMKernel.setInverted(faultEntry.ppn, faultEntry);
						// VMKernel.invertedPageTable[faultEntry.ppn] = faultEntry;
							
						//pin
						// VMKernel.setPinned(faultEntry.ppn, true);
						
						//swap outs
						// System.out.println("victim 's ppn is "+victim.ppn+" index to dave "+indexToSave);
						// byte[] buf = new byte[pageSize];
						int offset = victim.ppn*pageSize;
						byte[] memory = Machine.processor().getMemory();
						VMKernel.swapFile.write(indexToSave*pageSize, memory, offset, pageSize);
						
						//unpin
						// VMKernel.setPinned(faultEntry.ppn, false);

					} else{
						faultEntry.ppn = current;
						victim.valid = false;
						VMKernel.setInverted(faultEntry.ppn, faultEntry);
						// VMKernel.invertedPageTable[faultEntry.ppn] = faultEntry;
						Lib.debug(dbgVM,"clean page fault entry vpn is "+faultEntry.vpn+ " ppn is "+faultEntry.ppn);
						Lib.debug(dbgVM,"clean page vicitm's ppn "+current+" victim's vpn is "+victim.vpn);
					}
					// VMKernel.currentClock = current + 1;
					VMKernel.setClock(current + 1);
					break;
				}else{
					//set the inverted page off
					// VMKernel.invertedPageTable[current].used = false;
					VMKernel.getInverted(current).used = false;

				}
				//incremnt counter
				VMKernel.setClock(VMKernel.getClock() + 1);
				// VMKernel.currentClock++;
			
			}
			
		}else{
			
			faultEntry.ppn = VMKernel.remove();
			//set the invverted table 
			Lib.debug(dbgVM,"get free page, ppn is "+faultEntry.ppn);
			VMKernel.setInverted(faultEntry.ppn, faultEntry);
			// VMKernel.invertedPageTable[faultEntry.ppn] = faultEntry;
		}
		
		// System.out.println("now is ppn is "+faultEntry.ppn);
		boolean foundVPN = false;

		//check if faultEntry.vpn is in swap file, if yes read from swap file
		if(VMKernel.swapContains(faultEntry)){
			//read from swap in
			//pin
			// VMKernel.lock.acquire();
			// VMKernel.pinned[faultEntry.ppn] = true;
			// VMKernel.lock.release();
			Lib.debug(dbgVM, "load from swap file vpn "+faultEntry.vpn+" ppn is "+faultEntry.ppn);
			// Lib.debug(dbgVM,"vicitm vpn" + victim.vpn + "vicitm ppn" + victim.ppn +"spn "+indexToSave+ " mssinbg ppn "+faultEntry.ppn);

			VMKernel.setPinned(faultEntry.ppn, true);

			//fault entry index to save
			//swap outs
			int offset = faultEntry.ppn*pageSize;
			byte[] memory = Machine.processor().getMemory();
			VMKernel.swapFile.read(VMKernel.getSwap(faultEntry)*pageSize, memory, offset, pageSize);

			VMKernel.setPinned(faultEntry.ppn, false);

			// VMKernel.lock.acquire();
			// VMKernel.pinned[faultEntry.ppn] = false;
			// VMKernel.lock.release();
		}else{
			// System.out.println("vpn in else is "+faultEntry.vpn);
			for (int s = 0; s < coff.getNumSections(); s++) {
				CoffSection section = coff.getSection(s);
				for (int i = 0; i < section.getLength(); i++) {
					int vpn = section.getFirstVPN() + i;
					
					if(vpn == faultVpn) {
						Lib.debug(dbgVM, "load in coffsection vpn "+faultEntry.vpn+" ppn is "+faultEntry.ppn);
						foundVPN = true;
						// VMKernel.lock.acquire();
						// VMKernel.pinned[faultEntry.ppn] = true;
						// VMKernel.lock.release();
						VMKernel.setPinned(faultEntry.ppn, true);


						section.loadPage(i, pageTable[vpn].ppn);
						
						// VMKernel.lock.acquire();
						// VMKernel.pinned[faultEntry.ppn] = false;
						// VMKernel.lock.release();
						VMKernel.setPinned(faultEntry.ppn, false);

						if(section.isReadOnly()) {
							pageTable[vpn].readOnly = true;
						}
					}			
				}
			}
			
			if(!foundVPN) {
				Lib.debug(dbgVM, "zero fill "+faultEntry.vpn+" ppn is "+faultEntry.ppn);

				int paddr = pageSize * pageTable[faultVpn].ppn;
				byte[] arr = new byte[pageSize];
	
				// Caluclate the physical address == offset
				// Array should be the size of a pageSize
	
				byte[] memory = Machine.processor().getMemory();					
				System.arraycopy(arr, 0, memory, paddr, pageSize);
			}
		}
		
		faultEntry.valid = true;

	}

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
