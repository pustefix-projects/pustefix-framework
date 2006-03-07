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
 */

package de.schlund.pfixxml.event;

import java.util.EventListener;

/**
 * Listener receiving an event when the configuration of the object this
 * listener is registered to changes.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ConfigurationChangeListener extends EventListener {
    /**
     * Event triggered by object this listener is registered for, when the
     * object changes its configuration
     * 
     * @param event Event containing the source object
     */
    void configurationChanged(ConfigurationChangeEvent event);
}
