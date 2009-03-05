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

package de.schlund.pfixcore.example;

import org.apache.log4j.Logger;


/**
 * ContextTShirt.java
 *
 *
 * Created: Thu Oct 18 19:24:37 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public abstract class AbstractCloth {
    private String       size     = null;
    private Integer      color    = null;
    private Integer[]    features = null;
    private final Logger LOG      = Logger.getLogger(this.getClass().getName());

    public Integer getColor() { return color; }

    public Integer[] getFeature() { return features; }

    public String getSize() { return size; }

    public void setColor(Integer color) { this.color = color; }
    
    public void setSize(String size) { this.size = size; }

    public void setFeature(Integer[] features) { this.features = features; }

    public boolean needsData() {
        if (size == null || color == null) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        LOG.debug("Doing " + this.getClass().getName() + "...");
        return "[Size: " + size + "][Color: " + color + "]";
    }

    public void reset() {
        setSize(null);
        setColor(null);
        setFeature(null);
    }

}// AbstractCloth
