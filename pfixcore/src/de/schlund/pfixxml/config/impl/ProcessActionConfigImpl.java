/**
 * 
 */
package de.schlund.pfixxml.config.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.ProcessActionConfig;

/**
 * @author jtl
 *
 */
public class ProcessActionConfigImpl implements ProcessActionConfig {
    
    // unfortunate. can be "true", "false", "step" :-(
    private String forcestop = "false";
    private String jumptopage = null;
    private String jumptopageflow = null;
    private String pageflow = null;
    private String name = null;
    private LinkedList<String> submitprefixes = new LinkedList<String>();
    private LinkedList<String> retrieveprefixes = new LinkedList<String>();
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getForceStop()
     */
    public String getForceStop() {
        return forcestop;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getJumpToPage()
     */
    public String getJumpToPage() {
        return jumptopage;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getJumpToPageFlow()
     */
    public String getJumpToPageFlow() {
        return jumptopageflow;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getPageflow()
     */
    public String getPageflow() {
        return pageflow;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getRetrieveIWrapperPrefixes()
     */
    public List<String> getRetrievePrefixes() {
        return retrieveprefixes;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ProcessActionConfig#getSubmitIWrapperPrefixes()
     */
    public List<String> getSubmitPrefixes() {
        return submitprefixes;
    }

    /**
     * @param forcestop the forcestop to set
     */
    public void setForceStop(String forcestop) {
        this.forcestop = forcestop;
    }

    /**
     * @param jumptopage the jumptopage to set
     */
    public void setJumpToPage(String jumptopage) {
        this.jumptopage = jumptopage;
    }

    /**
     * @param jumptopageflow the jumptopageflow to set
     */
    public void setJumpToPageflow(String jumptopageflow) {
        this.jumptopageflow = jumptopageflow;
    }

    /**
     * @param pageflow the pageflow to set
     */
    public void setPageflow(String pageflow) {
        this.pageflow = pageflow;
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
