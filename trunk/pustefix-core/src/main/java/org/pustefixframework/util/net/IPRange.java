package org.pustefixframework.util.net;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.pustefixframework.util.NetUtils;

/**
 * Representation of an IP range. Supports CIDR notation, first/last IP range or single IP.
 * Can be used to check if a given IP is within an IP range.
 *
 */
public class IPRange implements Comparable<IPRange> {

    private static BigInteger BITS_32= new BigInteger(new byte[] {-1,-1,-1,-1});  
    private static BigInteger BITS_128 = new BigInteger(new byte[] {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1});

    private BigInteger firstIp;
    private BigInteger lastIp;

    /**
     * Create IP range from CIDR notation or single IP.
     */
    public IPRange(String ipOrCidr) {

        if(ipOrCidr == null) {
            throw new IllegalArgumentException("IP/CIDR value must not be null");
        }
        int ind = ipOrCidr.indexOf('/');
        String ip = ipOrCidr;
        int bits = -1;
        if(ind > -1) {
            ip = ipOrCidr.substring(0, ind);
            bits = Integer.parseInt(ipOrCidr.substring(ind + 1));
        }
        InetAddress adr = getAddress(ip);
        BigInteger ipVal = new BigInteger(adr.getAddress());

        if(bits == -1) {
            firstIp = ipVal;
            lastIp = ipVal;
        } else {
            BigInteger mask;
            if(adr instanceof Inet6Address) {
                mask = BITS_128.shiftLeft(128 - bits);
            } else {
                mask = BITS_32.shiftLeft(32 - bits);
            }
            firstIp = ipVal.and(mask);
            lastIp = firstIp.add(mask.not());
        }
    }

    /**
     * Create IP range from first to last IP (including both)
     */
    public IPRange(String first, String last) {

        firstIp = new BigInteger(getAddress(first).getAddress());
        lastIp = new BigInteger(getAddress(last).getAddress());
        if(lastIp.compareTo(firstIp) < 0) {
            BigInteger tmp = firstIp;
            firstIp = lastIp;
            lastIp = tmp;
        }
    }

    private InetAddress getAddress(String ip) {
        if(ip == null) {
            throw new IllegalArgumentException("IP must not be null");
        }
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("No valid IP value: " + ip);
        }
    }

    /**
     * Return if this IP range contains the given IP.
     */
    public boolean contains(String ip) {

        if(!NetUtils.checkIP(ip)) {
            //no valid IP -> don't process and prevent DNS lookup by InetAddress
            return false;
        }
        BigInteger ipNo = new BigInteger(getAddress(ip).getAddress());  
        return (firstIp.compareTo(ipNo)<=0 && lastIp.compareTo(ipNo)>=0);
    }

    @Override
    public int compareTo(IPRange range) {
        int cmp = firstIp.compareTo(range.firstIp);
        if(cmp == 0) {
            cmp = lastIp.compareTo(range.lastIp);
        }
        return cmp;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IPRange) {
            return ((IPRange)obj).compareTo(this) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (firstIp.toString(16) + ":" + lastIp.toString(16)).hashCode();
    }

}
