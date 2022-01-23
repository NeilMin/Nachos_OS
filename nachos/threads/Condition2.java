package nachos.threads;

import java.util.Queue;

import nachos.machine.*;
import java.util.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable. The
	 *                      current thread must hold this lock whenever it uses
	 *                      <tt>sleep()</tt>, <tt>wake()</tt>, or
	 *                      <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically reacquire
	 * the lock before <tt>sleep()</tt> returns.
	 */
	// wait()
	public void sleep() {
		// System.out.println("sleep");
		boolean status = Machine.interrupt().disable();

		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		conditionLock.release();

		KThread currentThread = KThread.currentThread();

		waitQueue.add(currentThread);
		// System.out.println(
		// "currentthread put to sleep " + currentThread.getName() + " size of wait
		// queue " + waitQueue.size());
		currentThread.sleep();

		conditionLock.acquire();
		Machine.interrupt().restore(status);

	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	// signal()
	public void wake() {
		//System.out.println("wake");

		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		boolean status = Machine.interrupt().disable();

		while (waitQueue.size() != 0) {
			KThread nextReadyThread = waitQueue.removeFirst();
			if (nextReadyThread != null) {
				// cancel the timer

				if(nextReadyThread.getStatus() != 2 && !ThreadedKernel.alarm.cancel(nextReadyThread) && nextReadyThread.getStatus() != 1){
					nextReadyThread.ready();
					return;
				}

			}
		}

		Machine.interrupt().restore(status);

	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current thread
	 * must hold the associated lock.
	 */
	// broadcast
	public void wakeAll() {

		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		boolean status = Machine.interrupt().disable();
		while (waitQueue.size() != 0) {
			wake();
		}

		Machine.interrupt().restore(status);

	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until either (1) another thread wakes it using <tt>wake()</tt>, or
	 * (2) the specified <i>timeout</i> elapses. The current thread must hold the
	 * associated lock. The thread will automatically reacquire the lock before
	 * <tt>sleep()</tt> returns.
	 */
	public void sleepFor(long timeout) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		boolean status = Machine.interrupt().disable();
		conditionLock.release();

		// if thread is woken up before its timeout call cancel
		// call waituntil
		waitQueue.add(KThread.currentThread());
		ThreadedKernel.alarm.waitUntil(timeout);
		conditionLock.acquire();

		Machine.interrupt().restore(status);

	}

	private Lock conditionLock;
	// queue to store all threads
	public static LinkedList<KThread> waitQueue = new LinkedList<KThread>();;

	// Place Condition2 testing code in the Condition2 class.

	// Example of the "interlock" pattern where two threads strictly
	// alternate their execution with each other using a condition
	// variable. (Also see the slide showing this pattern at the end
	// of Lecture 6.)

	private static class InterlockTest {
		private static Lock lock;
		private static Condition2 cv;

		private static class Interlocker implements Runnable {
			public void run() {
				lock.acquire();
				for (int i = 0; i < 10; i++) {
					System.out.println(KThread.currentThread().getName());

					cv.wake(); // signal
					cv.sleep(); // wait
				}
				lock.release();
			}
		}

		public InterlockTest() {
			lock = new Lock();
			cv = new Condition2(lock);

			KThread ping = new KThread(new Interlocker());
			ping.setName("ping");
			KThread pong = new KThread(new Interlocker());
			pong.setName("pong");

			ping.fork();
			pong.fork();

			// We need to wait for ping to finish, and the proper way
			// to do so is to join on ping. (Note that, when ping is
			// done, pong is sleeping on the condition variable; if we
			// were also to join on pong, we would block forever.)
			// For this to work, join must be implemented. If you
			// have not implemented join yet, then comment out the
			// call to join and instead uncomment the loop with
			// yields; the loop has the same effect, but is a kludgy
			// way to do it.
			ping.join();
			// for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
		}
	}
	// Place Condition2 test code inside of the Condition2 class.

	// Test programs should have exactly the same behavior with the
	// Condition and Condition2 classes. You can first try a test with
	// Condition, which is already provided for you, and then try it
	// with Condition2, which you are implementing, and compare their
	// behavior.

	// Do not use this test program as your first Condition2 test.
	// First test it with more basic test programs to verify specific
	// functionality.

	public static void cvTest5() {
		final Lock lock = new Lock();
		// final Condition empty = new Condition(lock);
		final Condition2 empty = new Condition2(lock);
		final LinkedList<Integer> list = new LinkedList<>();

		KThread consumer = new KThread(new Runnable() {
			public void run() {
				lock.acquire();
				while (list.isEmpty()) {
					empty.sleep();
				}
				Lib.assertTrue(list.size() == 5, "List should have 5 values.");
				while (!list.isEmpty()) {
					// context swith for the fun of it
					KThread.currentThread().yield();
					System.out.println("Removed " + list.removeFirst());
				}
				lock.release();
			}
		});

		KThread producer = new KThread(new Runnable() {
			public void run() {
				lock.acquire();
				for (int i = 0; i < 5; i++) {
					list.add(i);
					System.out.println("Added " + i);
					// context swith for the fun of it
					KThread.currentThread().yield();
				}
				empty.wake();
				lock.release();
			}
		});

		consumer.setName("Consumer");
		producer.setName("Producer");
		consumer.fork();
		producer.fork();

		// // We need to wait for the consumer and producer to finish,
		// // and the proper way to do so is to join on them. For this
		// // to work, join must be implemented. If you have not
		// // implemented join yet, then comment out the calls to join
		// // and instead uncomment the loop with yield; the loop has the
		// // same effect, but is a kludgy way to do it.
		consumer.join();
		producer.join();
		// for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
	}

	// Invoke Condition2.selfTest() from ThreadedKernel.selfTest()
	private static void sleepForTest1() {
		Lock lock = new Lock();
		Condition2 cv = new Condition2(lock);

		lock.acquire();
		long t0 = Machine.timer().getTime();
		System.out.println(KThread.currentThread().getName() + " sleeping");
		// no other thread will wake us up, so we should time out
		cv.sleepFor(2000);
		cv.wakeAll();
		KThread.yield();
		long t1 = Machine.timer().getTime();
		System.out.println(KThread.currentThread().getName() + " woke up, slept for " + (t1 - t0) + " ticks");
		lock.release();
		KThread.yield();

	}

	// Invoke Condition2.selfTest() from ThreadedKernel.selfTest()
	private static void sleepForTest2() {
		System.out.println("sleepfortest2 start");
		Lock lock = new Lock();
		Condition2 cv = new Condition2(lock);
		KThread child = new KThread(new Runnable() {
			public void run() {
				lock.acquire();
				cv.sleepFor(20000);
				lock.release();
			}
		});
		long t0 = Machine.timer().getTime();

		child.setName("child").fork();

		// no other thread will wake us up, so we should time out
		for(int i = 0; i < 5; i++){
			System.out.println("child "+child.getName());
			KThread.yield();
		}
		lock.acquire();
		cv.wake();
		long t1 = Machine.timer().getTime();
		System.out.println(KThread.currentThread().getName() + " woke up, slept for " + (t1 - t0) + " ticks");
		System.out.println("sleepfortest2 end");
		lock.release();

	}

	private static void waketest1() {
		System.out.println("waketest1 start");
		Lock lock = new Lock();
		Condition2 cv = new Condition2(lock);
		KThread child = new KThread(new Runnable() {
			public void run() {
				lock.acquire();
				cv.sleep();
				System.out.println("wakedup child");
				lock.release();
			}
		});
		Lock lock1 = new Lock();
		Condition2 cv1 = new Condition2(lock1);
		KThread child1 = new KThread(new Runnable() {
			public void run() {
				lock1.acquire();
				cv1.sleep();
				System.out.println("wakedup child1");
				lock1.release();
			}
		});
		long t0 = Machine.timer().getTime();


		child.setName("child").fork();
		child1.setName("child1").fork();


		// no other thread will wake us up, so we should time out
		for(int i = 0; i < 5; i++){
			// System.out.println("child "+child.getName());
			KThread.yield();
		}
		lock.acquire();
		cv.wake();
		lock.release();
		for(int i = 0; i < 5; i++){
			// System.out.println("child "+child.getName());
			// System.out.println("status is "+KThread.currentThread().getStatus());
			KThread.yield();
		}
		// long t1 = Machine.timer().getTime();
		// System.out.println(KThread.currentThread().getName() + " woke up, slept for " + (t1 - t0) + " ticks");
		// System.out.println("waketest1 end");

	}

	private static void waketest2() {
		System.out.println("waketest2 start");
		Lock lock = new Lock();
		Condition2 cv = new Condition2(lock);
		KThread child = new KThread(new Runnable() {
			public void run() {
				System.out.println("waketest");
			}
		});
		
		long t0 = Machine.timer().getTime();

		child.setName("child").fork();

		// no other thread will wake us up, so we should time out
		for(int i = 0; i < 5; i++){
			System.out.println("child "+child.getName());
			KThread.yield();
		}
		lock.acquire();
		cv.wake();
		long t1 = Machine.timer().getTime();
		System.out.println(KThread.currentThread().getName() + " woke up, slept for " + (t1 - t0) + " ticks");
		System.out.println("waketest2 end");
		lock.release();

	}

	public static void selfTest() {
		// new InterlockTest();
		// cvTest5();
		// sleepForTest1();
		// sleepForTest2();
		// waketest1();
		// waketest2();

	}

}
