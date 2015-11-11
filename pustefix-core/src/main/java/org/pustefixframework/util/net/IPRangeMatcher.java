package org.pustefixframework.util.net;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Checks if IPs match the given IP ranges.
 * 
 * By default up to 128 matching IPs are cached for faster performance.
 *
 */
public class IPRangeMatcher {

    private int maxCachedIPs = 128;
    private AtomicInteger matchedIPCount = new AtomicInteger();
    private ConcurrentHashMap<String, Boolean> matchedIPs = new ConcurrentHashMap<String, Boolean>();
    private SortedSet<IPRange> ranges = new TreeSet<IPRange>();

    public IPRangeMatcher(String... ipsOrCidrs) {

        for(String ipOrCidr: ipsOrCidrs) {
            IPRange range = new IPRange(ipOrCidr);
            addRange(range);
        }
    }

    public IPRangeMatcher(int maxCachedIPs, String... ipsOrCidrs) {

        this(ipsOrCidrs);
        this.maxCachedIPs = maxCachedIPs;
    } 

    public boolean matches(String ip) {

        if(matchedIPs.containsKey(ip)) {
            return true;
        }
        for(IPRange range: ranges) {
            if(range.contains(ip)) {
                if(matchedIPCount.get() < maxCachedIPs) {
                    Boolean old = matchedIPs.putIfAbsent(ip, Boolean.TRUE);
                    if(old == null) {
                        matchedIPCount.incrementAndGet();
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void addRange(IPRange range) {
        ranges.add(range);
    }

}
