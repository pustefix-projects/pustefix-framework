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

import org.apache.log4j.Logger;

import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;

/**
 * Implementation of PageUpdateService using a Thread started upon construction.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixTargetUpdateServiceImpl implements
        PustefixTargetUpdateService, Runnable {
    private ArrayList lowPriorityQueue;

    private ArrayList highPriorityQueue;

    private Object lock;

    public PustefixTargetUpdateServiceImpl() {
        this.lowPriorityQueue = new ArrayList();
        this.highPriorityQueue = new ArrayList();
        this.lock = new Object();
        Thread thread = new Thread(this);
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
            this.lock.notify();
        }
    }

    public void registerTargetForInitialUpdate(Target target) {
        if (target == null) {
            String msg = "Received null pointer as target!";
            Logger.getLogger(this.getClass()).warn(msg);
            return;
        }
        synchronized (this.lock) {
            this.lowPriorityQueue.add(target);
            this.lock.notify();
        }
    }

    public void run() {
        // TODO Auto-generated method stub
        while (true) {
            ArrayList lowCopy;
            ArrayList highCopy;
            synchronized (this.lock) {
                lowCopy = (ArrayList) this.lowPriorityQueue.clone();
                highCopy = (ArrayList) this.highPriorityQueue.clone();
                this.highPriorityQueue.clear();
            }
            while (!highCopy.isEmpty()) {
                Target target = (Target) highCopy.get(0);
                try {
                    target.getValue();
                } catch (TargetGenerationException e) {
                    String msg = "Generation of target "
                            + target.getTargetKey() + "failed!";
                    Logger.getLogger(this.getClass()).warn(msg);
                }
                highCopy.remove(0);
                synchronized (this.lock) {

                }
            }
            while (!lowCopy.isEmpty()) {
                Target target = (Target) lowCopy.get(0);
                try {
                    target.getValue();
                } catch (TargetGenerationException e) {
                    String msg = "Generation of target "
                            + target.getTargetKey() + "failed!";
                    Logger.getLogger(this.getClass()).warn(msg);
                }
                lowCopy.remove(0);
                synchronized (this.lock) {
                    this.lowPriorityQueue.remove(0);
                    if (!this.highPriorityQueue.isEmpty()) {
                        break;
                    }
                }
            }
            synchronized (this.lock) {
                if (this.highPriorityQueue.isEmpty()
                        && this.lowPriorityQueue.isEmpty()) {
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
