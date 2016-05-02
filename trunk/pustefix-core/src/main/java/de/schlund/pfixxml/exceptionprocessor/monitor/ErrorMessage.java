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

package de.schlund.pfixxml.exceptionprocessor.monitor;

import java.beans.ConstructorProperties;
import java.util.Date;

/**
 * Bean collecting some basic information about an exception.
 *
 */
public class ErrorMessage {

    private final Date time;
    private final String server;
    private final String type;
    private final String message;

    @ConstructorProperties({"time", "server", "type", "message"})
    public ErrorMessage(Date time, String server, String type, String message) {
        this.time = time;
        this.server = server;
        this.type = type;
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public String getServer() {
        return server;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
