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
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 * RequestDataImpl.java
 *
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

    private static final String DATA_PREFIX = "__DATA";
    private static final String CMDS_PREFIX  = "__CMD";
    
    public RequestDataImpl (Context context, PfixServletRequest preq) {
        page = context.getCurrentPageRequest().getName();
        initData(preq);
    }

    public Iterator getParameterNames() {
        return data.keySet().iterator();
    }

    public Iterator getCommandNames() {
        return cmds.keySet().iterator();
    }

    public RequestParam[] getParameters(String key) {
        ArrayList list = (ArrayList) data.get(key);
        if (list == null) return null;

        return (RequestParam[]) list.toArray(new RequestParam[] {});
    }

    public String[] getCommands(String key) {
        ArrayList list = (ArrayList) cmds.get(key);
        if (list == null) return null;

        return (String[]) list.toArray(new String[] {});
    }



    private void initData(PfixServletRequest preq) {
        RequestParam[] array;
        HashMap        map;
        String         data_prefix = DATA_PREFIX + ":";
        String         cmds_prefix = CMDS_PREFIX + "[" + page + "]:";
        CAT.debug(">>>> Checking for data starting with '" + data_prefix + "'");
        CAT.debug(">>>> Checking for cmds starting with '" + cmds_prefix + "'");

        String[] paramnames = preq.getRequestParamNames();
        for (int i = 0; i < paramnames.length; i++) {
            String name = paramnames[i];

            array = preq.getAllRequestParams(name);
            if (array != null) {
                addData(name, array);
            }

            CAT.debug(" >>> Looking at param '" + name + "'");
            CAT.debug("   > Looking for data encoded in paramname (prefix '" + data_prefix + "')");
            map = parseNameForPrefix(data_prefix, name);
            if (map != null) {
                addData(map);
            }

            CAT.debug("   > Looking for cmds encoded in paramname (prefix '" + cmds_prefix + "')");
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
            CAT.debug("  >> Key is " + key);

            int valuestart = keyend +1 ;
            int valueend   = name.indexOf(":", valuestart);
            if (valueend < 0) {
                CAT.warn("No trailing ':' was found after the value. Ignoring key/value pair.");
                break;
            }
            String value = name.substring(valuestart, valueend);
            CAT.debug("  >> Value is " + value);
            
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
    
    private void addData(String name, RequestParam[] array) {
        ArrayList list = (ArrayList) data.get(name);
        if (list == null) {
            list = new ArrayList();
            data.put(name, list);
        }
        list.addAll(Arrays.asList(array));
    }

    private void addData(HashMap map) {
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String   name  = (String) i.next();
            String[] array = (String[]) ((ArrayList) map.get(name)).toArray(new String[] {});
            RequestParam[] params = new RequestParam[array.length];
            for (int j = 0; j < array.length; j++) {
                params[j] = new SimpleRequestParam(array[j]);
            }
            addData(name, params);
        }
    }

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
