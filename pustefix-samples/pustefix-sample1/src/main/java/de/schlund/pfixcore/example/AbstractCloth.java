/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger LOG      = LoggerFactory.getLogger(this.getClass().getName());

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

    @Override
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
