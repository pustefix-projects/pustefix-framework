/*
 * Created on 10.12.2003
 *
 */
package de.schlund.pfixxml.targets.cachestat;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */
/**
 * Helper class.
 */
final class CacheHitMissPair {
    private long hits = 0;
    private long misses = 0;

    void increaseHits() {
        hits++;
    }

    void increaseMisses() {
        misses++;
    }

    long getHits() {
        return hits;
    }

    long getMisses() {
        return misses;
    }

    void resetHits() {
        hits = 0;
    }

    void resetMisses() {
        misses = 0;
    }
}
