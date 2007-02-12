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
package de.schlund.pfixxml;

/**
 * @author mleidig@schlund.de
 */
public class FactoryInitException extends Exception {

    private String factoryName;
    
    public FactoryInitException(String factoryName,Throwable cause) {
        super(createMessage(factoryName,cause),cause);
        this.factoryName=factoryName;
    }
    
    private static String createMessage(String factory,Throwable cause) {
        StringBuilder sb=new StringBuilder();
        sb.append("Factory initialization failed [");
        sb.append("Factory: ");
        sb.append(factory);
        sb.append(", Cause: ");
        sb.append(cause);
        sb.append("]");
        return sb.toString();
    }
   
    public String getFactoryName() {
        return factoryName;
    }
    
    public FactoryInitException copy() {
        return new FactoryInitException(factoryName,getCause());
    }
   
}
