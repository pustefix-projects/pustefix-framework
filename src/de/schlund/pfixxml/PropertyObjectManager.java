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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * This class manages shared objects, which are build from properties. 
 * 
 * @author mleidig
 */
public class PropertyObjectManager {

	private static PropertyObjectManager instance=new PropertyObjectManager();
	private HashMap propMaps;
	
	/**Returns PropertyObjectManager instance.*/
	public static PropertyObjectManager getInstance() {
		return instance;
	}
	
	/**Constructor.*/
	PropertyObjectManager() {
		propMaps=new HashMap();
	}
	
	/**Returns PropertyObject according to Properties and Class parameters. If it doesn't already exist, it will be created.*/
	public PropertyObject getPropertyObject(Properties props,String className) throws Exception {
		return getPropertyObject(props,Class.forName(className));
	}
	
	/**Returns PropertyObject according to Properties and Class parameters. If it doesn't already exist, it will be created.*/
	public PropertyObject getPropertyObject(Properties props,Class propObjClass) throws Exception {
		HashMap propObjs=null;
		synchronized (propMaps) {
			propObjs=(HashMap)propMaps.get(props);
			if(propObjs==null) {
				propObjs=new HashMap();
				propMaps.put(props,propObjs);
			}
		}
		synchronized (propObjs) {
			PropertyObject propObj=(PropertyObject)propObjs.get(propObjClass);
			if(propObj==null) {
				propObj=(PropertyObject)propObjClass.newInstance();
				propObj.init(props);
				propObjs.put(propObjClass,propObj);
			}
			return propObj;
		}
	}
	
	/**Removes PropertyObjects for Properties.They are newly created on demand, i.e. as a result of subsequent getPropertyObject calls.*/
	public void resetPropertyObjects(Properties props) {
		synchronized (propMaps) {
			propMaps.remove(props);
		}
	}

}