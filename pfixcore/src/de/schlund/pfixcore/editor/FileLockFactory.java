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

import de.schlund.util.*;
import java.util.*;
import java.io.*;

/**
 *
 *
 */
public class FileLockFactory {
    private        HashMap         filelocks = new HashMap();
    private static FileLockFactory instance  = new FileLockFactory();

    private FileLockFactory() {};
    
    public static FileLockFactory getInstance() {
        return instance;
    }
    
    public Object getLockObj(String filename) {
        synchronized (filelocks) {
            if (filelocks.get(filename) == null) {
                filelocks.put(filename, new Object());
            }
            return filelocks.get(filename);
        }
    }

}

