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

import de.schlund.pfixxml.targets.*;
import org.apache.log4j.*;
import java.lang.*;
import java.io.*;


/**
 *
 *
 */   


public class DependencyTracker {
    private static Category CAT = Category.getInstance(DependencyTracker.class.getName());
    
    public static String log(String type,String path, String part, String product,
                             String parent_href, String parent_part, String parent_product, String TargetGen, String TargetKey) {
        String RetVal = "0";
        try {
            TargetGenerator Generator =
                TargetGeneratorFactory.getInstance().createGenerator(TargetGen);
            DependencyType thetype = DependencyType.getByTag(type);
            Generator.getTarget(TargetKey).getAuxDependencyManager().addDependency(thetype, path, part, product,
                                                                                   parent_href, parent_part, parent_product);
        } catch (Exception e) {
            CAT.error("Error adding Dependency: ",e); 
            RetVal = "1"; 
        }
        
        return RetVal;
    }

}
