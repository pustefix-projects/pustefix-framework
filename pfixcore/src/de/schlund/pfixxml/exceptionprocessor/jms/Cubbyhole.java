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
package de.schlund.pfixxml.exceptionprocessor.jms;

import org.apache.log4j.Category;

/**
 * Class to decouple the exception-throwing tomcat-threads from the thread which 
 * handles the exception. It is a simple producer/consumer-problem where the
 * tomcat threads are the producers and the exception-handling thread is the consumer.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class Cubbyhole {



    private Object[] allElements = null; // the elements
    private int putPtr = 0; // circular indices
    private int takePtr = 0;
    private int usedSlots = 0; // length
    private static Category CAT = Category.getInstance(Cubbyhole.class.getName());
    /* Wait until there is enough place in the cubbyhole */ 
    private int TIMEOUT = 2000; //ms
    private boolean took = false;

    /**
     * Create a new cubbyhole object with the given capacity.
     * @exception IllegalArgumentException if capacity less or equal to zero.
     */
    Cubbyhole(int capacity) throws IllegalArgumentException {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        allElements = new Object[capacity];
    }


    /**
     * Place item in the cubbyhole, possibly waiting indefinitly until
     * it can be accepted. This implementation blocks on puts upon
     * reaching capacity.
     * @param x the element to be inserted. Should be non-null. 
     * @exception InterruptedException if the current thread has been interrupted.
     */
    synchronized void put(Object x) throws InterruptedException {
        while (usedSlots == allElements.length) {
            if(CAT.isDebugEnabled())
                CAT.debug("PUT: Cubbyhole was full. Calling 'wait()'.");      
            //cubbyhole is full->wait
            took = false;
            wait(TIMEOUT);
            if(!took) {
            	CAT.error("wait call exceeded timeout");
            	return;
            }
        }
        allElements[putPtr] = x;
        /*if (CAT.isDebugEnabled()) {
            StringBuffer msg = new StringBuffer(100);
            msg.append("PUT(").append(usedSlots).append(",").append(putPtr)
                .append("):").append(Thread.currentThread().getName()).append("--> ")
                .append(((ExceptionDataValue) x).getThrowable().toString());
            CAT.debug(msg.toString());
        }*/
        putPtr = (putPtr + 1) % allElements.length;
        if (usedSlots++ == 0) {
            if(CAT.isDebugEnabled()) 
                CAT.debug("PUT: Cubbyhole was empty. Calling 'notifyAll()'.");
            //cubbyhole was empty, wakeup all takers
            notifyAll();
        }
    }

    /**
     * Return and remove an item from the cubbyhole, possibly waiting 
     * indefinitly until such an item exists.
     * @return the object taken from the cubbyhole.
     * @exception InterruptedException if the current thread has been interrupted.
     */
    synchronized Object take() throws InterruptedException {
        while (usedSlots == 0) {
            if(CAT.isDebugEnabled())
                CAT.debug("TAKE: Cubbyhole was empty. Calling 'wait()'.");      
            //cubbyhole is empty->wait
            wait();
        }
        Object x = allElements[takePtr];
        /*if (CAT.isDebugEnabled()) {
            StringBuffer msg = new StringBuffer(100);
            msg.append("TAKE(").append(usedSlots).append(",").append(takePtr)
                .append("):").append(Thread.currentThread().getName()).append("-->")
                .append(((ExceptionDataValue) x).getThrowable().toString());
            CAT.debug(msg.toString());
        }*/
        allElements[takePtr] = null;
        takePtr = (takePtr + 1) % allElements.length;
        if (usedSlots-- == allElements.length) {
            if(CAT.isDebugEnabled())
                CAT.debug("TAKE: Cubbyhole was full. Calling 'notifyAll()'.");
            //cubbyhole was full, wakeup all puters
            took = true;
            notifyAll();
        }
        return x;
    }
} //Cubbyhole
