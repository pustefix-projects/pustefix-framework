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
package de.schlund.pfixcore.example.webservices;

import javax.jws.WebService;

import org.pustefixframework.webservices.AbstractService;

/**
 * DataImpl.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
@WebService
public class DataImpl extends AbstractService implements Data {
    
    public String exchangeData(String data,int strSize) throws Exception {
        ContextData ctx=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return ctx.exchangeData(data,strSize);
    }
    
    public String[] exchangeDataArray(String[] data,int arrSize,int strSize) throws Exception {
        ContextData ctx=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return ctx.exchangeDataArray(data,arrSize,strSize);
    }

}
