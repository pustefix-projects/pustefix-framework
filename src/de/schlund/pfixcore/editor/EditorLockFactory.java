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
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * EditorLockFactory.java
 *
 *
 * Created: Di Dec 04 23:45:28 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditorLockFactory {
    private static Category          CAT      = Category.getInstance(EditorLockFactory.class.getName());
    private static EditorLockFactory instance = new EditorLockFactory();
    private        WeakHashMap       locked   = new WeakHashMap();
    
    public static EditorLockFactory getInstance() {
        return instance;
    }

    public boolean isLockedBySession(EditorSessionStatus ess, AuxDependency aux) {
        String id = ess.getEditorSessionId();
        synchronized (locked) {
            AuxDependency tmp = (AuxDependency) locked.get(ess);
            if (tmp == null || tmp != aux) {
                CAT.debug("### " + ess.getUser().getId() + "/" + id
                          + " HAS NO lock on AuxDependency " + describe(aux));
                return false;
            } else {
                CAT.debug("### " + ess.getUser().getId() + "/" + id
                          + " HAS a lock on AuxDependency " + describe(aux));
                return true;
            }
        }
    }

    public boolean getLock(EditorSessionStatus ess, AuxDependency aux) {
        String id = ess.getEditorSessionId();
        synchronized (locked) {
            AuxDependency tmp = (AuxDependency) locked.get(ess);
            if (locked.containsValue(aux)) {
                // aux is locked by someone...
                if (aux == tmp) {
                    // OK, it's us who hold the lock :-)
                    CAT.debug("### ALREADY HOLDING lock for " + ess.getUser().getId() + "/" + id
                              + " on AuxDependency " + describe(aux));
                    return true;
                } else {
                    CAT.debug("### COULD NOT get lock for " + ess.getUser().getId() + "/" + id
                              + " on AuxDependency " + describe(aux));
                    return false;
                }
            } else {
                // aux is free to lock anyways
                if (tmp != null) {
                    // IMPORTANT! release the lock we have before before getting a new one.
                    // The whole thing depends on having only _ONE_ lock at a time!
                    CAT.debug("### BEFORE LOCKING: release lock for " + ess.getUser().getId() + "/" + id
                              + " on AuxDependency " + describe(tmp));
                    locked.remove(ess);
                }
                CAT.debug("### getting lock for " + ess.getUser().getId() + "/" + id
                          + " on AuxDependency " + describe(aux));
                locked.put(ess, aux);
                return true;
            }
        }
    }

    public void releaseLock(EditorSessionStatus ess) {
        String id = ess.getEditorSessionId();
        synchronized (locked) {
            if (locked.containsKey(ess)) {
                CAT.debug("### release lock for " + ess.getUser().getId() + "/" + id
                          + " on AuxDependency " + describe((AuxDependency) locked.get(ess)));
                locked.remove(ess);
            } else {
                CAT.debug("### NO lock registered for " + ess.getUser().getId() + "/" + id);
            }
        }
    }

    public AuxDependency getLockedAuxDependency(EditorSessionStatus ess) {
        synchronized (locked) {
            return (AuxDependency) locked.get(ess);
        }
    }

    // Eeeek, possible race because of weak hashmap.
    public EditorSessionStatus[] getAllLockingEditorStatusSessions() {
        synchronized (locked) {
            return (EditorSessionStatus[]) locked.keySet().toArray(new EditorSessionStatus[] {});
        }
    }

    // Eeeek, possible race because of weak hashmap.
    public EditorSessionStatus getLockingEditorSessionStatus(AuxDependency aux) {
        synchronized (locked) {
            for (Iterator i = locked.keySet().iterator(); i.hasNext(); ) {
                EditorSessionStatus key = (EditorSessionStatus) i.next();
                AuxDependency       tmp = (AuxDependency) locked.get(key);
                if (tmp == aux) {
                    return key;
                }
            }
            return null;
        }
    }

    private String describe(AuxDependency aux) {
        DependencyType type = aux.getType();
        if (type == DependencyType.IMAGE || type == DependencyType.FILE) {
            return type.toString() + " " + aux.getPath().getRelative();
        } else {
            return type.toString() + " " + aux.getPath().getRelative() + "@" + aux.getPart() + "@" + aux.getProduct();
        }
    }
}
