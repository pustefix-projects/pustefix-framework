/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.backend.config.EditorProjectInfo;
import org.springframework.beans.factory.DisposableBean;

import de.schlund.pfixcore.lucefix.PfixReadjustment;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetGenerator;

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
public class PustefixTargetUpdateServiceImpl extends Thread implements
        PustefixTargetUpdateService, DisposableBean {
    
    private PfixReadjustment pfixReadjustment;
    
    private ArrayList<Target> lowPriorityQueue;

    private ArrayList<Target> highPriorityQueue;

    private HashSet<TargetGenerator> tgenList;

    private Object lock;

    private boolean firstRunDone;

    private boolean waitingForRefill;

    private boolean isEnabled = false;

    private long startupDelay = 0;

    private long highRunDelay = 250;

    private long firstRunDelay = 250;

    private long nthRunDelay = 1000;

    private long completeRunDelay = 600000;

    private Logger LOG = Logger.getLogger(this.getClass());

    public void enableAutoUpdating(boolean flag) {
        LOG.debug("***** Target Updater currently enabled?: " + isEnabled);
        LOG.debug("***** New value: " + flag);

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
        super(new ThreadGroup("pustefix-target-update"), "target-update");
        this.lowPriorityQueue = new ArrayList<Target>();
        this.highPriorityQueue = new ArrayList<Target>();
        this.lock = new Object();
        this.tgenList = new HashSet<TargetGenerator>();
        this.firstRunDone = false;
        this.waitingForRefill = false;
    }

    public void init() {
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
        start();
    }
    
    public void destroy() {
        getThreadGroup().interrupt();
    }

    public void registerTargetForUpdate(Target target) {
        if (target == null) {
            String msg = "Received null pointer as target!";
            LOG.warn(msg);
            return;
        }
        synchronized (this.lock) {
            LOG.debug("  + HighPrio target " + target.getTargetKey());
            this.highPriorityQueue.add(target);
            this.lock.notifyAll();
        }
    }

    public void registerTargetGeneratorForUpdateLoop(TargetGenerator tgen) {
        synchronized (this.lock) {
            if (!this.tgenList.contains(tgen)) {
                this.tgenList.add(tgen);
                this.lowPriorityQueue.addAll(tgen.getPageTargetTree().getToplevelTargets());
                this.firstRunDone = false;
                this.lock.notifyAll();
            }
        }
    }

    public void run() {
        if (this.startupDelay > 0) {
            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            do {
                long waitTime = this.startupDelay + startTime - currentTime;
                Object waitLock = new Object();
                synchronized (waitLock) {
                    try {
                        waitLock.wait(waitTime);
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
                currentTime = System.currentTimeMillis();
            } while ((currentTime < (startTime + this.startupDelay)) && !isInterrupted());
        }

        while (!isInterrupted()) {
            ArrayList<Target> lowCopy;
            ArrayList<Target> highCopy;
            synchronized (this.lock) {
                lowCopy = new ArrayList<Target>(this.lowPriorityQueue);
                highCopy = new ArrayList<Target>(this.highPriorityQueue);
                this.highPriorityQueue.clear();
            }
            while (!(highCopy.isEmpty() || isInterrupted())) {
                Target target = (Target) highCopy.get(0);
                try {
                    target.getValue();
                } catch (TargetGenerationException e) {
                    LOG.warn("*** Exception generating HP " + target.getTargetKey() + ": " + e.getMessage());
                }
                highCopy.remove(0);
                synchronized (this.lock) {
                    try {
                        this.lock.wait(this.highRunDelay);
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
            }
    
            // Do automatic regeneration only if enabled
            if (this.isEnabled && !isInterrupted()) {
                // System.out.println("*** in low loop ***");
                if (!lowCopy.isEmpty()) {
                    while (!(lowCopy.isEmpty() || isInterrupted())) {
                        Target target = (Target) lowCopy.get(0);
                        boolean needsUpdate;
                        try {
                            needsUpdate = target.needsUpdate();
                        } catch (Exception e) {
                            // Remove target from queue without generating it
                            LOG.warn("*** Exception checking LP " + target.getTargetKey() + ": " + e.getMessage());
                            lowCopy.remove(0);
                            synchronized (this.lock) {
                                this.lowPriorityQueue.remove(0);
                            }
                            continue;
                        }
                        try {
                            if (needsUpdate) {
                                target.getValue();
                            }
                        } catch (TargetGenerationException e) {
                            LOG.warn("*** Exception generating LP " + target.getTargetKey() + ": " + e.getMessage());
                        }
                        lowCopy.remove(0);
                        synchronized (this.lock) {
                            this.lowPriorityQueue.remove(0);
                            if (!this.highPriorityQueue.isEmpty()) {
                                break;
                            }
                            
                            if (needsUpdate) {
                                // If a target has been generated, wait for some time.
                                long delay = nthRunDelay;
                                if (!this.firstRunDone) {
                                    delay = firstRunDelay;
                                }
                                if (delay > 0) {
                                    try {
                                        this.lock.wait(delay);
                                    } catch (InterruptedException e) {
                                        interrupt();
                                    }
                                }
                            }
                        }
                    }
                }
            }
    
            synchronized (this.lock) {
                if (this.isEnabled && !isInterrupted()) {
                    if (this.lowPriorityQueue.isEmpty() && !waitingForRefill) {
                        this.firstRunDone = true;
    
                        // All low priority targets (usually all targets)
                        // have been updated, so trigger regeneration of
                        // search index
                        pfixReadjustment.readjust();
    
                        // Delay refill of low priority queue
                        // in order to keep down system load
                        this.waitingForRefill = true;
                        //TODO: interrupt 
                        Thread refillToolThread = new Thread(getThreadGroup(), "target-update-refill") {
                            public void run() {
                                try {
                                    if (completeRunDelay > 0) {
                                        Thread.sleep(completeRunDelay);
                                    }
                                } catch (InterruptedException e) {
                                    interrupt();
                                }
                                synchronized (lock) {
                                    for (Iterator<TargetGenerator> i = tgenList.iterator(); i.hasNext() && !isInterrupted();) {
                                        TargetGenerator tgen = (TargetGenerator) i.next();
                                        lowPriorityQueue.addAll(tgen.getPageTargetTree().getToplevelTargets());
                                    }
                                    waitingForRefill = false;
                                    lock.notifyAll();
                                }
                            }
                        };
                        refillToolThread.start();
                    }
                }
    
                if (this.highPriorityQueue.isEmpty() && (!this.isEnabled || this.lowPriorityQueue.isEmpty())) {
                    try {
                        this.lock.wait();
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
            }
        } 
    }

    public void setPfixReadjustment(PfixReadjustment pfixReadjustment) {
        this.pfixReadjustment = pfixReadjustment;
    }
    
    public void setEditorProjectInfo(EditorProjectInfo info) {
        this.enableAutoUpdating(info.isEnableTargetUpdateService());
    }
}
