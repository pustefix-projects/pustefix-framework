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
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixxml.util.XPath;

public class SecurityManagerServiceImpl implements SecurityManagerService {
    private ThreadLocal principal;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private HashMap configuration;

    public SecurityManagerServiceImpl(FileSystemService filesystem,
            PathResolverService pathresolver) {
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.configuration = new HashMap();
    }

    public void setPrincipal(Principal principal) {
        this.principal.set(principal);
    }

    public Principal getPrincipal() {
        return (Principal) this.principal.get();
    }

    public void reloadConfiguration() throws EditorIOException,
            EditorParsingException {
        File configFile = new File(this.pathresolver
                .resolve("common/conf/userdata.xml"));
        Document xml = null;
        synchronized (this.filesystem.getLock(configFile)) {
            try {
                xml = this.filesystem.readXMLDocumentFromFile(configFile);
            } catch (FileNotFoundException e) {
                String err = "File " + configFile.getAbsolutePath()
                        + " could not be found!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            } catch (SAXException e) {
                String err = "Error during parsing file "
                        + configFile.getAbsolutePath() + "!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorParsingException(err, e);
            } catch (IOException e) {
                String err = "File " + configFile.getAbsolutePath()
                        + " could not be read!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            } catch (ParserConfigurationException e) {
                String err = "Error during initialization of XML parser!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new RuntimeException(err, e);
            }
        }
        synchronized (this.configuration) {
            this.configuration.clear();
            try {
                for (Iterator i = XPath.select(xml, "/userinfo/user")
                        .iterator(); i.hasNext();) {
                    Element userNode = (Element) i.next();
                    String userName = userNode.getAttribute("id");
                    if (userName == null) {
                        String err = "<user> has to have id attribute!";
                        Logger.getLogger(this.getClass()).error(err);
                        throw new EditorParsingException(err);
                    }
                    Element globalNode = (Element) XPath.selectNode(userNode,
                            "permissions/global");
                    boolean userIsAdmin = false;
                    boolean userMayEditDynIncludes = false;
                    if (globalNode != null) {
                        String temp;
                        temp = globalNode.getAttribute("admin");
                        if (temp != null) {
                            userIsAdmin = temp.equals("true");
                        }
                        temp = globalNode.getAttribute("editDynIncludes");
                        if (temp != null) {
                            userMayEditDynIncludes = temp.equals("true");
                        }
                    }
                    HashMap userConfiguration = new HashMap();
                    userConfiguration.put("global_admin", new Boolean(
                            userIsAdmin));
                    userConfiguration.put("global_editDynIncludes",
                            new Boolean(userMayEditDynIncludes));
                    for (Iterator i2 = XPath.select(userNode,
                            "permissions/project").iterator(); i2.hasNext();) {
                        Element projectNode = (Element) i2.next();
                        String projectName = projectNode.getAttribute("name");
                        if (projectName == null) {
                            String err = "<project> has to have name attribute!";
                            Logger.getLogger(this.getClass()).error(err);
                            throw new EditorParsingException(err);
                        }
                        boolean editIncludes = false;
                        boolean editImages = false;
                        String temp;
                        temp = projectNode.getAttribute("editIncludes");
                        if (temp != null) {
                            editIncludes = temp.equals("true");
                        }
                        temp = projectNode.getAttribute("editImages");
                        if (temp != null) {
                            editImages = temp.equals("true");
                        }
                        HashMap projectConfiguration = new HashMap();
                        projectConfiguration.put("editIncludes", new Boolean(
                                editIncludes));
                        projectConfiguration.put("editImages", new Boolean(
                                editImages));
                        userConfiguration.put("project_" + projectName,
                                projectConfiguration);
                    }
                    this.configuration.put(userName, userConfiguration);
                }
            } catch (TransformerException e) {
                // Should never happen as a DOM document is always well-formed!
                String err = "XPath error!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new RuntimeException(err, e);
            }
        }
    }

    public boolean mayEditIncludePartThemeVariant(IncludePartThemeVariant part) {
        for (Iterator i = part.getAffectedPages().iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            if (!mayEditIncludePartForProject(page.getProject()))
                return false;
        }
        return true;
    }

    private boolean mayEditIncludePartForProject(Project project) {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }
        Map userConfig;
        synchronized (this.configuration) {
            if (!this.configuration.containsKey(username)) {
                return false;
            }
            userConfig = (Map) this.configuration.get(username);
        }

        if (((Boolean) userConfig.get("global_admin")).booleanValue()) {
            return true;
        }
        if (!userConfig.containsKey("project_" + project.getName())) {
            return false;
        }
        Map projectConfig = (Map) userConfig.get("project_" + project.getName());
        return ((Boolean) projectConfig.get("editIncludes")).booleanValue();
    }

    public void checkEditIncludePartThemeVariant(IncludePartThemeVariant part)
            throws EditorSecurityException {
        if (!mayEditIncludePartThemeVariant(part)) {
            throw new EditorSecurityException(
                    "Operation editIncludePartThemeVariant not permitted!");
        }
    }

    public boolean mayCreateIncludePartThemeVariant(IncludePart part,
            Theme theme) {
        HashSet pages = new HashSet();
        HashSet themes = new HashSet();
        for (Iterator i = part.getThemeVariants().iterator(); i.hasNext();) {
            IncludePartThemeVariant variant = (IncludePartThemeVariant) i
                    .next();
            pages.addAll(variant.getAffectedPages());
            themes.add(variant.getTheme());
        }
        // For all pages which are affected by this IncludePart:
        // Check whether they will use a variant using theme.
        for (Iterator i = pages.iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            for (Iterator i2 = page.getThemes().getThemes().iterator(); i2
                    .hasNext();) {
                Theme currentTheme = (Theme) i2.next();
                if (currentTheme.equals(theme)) {
                    // This page WILL use the new variant
                    if (!mayEditIncludePartForProject(page.getProject())) {
                        return false;
                    }
                    break;
                }
                if (themes.contains(currentTheme)) {
                    // This page will not use the newly created variant
                    // as it is already using another theme with a higher
                    // priority.
                    break;
                }
            }
        }
        // No test failed, so the user is allowed to create the part
        return true;
    }

    public void checkCreateIncludePartThemeVariant(IncludePart part, Theme theme)
            throws EditorSecurityException {
        if (!mayCreateIncludePartThemeVariant(part, theme)) {
            throw new EditorSecurityException(
                    "Operation createIncludePartThemeVariant not permitted!");
        }
    }

    public boolean mayEditImage(Image image) {
        for (Iterator i = image.getAffectedPages().iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            if (!mayEditImageForProject(page.getProject()))
                return false;
        }
        return true;
    }

    private boolean mayEditImageForProject(Project project) {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }
        Map userConfig;
        synchronized (this.configuration) {
            if (!this.configuration.containsKey(username)) {
                return false;
            }
            userConfig = (Map) this.configuration.get(username);
        }
        if (((Boolean) userConfig.get("global_admin")).booleanValue()) {
            return true;
        }
        if (!userConfig.containsKey("project_" + project.getName())) {
            return false;
        }
        Map projectConfig = (Map) userConfig.get("project_" + project.getName());
        return ((Boolean) projectConfig.get("editImages")).booleanValue();
    }

    public void checkEditImage(Image image) throws EditorSecurityException {
        if (!mayEditImage(image)) {
            throw new EditorSecurityException(
                    "Operation editImage not permitted!");
        }
    }

    public boolean mayEditDynInclude() {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }
        Map userConfig;
        synchronized (this.configuration) {
            if (!this.configuration.containsKey(username)) {
                return false;
            }
            userConfig = (Map) this.configuration.get(username);
        }
        if (((Boolean) userConfig.get("global_admin")).booleanValue()) {
            return true;
        }
        return ((Boolean) userConfig.get("global_editDynIncludes")).booleanValue();
    }

    public void checkEditDynInclude() throws EditorSecurityException {
        if (!mayEditDynInclude()) {
            throw new EditorSecurityException(
                    "Operation editDynInclude not permitted!");
        }
    }

    public boolean mayAdmin() {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }
        Map userConfig;
        synchronized (this.configuration) {
            if (!this.configuration.containsKey(username)) {
                return false;
            }
            userConfig = (Map) this.configuration.get(username);
        }
        return ((Boolean) userConfig.get("global_admin")).booleanValue();
    }

    public void checkAdmin() throws EditorSecurityException {
        if (!mayAdmin()) {
            throw new EditorSecurityException(
                    "User is not an admin!");
        }
    }
}
