/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.workflow;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.XsltVersion;


/**
 * @author jtl
 *
 */

public class NavigationFactory {
    private final static Logger LOG = Logger.getLogger(NavigationFactory.class);
    private static HashMap<String, Navigation> navis = new HashMap<String, Navigation>();
    private static NavigationFactory instance = new NavigationFactory();
    
    public static NavigationFactory getInstance() {
        return instance;
    }
    
    public synchronized Navigation getNavigation(String navifilename,XsltVersion xsltVersion) throws Exception {
        FileResource navifile = ResourceUtil.getFileResourceFromDocroot(navifilename);
        return getNavigation(navifile,xsltVersion);
    }
            
    public synchronized Navigation getNavigation(FileResource navifile,XsltVersion xsltVersion) throws NavigationInitializationException {
       
        Navigation navi = null;
        
        navi = navis.get(navifile.toURI().toString());
        
        if (navi == null || navi.needsReload()) {
            LOG.info("***** Creating Navigation object *******");
            try {
                navi     = new Navigation(navifile,xsltVersion);
            } catch (TransformerConfigurationException e) {
                throw new NavigationInitializationException("Exception while loading navigation file " + navifile, e);
            } catch (IOException e) {
                throw new NavigationInitializationException("Exception while loading navigation file " + navifile, e);
            } catch (SAXException e) {
                throw new NavigationInitializationException("Exception while loading navigation file " + navifile, e);
            } catch (TransformerException e) {
                throw new NavigationInitializationException("Exception while loading navigation file " + navifile, e);
            }
            navis.put(navifile.toURI().toString(), navi);
        }
        
        return navi;
    }
}
