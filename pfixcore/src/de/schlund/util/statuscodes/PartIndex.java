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

package de.schlund.util.statuscodes;

import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Category;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

/** not synchronize! **/

public class PartIndex {
    private final static Category  LOG      = Category.getInstance(PartIndex.class.getName());
    private final static PartIndex instance = new PartIndex();
    private final static String    MESSAGES = "common/dyntxt/statusmessages.xml";
    private final static String    DO_CHECK = "partindex.reloadchanges";
    
    private Map       codes;
    private File      src;
    private long      mtime = -1;
    private boolean   docheck = false;
    
    public static PartIndex getInstance() {
        return instance;
    }
    
    private PartIndex() {}

    public void init(Properties props) throws Exception {
        String check = props.getProperty(DO_CHECK);
        if (check != null && check.equals("true")) {
            docheck = true;
        } else {
            docheck = false;
        }
        src = PathFactory.getInstance().createPath(MESSAGES).resolve();
        addAll(src);
    }

    //--
    public StatusCode lookup(String name) throws TransformerException {
        if (src.lastModified() > mtime) {
            addAll(src);
        }

        return (StatusCode) codes.get(name);
    }
    
    synchronized private void addAll(File src) throws TransformerException {
        if (src.lastModified() > mtime) {
            Document doc = Xml.parse(src);
            HashMap  tmp = new HashMap();
            List     lst;
            Iterator iter;
            Attr     attr;
            String   key;
            
            LOG.debug("\n\n**** Loading StatusCode file " + src + " ****\n");
            lst = XPath.select(doc, "/include_parts/part[product/@name='default']/@name");
            iter = lst.iterator();
            while (iter.hasNext()) {
                attr = (Attr) iter.next();
                key = attr.getValue();
                if (!tmp.containsKey(key)) {
                    tmp.put(key, new StatusCode(key));
                }
            }
            codes = tmp;
            mtime = src.lastModified();
            LOG.info(codes.size() + " StatusCodes loaded");
        }
    }
}
