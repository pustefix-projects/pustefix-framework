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

package de.schlund.pfixcore.workflow;

import java.util.*;
import org.apache.log4j.*;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.loader.Reloader;
import de.schlund.pfixxml.loader.StateTransfer;
import de.schlund.pfixxml.PropertyObject;
import de.schlund.pfixxml.PropertyObjectManager;

/**
 * <code>DirectOutputPageMap</code> holds a mapping of PageRequests to DirectOutputStates. It
 * implements the PropertyObject interface and is created and initialized by a PropertyObjectManager. This
 * ensures that the the DirectOutputPageMap will be recreated every time the properties change.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version $Id$
 */
public class DirectOutputPageMap implements PropertyObject,Reloader {
    protected            HashMap  pagemap       = new HashMap();
    public  final static String   CLASSNAMEPROP = "classname";
    private final static Category CAT           = Category.getInstance(DirectOutputPageMap.class.getName());
    
    /**
     * The <code>init</code> method initializes a mapping from {@link PageRequest}s to {@link DirectOutputState}s.
     *
     * @param properties a <code>Properties</code> value
     * @exception Exception if an error occurs
     */
    public void init(Properties properties) throws Exception {

        //Get PageRequestProperties object from PropertyObjectManager 
        PageRequestProperties preqprops = (PageRequestProperties) PropertyObjectManager.getInstance().
            getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageRequestProperties");
        
        PageRequest[] pages = preqprops.getAllDefinedPageRequests();
        
        for (int i = 0; i < pages.length; i++) {
            PageRequest       page      = pages[i];
            Properties        props     = preqprops.getPropertiesForPageRequest(page);
            String            classname = props.getProperty(CLASSNAMEPROP);
            DirectOutputState state     = DirectOutputStateFactory.getInstance().getDirectOutputState(classname);
            if (state == null) {
                CAT.error("***** Skipping page '" + page + "' as it's corresponding class " + classname +
                          "couldn't be initialized by the DirectOutputStateFactory");
            } else {
                pagemap.put(page, state);
            }
        }
        
        AppLoader appLoader=AppLoader.getInstance();
        if(appLoader.isEnabled()) {
            appLoader.addReloader(this);
        }
    }

    /**
     * The <code>getDirectOutputState</code> method returns the DirectOutputState that is
     * associated with the <code>page</code> parameter
     *
     * @param page a <code>PageRequest</code> value
     * @return a <code>DirectOutputState</code> value
     */
    public DirectOutputState getDirectOutputState(PageRequest page) {
	return (DirectOutputState) pagemap.get(page);
    }

    public String toString() {
	String ret = "";
	for (Iterator i = pagemap.keySet().iterator(); i.hasNext(); ) {
	    if (ret.length() > 0) {
		ret += ", ";
	    }
	    PageRequest k = (PageRequest) i.next();
	    ret += k + " -> " + ((DirectOutputState) pagemap.get(k)).getClass().getName();
	}
	return ret;
    }
    
    public void reload() {
        HashMap pageNew=new HashMap();
        Iterator it=pagemap.keySet().iterator();
        while(it.hasNext()) {
            PageRequest page=(PageRequest)it.next();
            DirectOutputState stOld=(DirectOutputState)pagemap.get(page);
            DirectOutputState stNew=(DirectOutputState)StateTransfer.getInstance().transfer(stOld);
            pageNew.put(page,stNew);
        }
        pagemap=pageNew;
    }
    
}
