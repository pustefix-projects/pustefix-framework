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
*
*/

package de.schlund.pfixxml.exceptionhandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.DecimalFormat;

import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;


/**
 * Removes all <see>STraceContainerChecker</see> from their <see>InstanceContainerChecker</see> 
 * if they did not match for too long. 
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class STraceCleanupTask extends TimerTask {

    //~ Instance/static variables ..............................................

    private Vector icheckers_=null;
    private PFUtil pfutil_   =null;

    //~ Constructors ...........................................................

    /**
     * Create a new STraceCleanupTask object.
     * @param icheckers a Vector containing all <see>InstanceCheckerContainer</see>.
     */
    STraceCleanupTask(Vector icheckers) {
        this.icheckers_=icheckers;
        pfutil_        =PFUtil.getInstance();
    }

    //~ Methods ................................................................

    /**
     * The overridden method which does all work.
     *  @see java.lang.Runnable#run()
     */
    public void run() {
        pfutil_.debug("STraceCleanupTask started.");
        int num         =0;
        long m          =0l;
        double mem      =0d;
        DecimalFormat df=new DecimalFormat("#0.0000");
        String str      =null;
        Thread.currentThread().setName("STraceCleanupTask-Thread");
        num=cleanUp();
        pfutil_.debug("Cleanup Task removed " + num + " STraceCheckers");
        //we dont want to send a mail if no STraceChecker is removed
        if(num==0) {
            pfutil_.debug("STraceCleanupTask ended.");
            return;
        }
        StringBuffer buf=new StringBuffer();
        String hostname =new String("Unkown");
        try {
            InetAddress host=InetAddress.getLocalHost();
            hostname=host.getHostName();
        } catch(UnknownHostException e) {
            pfutil_.error(e.getClass().getName() + " : " + e.getMessage());
        }
        buf.append("Current machine: " + hostname + "\n");
        m  =Runtime.getRuntime().freeMemory();
        mem=(double) m / (1024 * 1024);
        str=df.format(mem);
        buf.append(" \n Cleanup Task removed " + num + " STraceCheckers\n");
        buf.append("Memory before cleanup (System.getRuntime().gc()): " + 
                   str + "MB\n");
        Runtime.getRuntime().gc();
        m  =Runtime.getRuntime().freeMemory();
        mem=(double) m / (1024 * 1024);
        str=df.format(mem);
        buf.append("Memory after cleanup (System.getRuntime().gc()) : " + 
                   str + "MB\n");
        pfutil_.debug("Cleanup Task: " + buf.toString());
        pfutil_.debug("STraceCleanupTask ended.");
    }

    /**
     * Look at all <see>STraceCheckerContainer</see> in all <see>InstanceCheckerContainer</see>.
     * If an <see>STraceCheckerContainer</see> is obsolete, remove it from the <see>InstanceCheckerContainer</see>.
     * @return the number of removed <see>STraceCheckerContainer</see>.
     */
    private int cleanUp() {
        InstanceCheckerContainer icc=null;
        int num                     =0;
        for(Enumeration enm=icheckers_.elements(); enm.hasMoreElements();) {
            icc=(InstanceCheckerContainer) enm.nextElement();
            int count=icc.removeObsoleteCheckers();
            num+=count;
        }
        return num;
    }
} // STraceCleanupTask