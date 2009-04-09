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
package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pustefixframework.config.contextxmlservice.ProcessActionStateConfig;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.beans.Alias;
import de.schlund.pfixcore.oxm.impl.annotation.ClassNameAlias;

/**
 * @author jtl
 *
 */
@ClassNameAlias("action")
public class ProcessActionStateConfigImpl implements ProcessActionStateConfig {
    
    private String name = null;
    private LinkedList<String> submitprefixes = new LinkedList<String>();
    private LinkedList<String> retrieveprefixes = new LinkedList<String>();
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getRetrieveIWrapperPrefixes()
     */
    @Alias("retrieve")
    public List<String> getRetrievePrefixes() {
        return retrieveprefixes;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getSubmitIWrapperPrefixes()
     */
    @Alias("submit")
    public List<String> getSubmitPrefixes() {
        return submitprefixes;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param submitwrappers the submitwrappers to set
     */
    public void addSubmitPrefix(String prefix) {
        submitprefixes.add(prefix);
    }

    /**
     * @param retrievewrappers the retrievewrappers to set
     */
    public void addRetrievePrefix(String prefix) {
        retrieveprefixes.add(prefix);
    }

    void checkPrefixes(Map<String, IWrapperConfigImpl> iwrappers) throws SAXException{
        for (String prefix : submitprefixes) {
            if (!iwrappers.containsKey(prefix)) {
                throw new SAXException("prefix " + prefix + " from 'submit' part of action '" + name + "' isn't a defined IWrapper");
            }
        }
        for (String prefix : retrieveprefixes) {
            if (!iwrappers.containsKey(prefix)) {
                throw new SAXException("prefix " + prefix + " from 'retrieve' part of action '" + name + "' isn't a defined IWrapper");
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[" + name + " submit:<");
        for (String prefix : submitprefixes) {
            buf.append(prefix + " ");
        }
        buf.append("> retrieve:<");
        for (String prefix : retrieveprefixes) {
            buf.append(prefix + " ");
        }
        buf.append(">]");
        return buf.toString();
    }
}
