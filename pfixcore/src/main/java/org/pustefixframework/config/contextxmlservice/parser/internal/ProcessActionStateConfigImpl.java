/**
 * 
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
