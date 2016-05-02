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

import org.pustefixframework.config.contextxmlservice.ProcessActionPageRequestConfig;

import de.schlund.pfixcore.oxm.impl.annotation.ClassNameAlias;

/**
 * @author jtl
 *
 */
@ClassNameAlias("action")
public class ProcessActionPageRequestConfigImpl implements ProcessActionPageRequestConfig {
    
    // unfortunate. can be "true", "false", "step" :-(
    private String forcestop = "false";
    private String jumptopage = null;
    private String jumptopageflow = null;
    private String pageflow = null;
    private String name = null;
    
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

}
