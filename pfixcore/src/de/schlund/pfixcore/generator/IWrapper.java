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

package de.schlund.pfixcore.generator;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;
import de.schlund.pfixxml.*;

/**
 *
 */
     
public interface IWrapper extends Comparable {
    void                init(String prefix) throws Exception;
    void                load(RequestData req) throws Exception;
    // The reason for these to not being called get* is to avoid nameclashes with
    // descendents who may want to use a Parameter called e.g. "Prefix" (which would
    // result in a method getPrefix be generated)
    String              gimmePrefix();
    IHandler            gimmeIHandler();
    boolean             errorHappened();
    IWrapperParamInfo[] gimmeAllParamInfos();
    IWrapperParamInfo[] gimmeAllParamInfosWithErrors();
    void                defineOrder(int order);
}
