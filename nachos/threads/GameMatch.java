package nachos.threads;

import nachos.machine.*;

/**
 * A <i>GameMatch</i> groups together player threads of the same ability into
 * fixed-sized groups to play matches with each other. Implement the class
 * <i>GameMatch</i> using <i>Lock</i> and <i>Condition</i> to synchronize player
 * threads into groups.
 */
public class GameMatch {

    /* Three levels of player ability. */

    public static final int abilityBeginner = 1, abilityIntermediate = 2, abilityExpert = 3;

    public static Lock beginnerLock = new Lock();
    public static Condition beginnerCondition = new Condition(beginnerLock);

    public static Lock intermediateLock = new Lock();
    public static Condition intermediateCondition = new Condition(intermediateLock);

    public static Lock expertLock = new Lock();
    public static Condition expertCondition = new Condition(expertLock);

    private int beginnerSize = 0;
    private int intermediateSize = 0;
    private int expertSize = 0;

    // private LinkedList<KThread> beginnerWaitQueue = new LinkedList<KThread>();
    // private LinkedList<KThread> intermediateWaitQueue = new
    // LinkedList<KThread>();
    // private LinkedList<KThread> expertWaitQueue = new LinkedList<KThread>();

    private final int numPlayersInMatch;
    private int numOfMatches = 0;
    private int[] matchArray = new int[1];

    /**
     * Allocate a new GameMatch specifying the number of player threads of the same
     * ability required to form a match. Your implementation may assume this number
     * is always greater than zero.
     */
    public GameMatch(int numPlayersInMatch) {
        this.numPlayersInMatch = numPlayersInMatch;
    }

    /**
     * Wait for the required number of player threads of the same ability to form a
     * game match, and only return when a game match is formed. Many matches may be
     * formed over time, but any one player thread can be assigned to only one
     * match.
     *
     * Returns the match number of the formed match. The first match returned has
     * match number 1, and every subsequent match increments the match number by
     * one, independent of ability. No two matches should have the same match
     * number, match numbers should be strictly monotonically increasing, and there
     * should be no gaps between match numbers.
     * 
     * @param ability should be one of abilityBeginner, abilityIntermediate, or
     *                abilityExpert; return -1 otherwise.
     */
    public int play(int ability) {
        
        int[] currentMatchNumber = new int[1] ;
        switch (ability) {
            case abilityBeginner:
                beginnerLock.acquire();
                beginnerSize++;
                if (beginnerSize == numPlayersInMatch) {
                    beginnerCondition.wakeAll();
                    numOfMatches++;
                    matchArray[0] = numOfMatches;
                    currentMatchNumber[0] = matchArray[0];
                    matchArray = new int[1];
                    matchArray[0] = numOfMatches;
                    beginnerSize = 0;
                } else {
                    currentMatchNumber = matchArray;
                    beginnerCondition.sleep();
                }
                beginnerLock.release();

                break;
            case abilityIntermediate:
                intermediateLock.acquire();
                intermediateSize++;
                if (intermediateSize == numPlayersInMatch) {
                    intermediateCondition.wakeAll();
                    numOfMatches++;
                    matchArray[0] = numOfMatches;
                    currentMatchNumber[0] = matchArray[0];
                    matchArray = new int[1];
                    matchArray[0] = numOfMatches;
                    intermediateSize = 0;
                } else {
                    currentMatchNumber = matchArray;
                    intermediateCondition.sleep();
                }
                intermediateLock.release();

                break;
            case abilityExpert:
                expertLock.acquire();
                expertSize++;
                if (expertSize == numPlayersInMatch) {
                    expertCondition.wakeAll();
                    numOfMatches++;
                    matchArray[0] = numOfMatches;
                    currentMatchNumber[0] = matchArray[0];
                    matchArray = new int[1];
                    matchArray[0] = numOfMatches;
                    expertSize = 0;
                } else {
                    currentMatchNumber = matchArray;
                    expertCondition.sleep();
                }
                expertLock.release();

                break;
            default:
                return -1;

        }
        return currentMatchNumber[0];
    }

    public static void matchTest4() {
        final GameMatch match = new GameMatch(2);

        // Instantiate the threads
        KThread beg1 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg1 matched");
                // beginners should match with a match number of 1
                System.out.println("r is  "+r);
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg1.setName("B1");

        KThread beg2 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg2 matched");
                // beginners should match with a match number of 1
                System.out.println("r is ss "+r);
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg2.setName("B2");

        KThread int1 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityIntermediate);
                Lib.assertNotReached("int1 should not have matched!");
            }
        });
        int1.setName("I1");

        KThread exp1 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityExpert);
                Lib.assertNotReached("exp1 should not have matched!");
            }
        });
        exp1.setName("E1");

        // Run the threads. The beginner threads should successfully
        // form a match, the other threads should not. The outcome
        // should be the same independent of the order in which threads
        // are forked.
        beg1.fork();
        int1.fork();
        exp1.fork();
        beg2.fork();

        // Assume join is not implemented, use yield to allow other
        // threads to run
        for (int i = 0; i < 10; i++) {
            KThread.currentThread().yield();
        }
    }

    public static void matchTest5() {
        System.out.println("start match test5");
        final GameMatch match = new GameMatch(2);

        // Instantiate the threads
        KThread beg1 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg1 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg1.setName("B1");

        KThread beg2 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg2 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg2.setName("B2");

        KThread beg3 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg2 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 2, "expected match number of 2");
            }
        });
        beg3.setName("B3");

        KThread beg4 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg2 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 2, "expected match number of 2");
            }
        });
        beg4.setName("B4");

        // Run the threads. The beginner threads should successfully
        // form a match, the other threads should not. The outcome
        // should be the same independent of the order in which threads
        // are forked.
        beg1.fork();
        beg2.fork();
        beg3.fork();
        beg4.fork();

        // Assume join is not implemented, use yield to allow other
        // threads to run
        for (int i = 0; i < 10; i++) {
            KThread.currentThread().yield();
        }
        System.out.println("end match test5");

    }

    public static void matchTest6() {
        final GameMatch match = new GameMatch(2);

        // Instantiate the threads
        KThread beg1 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(5);
                System.out.println("beg1 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == -1, "expected match number of 1");
            }
        });
        beg1.setName("B1");

        // Run the threads. The beginner threads should successfully
        // form a match, the other threads should not. The outcome
        // should be the same independent of the order in which threads
        // are forked.
        beg1.fork();
       
        // Assume join is not implemented, use yield to allow other
        // threads to run
        for (int i = 0; i < 10; i++) {
            KThread.currentThread().yield();
        }
    }
    public static void matchTest7() {
        final GameMatch match = new GameMatch(2);
        final GameMatch match1 = new GameMatch(2);


        KThread beg1 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg1 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg1.setName("B1");

        KThread beg2 = new KThread(new Runnable() {
            public void run() {
                int r = match1.play(GameMatch.abilityBeginner);
                System.out.println("beg2 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 0, "expected match number of 1");
            }
        });
        beg2.setName("B2");

        KThread beg3 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg3 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg3.setName("B3");


        KThread beg4 = new KThread(new Runnable() {
            public void run() {
                int r = match.play(GameMatch.abilityBeginner);
                System.out.println("beg4 matched");
                // beginners should match with a match number of 1
                Lib.assertTrue(r == 1, "expected match number of 1");
            }
        });
        beg4.setName("B4");
        // Run the threads. The beginner threads should successfully
        // form a match, the other threads should not. The outcome
        // should be the same independent of the order in which threads
        // are forked.
        beg1.fork();
        beg2.fork();
        beg3.fork();
        beg4.fork();
       
        // Assume join is not implemented, use yield to allow other
        // threads to run
        for (int i = 0; i < 10; i++) {
            KThread.currentThread().yield();
        }
    }


    public static void selfTest() {
        //matchTest4();
        //matchTest5();
        //matchTest6();
        //matchTest7();

    }
}