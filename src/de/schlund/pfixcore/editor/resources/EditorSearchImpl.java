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

package de.schlund.pfixcore.editor.resources;

import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.statuscodes.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.apache.oro.text.regex.*;
import org.apache.xml.serialize.*;
import org.apache.xpath.*;
import org.w3c.dom.*;


/**
 * EditorSearchImpl.java
 *
 *
 * Created: Tue Mar 12 22:24:27 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 *
 */

public class EditorSearchImpl implements ContextResource, EditorSearch {
    private static Category CAT           = Category.getInstance(EditorSearchImpl.class);
    private static int      MATCH_CONTEXT = 25;
    
    private Perl5Pattern        pattern   = null;
    private StatusCode          status    = SCODE_RESET;
    private HashMap             result    = new HashMap();
    private HashMap             dynresult = new HashMap();
    private Boolean             full      = Boolean.FALSE;
    private EditorSessionStatus esess;
        
    /**
     *
     * @exception java.lang.Exception <description>
     */
    public void reset() throws Exception {
        pattern   = null;
        status    = SCODE_RESET;
        result    = new HashMap();
        dynresult = new HashMap();
        full      = Boolean.FALSE;
    }

    /**
     *
     * @param context <description>
     * @exception java.lang.Exception <description>
     */
    public void init(Context context) throws Exception {
        esess = EditorRes.getEditorSessionStatus(context.getContextResourceManager());
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.lang.Exception <description>
     */
    public void insertStatus(ResultDocument param1, Element param2) throws Exception {
        // TODO: implement this de.schlund.pfixcore.workflow.ContextResource method
    }

    /**
     *
     * @return <description>
     * @exception java.lang.Exception <description>
     */
    public boolean needsData() throws Exception {
        // it basically doesn't matter what we do here...
        if (status == SCODE_RESET) {
            return true;
        } else {
            return false;
        }
    }
    
    // implementation of de.schlund.pfixcore.editor.resources.EditorSearch interface

    /**
     *
     * @param param1 <description>
     */
    public void setPattern(Perl5Pattern param1) {
        pattern = param1;
    }
    
    public Perl5Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the Full value.
     * @return the Full value.
     */
    public Boolean getFullSource() {
        return full;
    }

    /**
     * Set the Full value.
     * @param newFull The new Full value.
     */
    public void setFullSource(Boolean newFull) {
        this.full = newFull;
    }
    
    /**
     *
     * @return <description>
     */
    public StatusCode getStatus() {
        return status;
    }
        
    /**
     *
     * @return <description>
     */
    public TreeSet getResultSet() {
        synchronized (result) {
            return new TreeSet(result.keySet());
        }
    }

    public TreeSet getDynResultSet() {
        synchronized (dynresult) {
            return new TreeSet(dynresult.keySet());
        }
    }

    public EditorSearchContext[] getSearchContexts(AuxDependency incpart) {
        synchronized (result) {
            ArrayList cons = (ArrayList) result.get(incpart);
            if (cons == null) return null;
            return (EditorSearchContext[]) cons.toArray(new EditorSearchContext[] {});
        }
    }

    public EditorSearchContext[] getDynSearchContexts(AuxDependency incpart) {
        synchronized (dynresult) {
            ArrayList cons = (ArrayList) dynresult.get(incpart);
            if (cons == null) return null;
            return (EditorSearchContext[]) cons.toArray(new EditorSearchContext[] {});
        }
    }
    
    private void addSearchContext(AuxDependency incpart, EditorSearchContext sc, HashMap map) {
        synchronized (map) {
            ArrayList cons = (ArrayList) map.get(incpart);
            if (cons == null) {
                cons = new ArrayList();
                map.put(incpart, cons);
            }
            cons.add(sc);
        }
    }

    
    // Now for the funny part: Do the search.

    // FIXME FIXME
    // For now we do the complete search here. I want to make this threaded, with setting the status to SCODE_RUNNING,
    // and when completed, the search thread will set the status to SCODE_OK -- or to SCODE_INT if a timeout is hit.
    
    public void startSearch() throws Exception {
        TargetGenerator tgen        = esess.getProduct().getTargetGenerator();
        String          product     = esess.getProduct().getName();
        TreeSet         allincludes = tgen.getDependencyRefCounter().getDependenciesOfType(DependencyType.TEXT);
        TreeSet         allcommons  = EditorCommonsFactory.getInstance().getAllCommons();
        
        for (Iterator i = allincludes.iterator(); i.hasNext(); ) {
            AuxDependency incpart = (AuxDependency) i.next();
            String        prod    = incpart.getProduct();
            
            Element  part = EditorHelper.getIncludePart(tgen, incpart);
            if (part != null) {
                Element content = (Element) XPathAPI.selectSingleNode(part, "./product[@name='" + prod + "']");
                if (content != null) {
                    doSearch(incpart, content, result);
                } else {
                    CAT.warn("*** Didn't get content for IncludePart " + incpart.getPart() + "@" + incpart.getPath());
                }
            }
        }

        for (Iterator i = allcommons.iterator(); i.hasNext(); ) {
            AuxDependency dynpart = (AuxDependency) i.next();
            String        prod    = dynpart.getProduct();
            if (prod.equals("default")) {
                AuxDependency tmp = AuxDependencyFactory.getInstance().
                    getAuxDependency(DependencyType.TEXT, dynpart.getPath(), dynpart.getPart(), product);
                if (!allcommons.contains(tmp)) {
                    Element  part = EditorHelper.getIncludePart(tgen, dynpart);
                    if (part != null) {
                        Element content = (Element) XPathAPI.selectSingleNode(part, "./product[@name='default']");
                        if (content != null) {
                            doSearch(dynpart, content, dynresult);
                        }
                    }
                }
            } else if (prod.equals(product)) {
                Element  part = EditorHelper.getIncludePart(tgen, dynpart);
                if (part != null) {
                    Element content = (Element) XPathAPI.selectSingleNode(part, "./product[@name='" + product + "']");
                    if (content != null) {
                        doSearch(dynpart, content, dynresult);
                    }
                }
            }
        }
        
        status = SCODE_OK;
    }

    private void doSearch(AuxDependency incpart, Element content, HashMap result) throws Exception {
        if (!full.booleanValue()) {
            doTextNodeSearch(incpart, content, result);
        } else {
            doSourceCodeSearch(incpart, content, result);
        }
    }

    private void doSourceCodeSearch(AuxDependency incpart, Element content, HashMap resmap) throws Exception {
        StringWriter  sw  = new StringWriter();
        OutputFormat  out = new OutputFormat("XML","ISO-8859-1",true);
        XMLSerializer xs  = new XMLSerializer(sw, out);
        out.setLineWidth(0);
        xs.asDOMSerializer().serialize(content);
        
        String text = sw.getBuffer().toString();
        Perl5Matcher        matcher = new Perl5Matcher();
        PatternMatcherInput minp    = new PatternMatcherInput(text);
        while (matcher.contains(minp, pattern)) {
            String match = minp.match();
            String pre   = minp.preMatch();
            String post  = minp.postMatch();
            if (pre.length() > MATCH_CONTEXT) {
                pre = "..." + pre.substring(pre.length() - MATCH_CONTEXT);
            }
            if (post.length() > MATCH_CONTEXT) {
                post = post.substring(0, MATCH_CONTEXT) + "...";
            }
            //CAT.debug("\n===>" + pre + "[[" + match + "]]" + post);
            addSearchContext(incpart, new EditorSearchContext(pre, match, post), resmap);
        }
    }
    
    private void doTextNodeSearch(AuxDependency incpart, Element content, HashMap resmap) throws Exception {
        NodeList textnodes = XPathAPI.selectNodeList(content, ".//text()");
        for (int j = 0; j < textnodes.getLength(); j++) {
            String text = ((Text) textnodes.item(j)).getNodeValue();
            text = text.trim();
            if (!text.equals("")) {
                // CAT.debug("===>>>> Looking in: " + text);
                Perl5Matcher        matcher = new Perl5Matcher();
                PatternMatcherInput minp    = new PatternMatcherInput(text);
                while (matcher.contains(minp, pattern)) {
                    String match = minp.match();
                    String pre   = minp.preMatch();
                    String post  = minp.postMatch();
                    if (pre.length() > MATCH_CONTEXT) {
                        pre = "..." + pre.substring(pre.length() - MATCH_CONTEXT);
                    }
                    if (post.length() > MATCH_CONTEXT) {
                        post = post.substring(0, MATCH_CONTEXT) + "...";
                    }
                    // CAT.debug("\n===>" + pre + "[[" + match + "]]" + post);
                    addSearchContext(incpart, new EditorSearchContext(pre, match, post), resmap);
                }
            }
        }
    }
    
}
 
