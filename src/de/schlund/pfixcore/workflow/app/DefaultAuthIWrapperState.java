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

package de.schlund.pfixcore.workflow.app;

import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.util.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.loader.*;
import de.schlund.util.statuscodes.*;
import java.util.*;

/**
 * DefaultAuthIWrapperState.java
 *
 *
 * Created: Wed Dec 07 19:23:49 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class DefaultAuthIWrapperState extends StateImpl {
    private static final String PROP_AUTHIFACE = "interface";
    private static final String PROP_AUXIFACE  = "auxinterface";
    
    /**
     * This returns alwys true. I don't know why you'd want to overwrite
     * this with a different behaviour.
     *
     * @param context a <code>Context</code> value
     * @param req a <code>HttpServletRequest</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.State#isAccessible(Context, PfixServletRequest) 
     */
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return true;
    }

    /**
     * In this method, we decide if the Session is authenticated or not. If the authentication
     * is OK, we return <b>null</b>. If the session is not authenticated, we want to return a
     * page to the user to query for authentication information.
     *
     * @param context a <code>Context</code> value
     * @param req a <code>HttpServletRequest</code> value
     * @return a <code>SPDocument</code> value
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context, PfixServletRequest) 
     */
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {

        Properties     props   = context.getPropertiesForCurrentPageRequest();
        ResultDocument resdoc  = new ResultDocument();
        ResultForm     resform = resdoc.createResultForm();
        ArrayList      aux     = new ArrayList();
        RequestData    rdata   = (RequestData) new RequestDataImpl(context, preq);
        HashMap        authwrp = PropertiesUtils.selectProperties(props, PROP_AUTHIFACE);
        TreeMap        auxwrp  = PropertiesUtils.selectPropertiesSorted(props, PROP_AUXIFACE);

        if (authwrp == null || authwrp.size() != 1) {
            if(authwrp == null) {
                CAT.error("authwrp == null");
            } else {
                StringBuffer msg = new StringBuffer();
                msg.append("\nauthwrp.size = ").append(authwrp.size()).append("\n");
                Set keys = authwrp.keySet();
                for(Iterator iter=keys.iterator(); iter.hasNext();) {
                    Object key = iter.next();
                    msg.append("key="+iter.next()+" value= "+authwrp.get(key)).append("\n");
                }
                CAT.error(msg);
            }
            throw new XMLException("ERROR: Need exactly one interface definition for authpage!");
        }

        String authprefix  = (String) authwrp.keySet().iterator().next();
        String authwrapper = (String) authwrp.get(authprefix);
        if (authwrapper == null || authwrapper.equals("")) {
            throw new XMLException("No interface for prefix " + authprefix);
        }

        if(CAT.isDebugEnabled()) {
            CAT.debug("======> Interface for authentication: " + authprefix + " => " + authwrapper);
        }
        Class thewrapper=null;
        AppLoader appLoader=AppLoader.getInstance();
        if(appLoader.isEnabled()) {
            thewrapper=appLoader.loadClass(authwrapper);
        } else {
            thewrapper=Class.forName(authwrapper);
        }
        IWrapper user        = (IWrapper) thewrapper.newInstance();
        user.init(authprefix);
        IHandler userhandler = user.gimmeIHandler();

        if (auxwrp != null) {
            for (Iterator i = auxwrp.keySet().iterator(); i.hasNext(); ) {
                String sorted_prefix = (String) i.next();
                String prefix        = sorted_prefix.substring(sorted_prefix.indexOf(".") + 1);
                String iface         = (String) auxwrp.get(sorted_prefix);
                if (iface == null || iface.equals("")) {
                    throw new XMLException("No interface for prefix " + prefix);
                }
                Class auxwrapper=null;
                if(appLoader.isEnabled()) {
                    auxwrapper=appLoader.loadClass(iface);
                } else {
                    auxwrapper=Class.forName(iface);
                }
                IWrapper wrapper    = (IWrapper) auxwrapper.newInstance();
                wrapper.init(prefix);
                aux.add(wrapper);
            }
        }
        

        // Two cases: we are actively submitting data, or not.
        // If we are, we always try to authenticate against supplied user data.
        if (isSubmitAuthTrigger(context, preq)) {
            if(CAT.isDebugEnabled()) {
                CAT.debug("================> Handling AUTHDATA SUBMIT");
            }
            user.load(rdata);
            if (user.errorHappened()) { // during loading of the wrapper...
                userhandler.retrieveCurrentStatus(context, user);
                if(CAT.isDebugEnabled()) {
                    CAT.debug("================> Error during loading of wrapper data");
                }
                // Try loading the aux interfaces, just to echo the stringvals.
                // so no error handling needs to take place.
                auxEchoData(aux, rdata, resform);
                userInsertErrors(context.getProperties(), user, resform);
                return resdoc;
            } else {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("================> Calling handleSubmittedData on " + userhandler.getClass().getName());
                }
                userhandler.handleSubmittedData(context, user);
                if (user.errorHappened()) { // during trying to authenticate
                    userhandler.retrieveCurrentStatus(context, user);
                    // Try loading the aux interfaces, just to echo the stringvals.
                    // so no error handling needs to take place.
                    auxEchoData(aux, rdata, resform);
                    userInsertErrors(context.getProperties(), user, resform);
                    return resdoc;
                } else {
                    // Try loading the aux interfaces, and call
                    // their handlers if no error happened.
                    auxLoadData(aux, context, rdata);
                    resdoc.setSPDocument(null);
                    return resdoc;
                }
            }
        } else { // No data is actually submitted
            if(CAT.isDebugEnabled()) {
                CAT.debug("================> Checking AUTHDATA\n" +
                    "================> Userhandler: " + userhandler.getClass().getName());
            }
            if (userhandler.needsData(context)) { // Not authenticated
                userhandler.retrieveCurrentStatus(context, user);
                // Try loading the aux interfaces, just to echo the stringvals.
                // so no error handling needs to take place.
                auxEchoData(aux, rdata, resform);
                userInsertErrors(context.getProperties(), user, resform);
                return resdoc;
            } else { // OK, we are already authenticated. So we still want to try to handle the aux data.
                // Try loading the aux interfaces, and call
                // their handlers if no error happened.
                auxLoadData(aux, context, rdata);
                resdoc.setSPDocument(null);
                return resdoc;
            }
        }
    }

    private void userInsertErrors(Properties props, IWrapper user, ResultForm resform) {
        IWrapperParamInfo[] pinfos = user.gimmeAllParamInfos();
        String              prefix = user.gimmePrefix(); 
        for (int i = 0; i < pinfos.length; i++) {
            IWrapperParamInfo pinfo  = pinfos[i];
            StatusCode[]      scodes = pinfo.getStatusCodes();
            String            name   = pinfo.getName();
            String[]          value  = pinfo.getStringValue();
            if (value != null) {
                for (int j = 0; j < value.length; j++) {
                    resform.addValue(prefix + "." + name, value[j]);
                }
            }
            if (scodes != null) {
                for (int j = 0; j < scodes.length; j++) {
                    resform.addStatusCode(props, scodes[j], prefix + "." + name);
                }
            }
        }
    }

    
    private void auxEchoData(ArrayList aux, RequestData rdata, ResultForm resform) throws Exception {
        for (Iterator i = aux.iterator(); i.hasNext(); ) {
            IWrapper tmp = (IWrapper) i.next();
            tmp.load(rdata);
            IWrapperParamInfo[] params = tmp.gimmeAllParamInfos();
            String              prefix = tmp.gimmePrefix();
            for (int j = 0; j < params.length; j++) {
                IWrapperParamInfo par  = params[j];
                String[]          sarr = par.getStringValue();
                String            val  = "";
                if (sarr != null && sarr.length > 0) {
                    val = sarr[0];
                }
                // put the stringvals into hidden variables,
                // so the next submit will supply them again.
                resform.addHiddenValue(prefix + "." + par.getName(), val);
            }
        }
        for (Iterator i = rdata.getParameterNames(); i.hasNext(); ) {
            String name = (String) i.next();
            if (name.equals(AbstractXMLServer.PARAM_ANCHOR)) {
                RequestParam[] vals = rdata.getParameters(name);
                for (int j = 0; j < vals.length; j++) {
                    resform.addHiddenValue(name, vals[j].getValue());
                }
            }
        }
    }
    
    private void auxLoadData(ArrayList aux, Context context, RequestData rdata) throws Exception {
        for (Iterator i = aux.iterator(); i.hasNext(); ) {
            IWrapper tmp = (IWrapper) i.next();
            IHandler hnd = tmp.gimmeIHandler();
            tmp.load(rdata);
            if (!tmp.errorHappened()) {
                hnd.handleSubmittedData(context, tmp);
            }
            // No error handling here!! if it didn't work --- bad luck.
            // We still return null, as the authentication is OK
        }
    }
    
}
