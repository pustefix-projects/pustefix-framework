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

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Category;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

/** not synchronized! **/

public class PartIndex {
    private final static Category  LOG      = Category.getInstance(PartIndex.class.getName());
    private final static PartIndex instance = new PartIndex();
    private final static String    MESSAGES = "partindex.scodefile";
    private final static String    DO_CHECK = "partindex.reloadchanges";
    
    private HashMap    codes = new HashMap();
    private PartFile[] src;
    private boolean    docheck = false;
    
    public static PartIndex getInstance() {
        return instance;
    }
    
    public PartIndex() {}

    public void init(Properties props) throws Exception {
        String check = props.getProperty(DO_CHECK);
        if (check != null && check.equals("true")) {
            docheck = true;
        } else {
            docheck = false;
        }

        HashMap scmap   = PropertiesUtils.selectProperties(props, MESSAGES);
        HashSet scfiles = new HashSet(scmap.values());
        
        src = new PartFile[scfiles.size()];

        int i = 0;
        for (Iterator iter = scfiles.iterator(); iter.hasNext();) {
            src[i++] = new PartFile(PathFactory.getInstance().createPath((String) iter.next()));
        }
        
        addAll();
    }

    //--
    public StatusCode lookup(String name) throws TransformerException {
        if (docheck) {
            addAll();
        }

        return (StatusCode) codes.get(name);
    }
    
    public synchronized void addAll() throws TransformerException {
        for (int i = 0; i < src.length; i++) {
            PartFile onesrc = src[i];
            if (onesrc.wantUpdate()) {
                onesrc.updateMtime();
                addAll(onesrc);
            }
        }
    }
    
    public synchronized void addAll(PartFile pfile) throws TransformerException {
        HashMap  tmp    = new HashMap();
        List     lst;
        Iterator iter;
        Attr     attr;
        String   key;
        Path     scpath = pfile.getPath();
        Document doc    = Xml.parse(pfile.getSCFile());
        
        LOG.debug("**** Loading StatusCode file " + scpath.getRelative() + " ****");
        lst = XPath.select(doc, "/include_parts/part[product/@name='default']/@name");
        iter = lst.iterator();
        while (iter.hasNext()) {
            attr = (Attr) iter.next();
            key = attr.getValue();
            if (!tmp.containsKey(key)) {
                tmp.put(key, new StatusCode(key, scpath));
            }
        }
        
        codes.putAll(tmp);

        LOG.info(tmp.size() + " StatusCodes loaded");
    }

    private class PartFile {
        Path path;
        File scfile;
        long mtime = -1;
        
        public PartFile(Path path) {
            this.path = path;
            scfile = path.resolve();
        }

        boolean wantUpdate() {
            return (scfile.lastModified() > mtime);
        }

        void updateMtime() {
            mtime = scfile.lastModified();
        }
        
        Path getPath() {
            return path;
        }

        File getSCFile() {
            return scfile;
        }
        
    }
}
