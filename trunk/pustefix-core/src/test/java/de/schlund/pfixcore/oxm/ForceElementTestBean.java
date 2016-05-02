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
package de.schlund.pfixcore.oxm;

import java.util.Date;

import de.schlund.pfixcore.beans.Alias;
import de.schlund.pfixcore.oxm.impl.annotation.DateSerializer;
import de.schlund.pfixcore.oxm.impl.annotation.ForceElementSerializer;

/**
 * Simple test bean that tests the 
 * ForceElementSerializer
 *  
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class ForceElementTestBean {
    
    @ForceElementSerializer
    public String foo = "foo";

    @ForceElementSerializer
    @DateSerializer("yyyy-MM-dd HH:mm:ss")
    @Alias("openingDate")
    public Date date = new Date(1204479792269l);
    
    @ForceElementSerializer
    @Alias("baz")
    public String getBar() {
        return "bar";
    }
}