/*
 * This file is particulart of PFIXCORE.
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

package de.schlund.util.statuscodes;

import javax.xml.transform.TransformerException;

public class StatusCodeFactory {
    private final static StatusCodeFactory instance = new StatusCodeFactory();

    public static StatusCodeFactory getInstance() {
        return instance;
    }
    
    private final String domain;
    
    public StatusCodeFactory() {
        this(null);
    }

    public StatusCodeFactory(String domain) {
        this.domain = domain;
    }

    private String getPart(String code) {
        String part;
        StatusCode scode;
        
        if (domain == null) { 
            return code;
        } else {
            return domain + "." + code;
        }
    }

    public StatusCode testStatusCode(String code) throws TransformerException {
        return (StatusCode) PartIndex.getInstance().lookup(getPart(code));
    }

    public boolean statusCodeExists(String code) throws TransformerException {
        return (testStatusCode(code) == null ? false : true);
    }
    
    public StatusCode getStatusCode(String code) throws StatusCodeException {
        StatusCode scode;

        try {
            scode = testStatusCode(code);
        } catch (TransformerException e) {
            throw new StatusCodeException("StatusCodeFactory, statusmessages.xml couldn't be parsed");
        }
        if (scode == null) {
            throw new StatusCodeException("StatusCodeFactory, StatusCode [" + getPart(code) + "] not defined");
        }
        return scode;
    }
}
