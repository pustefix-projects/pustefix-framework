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

package de.schlund.pfixxml.multipart;

import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.ParameterList;

/**
 *
 *
 */

public class HeaderStruct {

    private static final ParameterList DEF_LIST = new ParameterList();

    private String name = null;
    private String value = null;
    private ParameterList params = null;
    
    public HeaderStruct() {
        params = DEF_LIST;
    } 

    /**
     * Gets the name.
     * @return Returns a String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value.
     * @return Returns a String
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public void resetParams() {
        params = DEF_LIST;
    }

    public void initParams(String val) {
        try {
            if (val != null) {
                params = new ParameterList(val);
            } else {
                params = DEF_LIST;
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            params = DEF_LIST;
        }
    }
    
    public String getParam(String nameP) {
        return params.get(nameP);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("name:   ").append(name).append('\n');
        buf.append("value:  ").append(value).append('\n');
        @SuppressWarnings("unchecked")
        Enumeration enm = params.getNames();
        while (enm.hasMoreElements()) {
            String pName = (String)enm.nextElement();
            buf.append("pname: '" + pName + "' pvalue: '" + params.get(pName) + "'\n");
        }
        return buf.toString();
    }

}
