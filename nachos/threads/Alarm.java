package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;

import java.util.ArrayList;

// import javafx.util.Pair;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */

public class Alarm {

	private class ThreadWakeTime implements Comparable<ThreadWakeTime> {
		KThread thread;
		long wakeTime;

		public ThreadWakeTime(KThread x, long y) {
			this.thread = x;
			this.wakeTime = y;
		}

		public int compareTo(ThreadWakeTime threadWakeTime) {
			if (this.wakeTime > threadWakeTime.wakeTime) {
				return 1;
			} else {
				if (this.wakeTime < threadWakeTime.wakeTime) {
					return -1;
				} else {
					return 0;
				}
			}
		}

		public String toString() {
			return thread.toString() + wakeTime;
		}

		public KThread getKey() {

			return thread;
		}

		public long getValue() {

			return wakeTime;
		}
	}

	// create a waitqueue hashtable to store thread/waketime
	private PriorityQueue<ThreadWakeTime> waitQueue = new PriorityQueue<ThreadWakeTime>();

	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});

	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current thread
	 * to yield, forcing a context switch if there is another thread that should be
	 * run.
	 */
	public void timerInterrupt() {
		boolean status = Machine.interrupt().disable();

		for (int i = 0; i < waitQueue.size(); i++) {
			if (!waitQueue.isEmpty() && Machine.timer().getTime() >= waitQueue.peek().getValue()) {
				ThreadWakeTime currentThreadWakeTime = waitQueue.poll();
				KThread currentThread = currentThreadWakeTime.getKey();
				if (currentThread != null) {
					currentThreadWakeTime.getKey().ready();
				}
			}
		}
		KThread.yield();

		Machine.interrupt().restore(status);

	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up in
	 * the timer interrupt handler. The thread must be woken up (placed in the
	 * scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		if (x <= 0) {
			return;
		}
		KThread currentThread = KThread.currentThread();

		long wakeTime = Machine.timer().getTime() + x;
		ThreadWakeTime threadWakeTime = new ThreadWakeTime(currentThread, wakeTime);
		boolean status = Machine.interrupt().disable();

		this.waitQueue.add(threadWakeTime);

		KThread.sleep();
		Machine.interrupt().restore(status);

	}

	/**
	 * Cancel any timer set by <i>thread</i>, effectively waking up the thread
	 * immediately (placing it in the scheduler ready set) and returning true. If
	 * <i>thread</i> has no timer set, return false.
	 * 
	 * <p>
	 * 
	 * @param thread the thread whose timer should be cancelled.
	 */
	public boolean cancel(KThread thread) {
		// remove thread from waitqueue and return true
		// if thread does not exist in alarm class return false
		boolean status = Machine.interrupt().disable();

		for (int i = 0; i < waitQueue.size(); i++) {
			ThreadWakeTime currentThreadWakeTime = waitQueue.poll();
			
			if (currentThreadWakeTime.getKey() == thread) {
				// delete
				currentThreadWakeTime.getKey().ready();
				Machine.interrupt().restore(status);

				return true;
			}
		}
		Machine.interrupt().restore(status);

		return false;

	}

	/**
	 * test
	 */
	public static void alarmTest1() {
		int durations[] = { 1000, 10 * 1000, 100 * 1000 };
		long t0, t1;

		for (int d : durations) {
			t0 = Machine.timer().getTime();
			ThreadedKernel.alarm.waitUntil(d);
			t1 = Machine.timer().getTime();
			System.out.println("alarmTest1: waited for " + (t1 - t0) + " ticks ");
		}
	}

	public static void alarmTest2() {
		System.out.println("start alarmtest2");
		long t0, t1;

		t0 = Machine.timer().getTime();
		ThreadedKernel.alarm.waitUntil(0);
		t1 = Machine.timer().getTime();
		System.out.println("alarmTest1: waited for " + (t1 - t0) + " ticks ");

		System.out.println("end alarmtest2");

	}

	public static void alarmTest3() {
		System.out.println("start alarmtest3");

		long t0, t1;
		KThread child1 = new KThread(new Runnable() {
			long t2, t3;
			public void run() {

				t2 = Machine.timer().getTime();

				ThreadedKernel.alarm.waitUntil(100);

				System.out.println("child1");
				t3 = Machine.timer().getTime();
				System.out.println("alarmTest3: child1 waited for " + (t3 - t2) + " ticks ");

			}
		});

		child1.setName("child1").fork();

		t0 = Machine.timer().getTime();
		ThreadedKernel.alarm.waitUntil(10000);

		t1 = Machine.timer().getTime();

		System.out.println("alarmTest3: parent waited for " + (t1 - t0) + " ticks ");

		System.out.println("end alarmtest3");


	}

	// Implement more test methods here ...

	// Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
	public static void selfTest() {
		alarmTest1();
		alarmTest2();
		alarmTest3();
		// Invoke your other test methods here ...
	}
}
