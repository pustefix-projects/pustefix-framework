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
package de.schlund.pfixxml.targets;

import java.io.File;

/**
 * Similar to java.io.File, but the base directory for relative paths can be 
 * specified.  
 */
public class Path implements Comparable {
	private static final String SEP = File.separator; 
	
	public static Path create(String path) {
	    if (path.startsWith(SEP)) {
		    return create(SEP, path.substring(SEP.length()));
	    } else {
	        return create(System.getProperty("user.dir"), path);
	    }
	}

	public static Path create(String base, String relative) {
        if (relative.startsWith(SEP)) {
		    relative = relative.substring(1);
            // TODO: throw new IllegalArgumentException("relative path expected: " + path);
        }
        return new Path(base, relative);
    }

	public static Path createOpt(String base, String relative) {
	    if (relative.length() == 0) {
	        return null;
	    } else {
	        return create(base, relative);
	    }
	}
	
	public static String getRelativeString(String base, String absolute) {
	    if (!base.endsWith(SEP)) {
	        base = base + SEP;
	    }
	    if (absolute.startsWith(base)) {
	        return absolute.substring(base.length());
	    } else {
	        return null;
	    }
	}
	
    //--

	/** starts and ends with SEP */
	private final String base;
	
    /** never starts with SEP, never empty */
    private final String relative;

    /** use one of the create methods ... */
    private Path(String base, String relative) {
        if (relative.length() == 0) {
            throw new IllegalArgumentException("empty relative: " + relative);
        }
        if (relative.startsWith(SEP)) {
            throw new IllegalArgumentException("relative is absolute: " + relative);
        }
        if (!base.startsWith(SEP)) {
            throw new IllegalArgumentException("relative base: " + base);
        }
        if (!base.endsWith(SEP)) {
            base = base + SEP;
        }
        this.base = base;
        this.relative = relative;
    }

    public String getBase() {
        return base;
    }
    
    public String getRelative() {
        return relative;
    }

    public File resolve() {
        return new File(base + relative);
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
        return relative.substring(relative.lastIndexOf("."));
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
    public int compareTo(Object obj) {
        // TODO: base might be different?!
        return relative.compareTo(((Path) obj).relative);
    }

    public int hashCode() {
        return relative.hashCode();
    }

    /** use getRelative to get the path without additional text */
    public String toString() {
        return "path " + getRelative();
    }
}
