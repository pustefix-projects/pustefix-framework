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

package de.schlund.pfixcore.editor;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.FactoryInit;
import de.schlund.pfixcore.util.Meminfo;
import java.util.*;
import org.apache.log4j.*;

/**
 * EditorPageUpdater.java
 *
 *
 * Created: Mon Dec  3 14:09:18 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditorPageUpdater implements FactoryInit, Runnable {
    private static EditorPageUpdater instance = new EditorPageUpdater();
    private static Category          CAT      = Category.getInstance(EditorPageUpdater.class.getName());
    private        HashSet           targets  = new HashSet();
    private        Object            WAITLOCK = new Object();
    
    private EditorPageUpdater() {}
    
    public static EditorPageUpdater getInstance() {
        return instance;
    }
    
    public void init(Properties props) throws Exception {
        CAT.debug("*** Starting EditorPageUpdater! ***");
        CAT.debug("*** ...done                     ***");
        Thread thread = new Thread(instance);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public void addTarget(Target target) {
        if (target == null) {
            CAT.warn("\n*********** Got a 'null' target for updating!!! Ignoring. **************");
            return;
        }
        synchronized (WAITLOCK) {
            targets.add(target);
            WAITLOCK.notify();
        }
    }
    
    public void run() {
        HashSet copy;
        for (;;) {
            synchronized (WAITLOCK) {
                copy = (HashSet) targets.clone();
                targets.clear();
            }
            if (!copy.isEmpty()) {
                for (Iterator i = copy.iterator(); i.hasNext(); ) {
                    Target current = (Target) i.next();
                    CAT.debug("*** Updating " + current.getTargetKey() + " ***");
                    try {
                        current.getValue();
                    } catch (Exception e) {
                        CAT.warn("*** CAUTION: Exception on updating of " + current.getTargetKey());
                    }
                }
                Meminfo.print("After complete update run");
            }
            synchronized (WAITLOCK) {
                if (targets.isEmpty()) { 
                    try {
                        WAITLOCK.wait();
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
    
}// EditorPageUpdater
