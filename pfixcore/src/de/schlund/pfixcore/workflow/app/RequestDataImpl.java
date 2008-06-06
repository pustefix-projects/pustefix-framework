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



import java.util.HashMap;
import java.util.Iterator;

import de.schlund.pfixcore.generator.RequestData;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;

/**
 * Implementation of the RequestData interface.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class RequestDataImpl implements RequestData {

    private HashMap<String, RequestParam[]> data = new HashMap<String, RequestParam[]>();
    private HashMap<String, String[]> cmds = new HashMap<String, String[]>();
    private String   page;

    private static final String CMDS_PREFIX = "__CMD";
    private static final String SBMT_PREFIX = "__SBMT:";
    private static final String SYNT_PREFIX = "__SYNT:";
    
    public RequestDataImpl (Context context, PfixServletRequest preq) {
        page = context.getCurrentPageRequest().getRootName();
        initData(preq);
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getParameterNames()
     */
    public Iterator<String> getParameterNames() {
        return data.keySet().iterator();
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getCommandNames()
     */
    public Iterator<String> getCommandNames() {
        return cmds.keySet().iterator();
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getParameters(String)
     */
    public RequestParam[] getParameters(String key) {
        return (RequestParam[]) data.get(key);
    }

    /**
     * @see de.schlund.pfixcore.generator.RequestData#getCommands(String)
     */
    public String[] getCommands(String key) {
        return (String[]) cmds.get(key);
    }

    private void initData(PfixServletRequest preq) {
        String cmds_prefix = CMDS_PREFIX + "[" + page + "]:";

        String[] paramnames = preq.getRequestParamNames();
        for (int i = 0; i < paramnames.length; i++) {
            String name = paramnames[i];

            if (name.startsWith(cmds_prefix)) {
                String         key     = name.substring(cmds_prefix.length());
                RequestParam[] cmdvals = preq.getAllRequestParams(name);
                if (cmdvals != null && cmdvals.length > 0) {
                    String[] cmdstrs = new String[cmdvals.length];
                    for (int j = 0; j < cmdvals.length; j++) {
                        cmdstrs[j] = cmdvals[j].getValue();
                    }
                    cmds.put(key, cmdstrs);
                }
            } else if (name.startsWith(SYNT_PREFIX) || name.startsWith(SBMT_PREFIX) || name.startsWith(CMDS_PREFIX)) {
                // these are parameters that are no valid cmd and no "real" data
            } else {
                RequestParam[] datavals = preq.getAllRequestParams(name);
                if (datavals != null) {
                    data.put(name, datavals);
                }
            }
        }
    }
}
