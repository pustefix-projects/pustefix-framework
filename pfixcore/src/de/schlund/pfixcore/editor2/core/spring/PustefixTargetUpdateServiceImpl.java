/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.lucefix.PfixReadjustment;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;

/**
 * Implementation of PageUpdateService using a Thread started upon construction.
 * This service generates all targets registered by using
 * {@link #registerTargetForInitialUpdate(Target)} in a background loop. After
 * the loop has completed the first time, there is a wait time of one second
 * between the generation of each target. Targets registered by using
 * {@link #registerTargetForUpdate(Target)} are generated with a higher priority
 * and without the wait time, but after being generated once, they are
 * automatically removed from the queue.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixTargetUpdateServiceImpl implements
        PustefixTargetUpdateService, Runnable {
    private ArrayList lowPriorityQueue;

    private ArrayList highPriorityQueue;

    private HashSet targetList;

    private Object lock;

    private boolean firstRunDone;

    private boolean waitingForRefill;

    private boolean isEnabled = false;

    private long startupDelay = 0;

    private long highRunDelay = 250;

    private long firstRunDelay = 250;

    private long nthRunDelay = 1000;

    private long completeRunDelay = 600000;

    public void setEnabled(boolean flag) {

        System.out.println("***** Called from Spring with value: " + flag);
        System.out.println("***** Doing nothing....: ");

        // Make sure sleeping thread is awakened
        // when service is enabled
//         synchronized (this.lock) {
//         	this.lock.notifyAll();
//         }
    }

    public void setEnabledXXX(boolean flag) {

        System.out.println("***** Target Updater currently enabled?: " + isEnabled);
        System.out.println("***** New value: " + flag);

        this.isEnabled = flag;
        
        // Make sure sleeping thread is awakened
        // when service is enabled
        synchronized (this.lock) {
        	this.lock.notifyAll();
        }
    }

    public void setStartupDelay(long delay) {
        this.startupDelay = delay;
    }

    public void setHighRunDelay(long delay) {
        this.highRunDelay = delay;
    }

    public void setFirstRunDelay(long delay) {
        this.firstRunDelay = delay;
    }

    public void setNthRunDelay(long delay) {
        this.nthRunDelay = delay;
    }

    public void setCompleteRunDelay(long delay) {
        this.completeRunDelay = delay;
    }

    public PustefixTargetUpdateServiceImpl() {
        this.lowPriorityQueue = new ArrayList();
        this.highPriorityQueue = new ArrayList();
        this.lock = new Object();
        this.targetList = new HashSet();
        this.firstRunDone = false;
        this.waitingForRefill = false;
    }

    public void init() {
        Thread thread = new Thread(this, "pustefix-target-update");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public void registerTargetForUpdate(Target target) {
        if (target == null) {
            String msg = "Received null pointer as target!";
            Logger.getLogger(this.getClass()).warn(msg);
            return;
        }
        synchronized (this.lock) {
            this.highPriorityQueue.add(target);
            this.lock.notifyAll();
    	}
    }

    public void registerTargetForInitialUpdate(Target target) {
        if (target == null) {
            String msg = "Received null pointer as target!";
            Logger.getLogger(this.getClass()).warn(msg);
            return;
        }
        // System.out.println("*** register for ini update: " + target.getTargetKey() + " ***");
        synchronized (this.lock) {
            if (!this.targetList.contains(target)) {
                this.targetList.add(target);
                this.lowPriorityQueue.add(target);
                this.firstRunDone = false;
                this.lock.notifyAll();
            }
        } 
    }

    public void run() {
        // Wait for delay
        if (this.startupDelay > 0) {
            long startTime = Calendar.getInstance().getTimeInMillis();
            long currentTime = Calendar.getInstance().getTimeInMillis();
            ;
            do {
                long waitTime = this.startupDelay + startTime - currentTime;
                Object waitLock = new Object();
                synchronized (waitLock) {
                    try {
                        waitLock.wait(waitTime);
                    } catch (InterruptedException e) {
                        // Ignore interruption
                    }
                }
                currentTime = Calendar.getInstance().getTimeInMillis();
            } while (currentTime < (startTime + this.startupDelay));
        }

        while (true) {
            // System.out.println("*** In RUN ***");
            ArrayList lowCopy;
            ArrayList highCopy;
            synchronized (this.lock) {
                lowCopy = (ArrayList) this.lowPriorityQueue.clone();
                highCopy = (ArrayList) this.highPriorityQueue.clone();
                this.highPriorityQueue.clear();
                // System.out.println("*** low: " + lowCopy.size() + " high: " + highCopy.size() + " ***");
            }
            while (!highCopy.isEmpty()) {
                Target target = (Target) highCopy.get(0);
                try {
                    // System.out.println("*** high gen: " + target.getTargetKey() + " ***");
                    target.getValue();
                } catch (TargetGenerationException e) {
                    String msg = "Generation of target "
                        + target.getTargetKey() + " failed!";
                    Logger.getLogger(this.getClass()).warn(msg);
                }
                highCopy.remove(0);
                try {
                    this.lock.wait(this.highRunDelay);
                } catch (InterruptedException e) {
                    // Ignore interruption and continue
                }
            }

            // Do automatic regeneration only if enabled
            if (this.isEnabled) {
                // System.out.println("*** in low loop ***");
                while (!lowCopy.isEmpty()) {
                    // System.out.println("*** low is not empty ***");
                    Target target = (Target) lowCopy.get(0);
                    boolean needsUpdate;
                    // System.out.println("*** low: checking target " + target.getTargetKey() + " ***");
                    try {
                        needsUpdate = target.needsUpdate();
                    } catch (Exception e) {
                        // Remove target from queue without generating it
                        lowCopy.remove(0);
                        synchronized (this.lock) {
                            this.lowPriorityQueue.remove(0);
                        }
                        continue;
                    }
                    try {
                        if (needsUpdate) {
                            // System.out.println("*** low: generating target " + target.getTargetKey() + " ***");
                            target.getValue();
                        }
                    } catch (TargetGenerationException e) {
                        String msg = "Generation of target "
                                + target.getTargetKey() + " failed!";
                        Logger.getLogger(this.getClass()).warn(msg);
                    }
                    lowCopy.remove(0);
                    synchronized (this.lock) {
                        this.lowPriorityQueue.remove(0);
                        if (!this.highPriorityQueue.isEmpty()) {
                            break;
                        }
                        if (this.firstRunDone && this.nthRunDelay > 0) {
                            try {
                                // Wait a second to make sure
                                // background generation loop
                                // does not consume to much
                                // CPU time
                                if (needsUpdate) {
                                    // System.out.println("*** low: wating (nth) " + nthRunDelay + " ***");
                                    this.lock.wait(this.nthRunDelay);
                                }
                            } catch (InterruptedException e) {
                                // Ignore interruption and continue
                            }
                        }
                        if (!this.firstRunDone && this.firstRunDelay > 0) {
                            try {
                                // Wait a second to make sure
                                // background generation loop
                                // does not consume to much
                                // CPU time
                                if (needsUpdate) {
                                    // System.out.println("*** low: wating (1st) " + firstRunDelay + " ***");
                                    this.lock.wait(this.firstRunDelay);
                                }
                            } catch (InterruptedException e) {
                                // Ignore interruption and continue
                            }
                        }
                    }
                }
            }

            synchronized (this.lock) {
                if (this.isEnabled) {
                    // System.out.println("*** in low prio after loop ***");
                    if (this.lowPriorityQueue.isEmpty() && !waitingForRefill) {
                        this.firstRunDone = true;

                        // All low priority targets (usually all targets)
                        // have been updates, so trigger regeneration of
                        // search index
                        // System.out.println("*** start index ***");
                        PfixReadjustment.getInstance().readjust();

                        // Delay refill of low priority queue
                        // in order to keep down system load
                        this.waitingForRefill = true;
                        Runnable refillTool = new Runnable() {
                                public void run() {
                                    try {
                                	if (completeRunDelay > 0) {
                                            Thread.sleep(completeRunDelay);
                                	}
                                    } catch (InterruptedException e) {
                                        // Ignore
                                    }
                                    synchronized (lock) {
                                        lowPriorityQueue.addAll(targetList);
                                        waitingForRefill = false;
                                        lock.notifyAll();
                                    }
                                }
                            };
                        Thread toolThread = new Thread(refillTool, "target-update-refill");
                        toolThread.start();
                        
                    }
                }

                if (this.highPriorityQueue.isEmpty()
                    && (!this.isEnabled || this.lowPriorityQueue.isEmpty())) {
                    try {
                        this.lock.wait();
                    } catch (InterruptedException e) {
                        // Ignore exception
                    }
                }
            }
        }
    }
}
