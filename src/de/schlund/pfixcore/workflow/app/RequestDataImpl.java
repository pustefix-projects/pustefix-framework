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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Category;

import de.schlund.pfixcore.generator.RequestData;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;

/**
 * Implementation of the RequestData interface.
 * <br/>
 *
 * Created: Thu Nov 22 15:04:30 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class RequestDataImpl implements RequestData {

    private HashMap  data = new HashMap();
    private HashMap  cmds = new HashMap();
    private Category CAT  = Category.getInstance(this.getClass().getName());
    private String   page;

//    private static final String DATA_PREFIX = "__DATA";
    private static final String CMDS_PREFIX  = "__CMD";
    
    public RequestDataImpl (Context context, PfixServletRequest preq) {
        page = context.getCurrentPageRequest().getName();
        initData(preq);
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getParameterNames()
     */
    public Iterator getParameterNames() {
        return data.keySet().iterator();
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getCommandNames()
     */
    public Iterator getCommandNames() {
        return cmds.keySet().iterator();
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getParameters(String)
     */
    public RequestParam[] getParameters(String key) {
        ArrayList list = (ArrayList) data.get(key);
        if (list == null) return null;

        return (RequestParam[]) list.toArray(new RequestParam[] {});
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getCommands(String)
     */
    public String[] getCommands(String key) {
        ArrayList list = (ArrayList) cmds.get(key);
        if (list == null) return null;

        return (String[]) list.toArray(new String[] {});
    }



    private void initData(PfixServletRequest preq) {
        RequestParam[] array;
        HashMap        map;
        // String         data_prefix = DATA_PREFIX + ":";
        String         cmds_prefix = CMDS_PREFIX + "[" + page + "]:";
        // CAT.debug(">>>> Checking for data starting with '" + data_prefix + "'");
        if(CAT.isDebugEnabled()) {
            CAT.debug(">>>> Checking for cmds starting with '" + cmds_prefix + "'");
        }

        String[] paramnames = preq.getRequestParamNames();
        for (int i = 0; i < paramnames.length; i++) {
            String name = paramnames[i];

            array = preq.getAllRequestParams(name);
            if (array != null) {
                ArrayList list = new ArrayList();
                list.addAll(Arrays.asList(array));
                data.put(name, list);
            }

            //CAT.debug(" >>> Looking at param '" + name + "'");
            //CAT.debug("   > Looking for data encoded in paramname (prefix '" + data_prefix + "')");
            //map = parseNameForPrefix(data_prefix, name);
            //if (map != null) {
            //    addData(map);
            //}
            if(CAT.isDebugEnabled()) {
                CAT.debug("   > Looking for cmds encoded in paramname (prefix '" + cmds_prefix + "')");
            }
            map = parseNameForPrefix(cmds_prefix, name);
            if (map != null) {
                addCmds(map);
            }
        }
    }

    private HashMap parseNameForPrefix(String prefix, String name) {
        HashMap retval = new HashMap();
        int     index  = 0;
        while (name.indexOf(prefix, index) >= 0) {
            int keystart = name.indexOf(prefix, index) + prefix.length();
            int keyend   = name.indexOf(":", keystart);
            if (keyend < 0) {
                CAT.warn("No trailing ':' was found after the key. Ignoring key.");
                break;
            }
            String key = name.substring(keystart, keyend);
            if(CAT.isDebugEnabled()) {
                CAT.debug("  >> Key is " + key);
            }
            int valuestart = keyend +1 ;
            int valueend   = name.indexOf(":", valuestart);
            if (valueend < 0) {
                CAT.warn("No trailing ':' was found after the value. Ignoring key/value pair.");
                break;
            }
            String value = name.substring(valuestart, valueend);
            if(CAT.isDebugEnabled()) {
                CAT.debug("  >> Value is " + value);
            }
            ArrayList list = (ArrayList) retval.get(key);
            if (list == null) {
                list = new ArrayList();
                retval.put(key, list);
            }
            list.add(value);
            
            index = valueend;
        }
        return retval;
    }
    
    // private void addData(String name, RequestParam[] array) {
    //     ArrayList list = (ArrayList) data.get(name);
    //     if (list == null) {
    //         list = new ArrayList();
    //         data.put(name, list);
    //     }
    //     list.addAll(Arrays.asList(array));
    // }

    // private void addData(HashMap map) {
    //     for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
    //         String   name  = (String) i.next();
    //         String[] array = (String[]) ((ArrayList) map.get(name)).toArray(new String[] {});
    //         RequestParam[] params = new RequestParam[array.length];
    //         for (int j = 0; j < array.length; j++) {
    //             params[j] = new SimpleRequestParam(array[j]);
    //         }
    //         addData(name, params);
    //     }
    // }

    private void addCmds(String name, String[] array) {
        ArrayList list = (ArrayList) cmds.get(name);
        if (list == null) {
            list = new ArrayList();
            cmds.put(name, list);
        }
        list.addAll(Arrays.asList(array));
    }
    
    private void addCmds(HashMap map) {
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String   name  = (String) i.next();
            String[] array = (String[]) ((ArrayList) map.get(name)).toArray(new String[] {});
            addCmds(name, array);
        }
    }

}
