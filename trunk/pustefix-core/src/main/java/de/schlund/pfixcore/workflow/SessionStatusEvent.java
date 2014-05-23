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

package de.schlund.pfixcore.workflow;

/**
 * Represents an event related to the life-cycle of a session.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SessionStatusEvent {
    /**
     * Type of status change
     */
    public enum Type {
        /**
         * Session is being destroyed
         */
        SESSION_DESTROYED;
    }
    
    Type type;
    
    /**
     * Creates a new event with the specified type
     * @param type status change event
     */
    public SessionStatusEvent(Type type) {
        this.type = type;
    }
    
    /**
     * Returns which type of status change caused this event
     * 
     * @return type of status change
     */
    public Type getType() {
        return this.type;
    }
}
