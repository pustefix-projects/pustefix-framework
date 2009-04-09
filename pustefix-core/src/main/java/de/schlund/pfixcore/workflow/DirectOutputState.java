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
 *
 */

package de.schlund.pfixcore.workflow;

import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * The <code>DirectOutputState</code> interface is implemented by classes that wish to produce
 * the Output for a request directly. This is in contrast to the
 * {@link State} interface that is implemented by classes that produce a DOM tree as their response.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */
public interface DirectOutputState {
    /**
     * <code>isAccessible</code> can be called to check if the DirectOutputState is accessible.
     *
     * @param crm a <code>ContextResourceManager</code> that comes from a foreign ContextXMLServlet.
     * @param props the <code>Properties</code> associated with the current PageRequest.
     * @param preq the current <code>PfixServletRequest</code>.
     * @return a <code>boolean</code> value: trueif accessible, false if not.
     * @exception Exception if an error occurs
     */
    boolean isAccessible(ContextResourceManager crm, Properties props, PfixServletRequest preq) throws Exception;
    /**
     * Describe <code>handleRequest</code> method here.
     *
     * @param crm a <code>ContextResourceManager</code> that comes from a foreign ContextXMLServlet.
     * @param props the <code>Properties</code> associated with the current PageRequest.
     * @param preq the current <code>PfixServletRequest</code>.
     * @param res the curent <code>HttpServletResponse</code>.
     * @exception Exception if an error occurs
     */
    void handleRequest(ContextResourceManager crm, Properties props, PfixServletRequest preq, HttpServletResponse res) throws Exception;
}

