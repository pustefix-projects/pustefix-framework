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

package de.schlund.pfixxml.exceptionhandler;

/**
 * Class to encapsulate the configuration how an exception 
 * will be treated by the exceptionhandler. It maps the 
 * values from the appropriate property file to internal fields.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class ExceptionConfig {

    //~ Instance/static variables ..............................................

    private int burst_        =0;
    private String ldimension_=null;
    private int limit_        =0;
    private String match_     =null;
    private String type_      =null;

    //~ Constructors ...........................................................
    /**
     * Create a new exceptioconfig object with the given type, match, limit (and its
     * dimension) and burst.
     */
    ExceptionConfig(String type, String match, int limit, String ldim, 
                    int burst) {
        this.type_      =type;
        this.match_     =match;
        this.limit_     =limit;
        this.ldimension_=ldim;
        this.burst_     =burst;
    }

    //~ Methods ................................................................

    /**
     * Returns the burst.
     * @return int
     */
    int getBurst() {
        return burst_;
    }

    /**
     * Returns the limit.
     * @return int
     */
    int getLimit() {
        return limit_;
    }

    /**
     * Returns the dimension of the limit.
     * @return String
     */
    String getLimitDimension() {
        return ldimension_;
    }

    /**
     * Returns the match.
     * @return String
     */
    String getMatch() {
        return match_;
    }

    /**
     * Returns the type.
     * @return String
     */
    String getType() {
        return type_;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer(128);
        buf.append("Type ="+type_).append("\n");
        buf.append("Match="+match_).append("\n");
        buf.append("Limit="+limit_+"/"+ldimension_).append("\n");
        buf.append("Burst="+burst_).append("\n");
        return buf.toString();
    }
    
}