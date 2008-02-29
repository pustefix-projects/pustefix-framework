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
package de.schlund.pfixxml.util;

import java.io.File;

import de.schlund.pfixxml.resources.FileResource;

/**
 * Similar to java.io.File, but the base directory for relative paths can be 
 * specified.  
 * 
 * @deprecated Use {@link FileResource} instead
 */
public class Path implements Comparable<Path> {
    private static final String SEP = File.separator; 
    
    public static final File ROOT = new File(SEP); // TODO: windows
    public static final File HERE = new File(System.getProperty("user.dir"));
    public static final File USER = new File(System.getProperty("user.home"));

    public static Path create(String relative) {
        return create(HERE, relative);
    }
    
    public static Path create(File base, String relative) {
        return new Path(base, relative);
    }

    public static String getRelativeString(File base, String absolute) {
        String prefix = base.getAbsolutePath() + SEP;
        if (absolute.startsWith(prefix)) {
            return absolute.substring(prefix.length());
        } else {
            return null;
        }
    }
    
    //--
    
    /** starts and ends with SEP */
    private final File base;
    
    /** never starts with SEP, may be "" */
    private final String relative;

    /** use one of the create methods ... */
    private Path(File base, String relative) {
        if (relative.startsWith(SEP)) {
            throw new IllegalArgumentException("relative is absolute: " + relative);
        }
        if (!base.isAbsolute()) {
            throw new IllegalArgumentException("relative base: " + base);
        }
        this.base = base;
        this.relative = relative;
    }

    public File getBase() {
        return base;
    }
    
    public String getRelative() {
        return relative;
    }

    public File resolve() {
        if ("".equals(relative)) {
            return base;
        } else {
            return new File(base.getPath(), relative);
        }
    }

    /**
     * TODO: does not consider base ...
     * @return null if no directory 
     * */
    public String getDir() {
        int idx;
        
        idx = relative.lastIndexOf(SEP);
        return (idx == -1)? null : relative.substring(0, idx);
    }
    
    public String getName() {
        return relative.substring(relative.lastIndexOf(SEP) + 1);
    }
    
    public String getSuffix() {
        int idx;
        
        idx = relative.lastIndexOf(".");
        return (idx == -1)? "" : relative.substring(idx);
    }
    
    //--
    
    /** TODO: does not consider base */
    public boolean equals(Object obj) {
        if (obj instanceof Path) {
            return ((Path) obj).relative.equals(relative);
        } else {
            return false;
        }
    }
    
    /** TODO: does not consider base */
    public int compareTo(Path obj) {
        // TODO: base might be different?!
        return relative.compareTo(obj.relative);
    }
    
    public int hashCode() {
        return relative.hashCode();
    }
    
    /**
     * Returns a string representation in the form:
     * <code>getClass().getName()+"[base=\""+getBase()+"\"; relative=\""+getRelative()+"\"]"</code>
     * 
     * Use {@link #getBase()} and {@link #getRelative()} to get the path without additional text.
     */
    public String toString() {
        return getClass().getName()+"[base=\""+getBase()+"\"; relative=\""+getRelative()+"\"]";
    }

}
