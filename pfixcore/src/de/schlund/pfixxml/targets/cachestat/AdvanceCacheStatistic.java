/*
 * Created on 13.10.2003
 *
 */
package de.schlund.pfixxml.targets.cachestat;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Category;

/**
 * Provides a queue with QUEUE_SIZE entries which
 * are of type CacheHitMissPair. It provides a 
 * pointer which points to the current element. The
 * current element is used for recording cache hits and missses.
 * After NEXT_TICK (in ms) the next entry in the queue is selected(
 * if there was an old entry, it will be overridden). 
 * 
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */

public class AdvanceCacheStatistic {
    private CacheHitMissPair[] statQueue;
    private int index = 0;
    private int queueSize = 0;
    private static Category CAT = Category.getInstance(AdvanceCacheStatistic.class.getName());

    public AdvanceCacheStatistic(Timer timer, int queuesize, int queueticks) {
        if(timer == null) {
            throw new IllegalArgumentException("A NP passed as timer is not valid here.");
        }
        if(queuesize < 1) {
            queuesize = 60;
            //throw new IllegalArgumentException("queuesize must not be < 1");
        }
        if(queueticks < 1) {
            queueticks = 60000;
            //throw new IllegalArgumentException("queueticks must not be < 1");
        }
        
        queueSize = queuesize;
        statQueue = new CacheHitMissPair[queueSize];
        for(int i=0; i<queueSize; i++) {
            statQueue[i] = new CacheHitMissPair();
        }
        timer.schedule(new AdvanceTask(), queueticks, queueticks);    
    }

    public long getHits() {
        long hits = 0;
        for(int i=0; i<statQueue.length; i++) {
            CacheHitMissPair pair = statQueue[i];
            if(pair != null) {
                hits += pair.getHits();
            }
        }
        if(CAT.isDebugEnabled()) CAT.debug(this.hashCode()+"Hits: "+hits);
        return hits;
    }

    public long getMisses() {
        long misses = 0;
        for(int i=0; i<statQueue.length; i++) {
            CacheHitMissPair pair = statQueue[i];
            if(pair != null) {
                misses += pair.getMisses();
            }
        }
        if(CAT.isDebugEnabled()) CAT.debug(this.hashCode()+" Misses: "+misses);
        return misses;     
    }

    
    synchronized void registerHit() {
        statQueue[index].increaseHits();
    }

    
    synchronized void registerMiss() {
        statQueue[index].increaseMisses();
    }


    synchronized void advance() {
        if(index == (queueSize - 1)) {
            index = 0;
        } else {
            index++;
        }
        if(CAT.isDebugEnabled()) CAT.debug("--->Reset CacheHitMissPair at:"+index);
        statQueue[index].resetHits();
        statQueue[index].resetMisses();
    }

    /**
     * Timer task which is executed all NEXT_TICK ms.
     */
    class AdvanceTask extends TimerTask {

        public void run() {
            advance();
        }
    }

}







