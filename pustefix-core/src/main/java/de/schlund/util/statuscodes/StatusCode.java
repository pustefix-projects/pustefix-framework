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

package de.schlund.util.statuscodes;

import java.net.URI;

import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.Resource;

public class StatusCode {
	
    private String part;
    private DocrootResource path;
    private URI uri;
    
    @Deprecated
    public StatusCode(String part, DocrootResource path) { 
        this.part = part;
        this.path = path;
        this.uri = path.toURI();
    }
    
    public StatusCode(String part, URI uri) {
    	this.part = part;
    	this.uri = uri;
    }
    
    public String getStatusCodeId() { 
        return part;
    }

    @Deprecated
    public Resource getStatusCodePath() {
        return path;
    }
    
    public URI getStatusCodeURI() {
    	return uri;
    }

    @Override
    public String toString() {
        return "StatusCode:" + getStatusCodeId() + "@" + getStatusCodeURI().toASCIIString();
    }

    public static String convertToFieldName(String part) {
        return part.replace('.', '_').replace(':', '_').toUpperCase();
    }

}
