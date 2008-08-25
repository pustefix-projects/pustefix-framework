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

package de.schlund.pfixcore.editor2.frontend.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pustefixframework.container.annotations.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorException;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorIncludeHasChangedException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.frontend.util.ContextStore;
import de.schlund.pfixcore.editor2.frontend.util.DiffUtil;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.editor2.frontend.util.DiffUtil.ComparedLine;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.util.Xml;

public abstract class CommonIncludesResource {
    
    private ConfigurationService configuration;
    
    private ProjectsResource projectsResource;
    
    private IncludesResource includesResource;
    
    private SessionResource sessionResource;
    
    private BackupService backup;
    
    private Context                 context;

    private IncludePartThemeVariant selectedIncludePart;

    private Set<String>             openDirectories = Collections.synchronizedSet(new HashSet<String>());

    private Set<String>             openFiles       = Collections.synchronizedSet(new HashSet<String>());

    private ComparedLine[]          lastDiff;

    private boolean                 indentedContent = false;

    protected abstract boolean securityMayCreateIncludePartThemeVariant(IncludePart includePart, Theme theme);

    protected abstract IncludePartThemeVariant internalSelectIncludePart(Project project, String path, String part, String theme);

    protected abstract Collection<Theme> getPossibleThemes(IncludePartThemeVariant selectedIncludePart, Project project, Collection<Page> pages);

    protected abstract boolean securityMayEditIncludePartThemeVariant(IncludePartThemeVariant variant);

    protected abstract void renderAllIncludes(ResultDocument resdoc, Element elem, Project project);

    protected abstract Set<IncludeFile> getIncludeFilesInDirectory(String dirname, Project project);

    protected abstract Set<IncludePartThemeVariant> getIncludePartsInFile(String filename, Project project);

    @InitResource
    public void init(Context context) throws Exception {
        this.context = context;
    }

    @InsertStatus
    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        // System.out.println("In IS");
        Project project = projectsResource.getSelectedProject();
        if (project != null) {
            this.renderAllIncludes(resdoc, elem, project);
        }

        if (this.selectedIncludePart != null) {
            // System.out.println("In IS: HASH: " +
            // this.selectedIncludePart.getMD5());
            Element currentInclude = resdoc.createSubNode(elem, "currentinclude");
            currentInclude.setAttribute("path", this.selectedIncludePart.getIncludePart().getIncludeFile().getPath());
            currentInclude.setAttribute("part", this.selectedIncludePart.getIncludePart().getName());
            currentInclude.setAttribute("theme", this.selectedIncludePart.getTheme().getName());
            currentInclude.setAttribute("hash", this.selectedIncludePart.getMD5());
            if (this.securityMayEditIncludePartThemeVariant(this.selectedIncludePart)) {
                currentInclude.setAttribute("mayEdit", "true");
            } else {
                currentInclude.setAttribute("mayEdit", "false");
            }

            // Render possible new branches
            Collection<Page> pages = this.selectedIncludePart.getAffectedPages();
            Collection<Theme> themes = this.getPossibleThemes(this.selectedIncludePart, project, pages);
            if (!themes.isEmpty()) {
                Element possibleThemes = resdoc.createSubNode(currentInclude, "possiblethemes");
                for (Iterator<Theme> i = themes.iterator(); i.hasNext();) {
                    Theme theme = i.next();
                    ResultDocument.addTextChild(possibleThemes, "option", theme.getName());
                }
            }

            // Render backups
            Collection<String> backups = backup.listIncludeVersions(this.selectedIncludePart);
            if (!backups.isEmpty()) {
                Element backupsNode = resdoc.createSubNode(currentInclude, "backups");
                for (Iterator<String> i = backups.iterator(); i.hasNext();) {
                    String version = i.next();
                    ResultDocument.addTextChild(backupsNode, "option", version);
                }
            }

            // Render affected pages

            if (!pages.isEmpty()) {
                Element pagesNode = resdoc.createSubNode(currentInclude, "pages");
                HashMap<Project, Element> projectNodes = new HashMap<Project, Element>();
                for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
                    Page page = i.next();
                    Element projectNode;
                    if (projectNodes.containsKey(page.getProject())) {
                        projectNode = projectNodes.get(page.getProject());
                    } else {
                        projectNode = resdoc.createSubNode(pagesNode, "project");
                        projectNode.setAttribute("name", page.getProject().getName());
                        projectNodes.put(page.getProject(), projectNode);
                    }
                    Element pageNode = resdoc.createSubNode(projectNode, "page");
                    pageNode.setAttribute("name", page.getName());
                    if (page.getVariant() != null) {
                        pageNode.setAttribute("variant", page.getVariant().getName());
                    }
                }
            }

            // Render includes
            TreeSet<IncludePartThemeVariant> includes = new TreeSet<IncludePartThemeVariant>(this.selectedIncludePart.getIncludeDependencies(false));
            if (!includes.isEmpty()) {
                Element includesNode = resdoc.createSubNode(currentInclude, "includes");
                for (Iterator<IncludePartThemeVariant> i = includes.iterator(); i.hasNext();) {
                    IncludePartThemeVariant variant = i.next();
                    this.renderInclude(variant, includesNode);
                }
            }

            // Render images
            // Get images for current target and all include parts
            TreeSet<Image> images = new TreeSet<Image>(this.selectedIncludePart.getImageDependencies(true));
            if (!images.isEmpty()) {
                Element imagesNode = resdoc.createSubNode(currentInclude, "images");
                for (Iterator<Image> i = images.iterator(); i.hasNext();) {
                    Image image = i.next();
                    Element imageNode = resdoc.createSubNode(imagesNode, "image");
                    imageNode.setAttribute("path", image.getPath());
                    imageNode.setAttribute("modtime", Long.toString(image.getLastModTime()));
                }
            }

            // Render content of include part
            Node content = this.selectedIncludePart.getIncludePart().getContentXML();
            if (content != null) {
                Element contentNode = resdoc.createSubNode(currentInclude, "content");
                contentNode.appendChild(contentNode.getOwnerDocument().importNode(content, true));
            }

            // Render compare view, if available
            if (this.lastDiff != null) {
                Element compareNode = resdoc.createSubNode(currentInclude, "compare");
                for (int i = 0; i < this.lastDiff.length; i++) {
                    ComparedLine line = this.lastDiff[i];
                    Element lineNode = resdoc.createSubNode(compareNode, "line");
                    lineNode.setAttribute("status", line.getStatus().toString());
                    if (line.getVersion1() != null) {
                        ResultDocument.addTextChild(lineNode, "version1", line.getVersion1());
                    }
                    if (line.getVersion2() != null) {
                        ResultDocument.addTextChild(lineNode, "version2", line.getVersion2());
                    }
                }
            }

            // Render other users editing this include part at the same time
            Element concurrentEditsNode = resdoc.createSubNode(currentInclude, "concurrentedits");
            Map<Context, String> contextmap = ContextStore.getInstance().getContextMap();
            for (Iterator<Context> i = contextmap.keySet().iterator(); i.hasNext();) {
                Context foreignCtx = i.next();
                if (foreignCtx != this.context && EditorResourceLocator.getSessionResource(foreignCtx).isInIncludeEditView()) {
                    IncludePartThemeVariant otherSelectedInclude = EditorResourceLocator.getIncludesResource(foreignCtx).getSelectedIncludePart();
                    if (otherSelectedInclude != null && otherSelectedInclude.equals(this.selectedIncludePart)) {
                        Element usernameNode = resdoc.createSubNode(concurrentEditsNode, "user");
                        usernameNode.setAttribute("username", contextmap.get(foreignCtx));
                    }
                }
            }
        }
    }

    private void renderInclude(IncludePartThemeVariant variant, Element parent) {
        Document doc = parent.getOwnerDocument();
        Element includeNode = doc.createElement("include");
        parent.appendChild(includeNode);
        includeNode.setAttribute("part", variant.getIncludePart().getName());
        includeNode.setAttribute("path", variant.getIncludePart().getIncludeFile().getPath());
        includeNode.setAttribute("theme", variant.getTheme().getName());

        try {
            TreeSet<IncludePartThemeVariant> variants = new TreeSet<IncludePartThemeVariant>(variant.getIncludeDependencies(false));
            for (Iterator<IncludePartThemeVariant> i = variants.iterator(); i.hasNext();) {
                IncludePartThemeVariant variant2 = i.next();
                this.renderInclude(variant2, includeNode);
            }
        } catch (EditorParsingException e) {
            // Omit dependencies for this part
        }
    }

    public void reset() throws Exception {
        this.selectedIncludePart = null;
    }

    public boolean selectIncludePart(String path, String part, String theme) {
        Project project = projectsResource.getSelectedProject();
        if (project == null) {
            return false;
        }
        IncludePartThemeVariant variant = this.internalSelectIncludePart(project, path, part, theme);
        if (variant == null) {
            return false;
        } else {
            this.selectedIncludePart = variant;
            // Make sure part is visible in navigation
            this.openFileTree(variant.getIncludePart().getIncludeFile().getPath());
            return true;
        }
    }

    public void unselectIncludePart() {
        this.selectedIncludePart = null;
        // Reset tree status, too
        this.openDirectories.clear();
        this.openFiles.clear();
    }

    public IncludePartThemeVariant getSelectedIncludePart() {
        return this.selectedIncludePart;
    }

    public int restoreBackup(String version, String hash) {
        if (this.selectedIncludePart == null) {
            return 1;
        }

        if (!this.selectedIncludePart.getMD5().equals(hash)) {
            return 2;
        }

        try {
            if (backup.restoreInclude(this.selectedIncludePart, version)) {
                return 0;
            } else {
                return 1;
            }
        } catch (EditorSecurityException e) {
            return 1;
        }
    }

    public String getMD5() {
        if (this.selectedIncludePart == null) {
            return "";
        }
        return this.selectedIncludePart.getMD5();
    }

    private boolean checkLineNotEmpty(CharSequence seq) {
        if (seq.length() == 0) {
            return false;
        }
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            if (c != ' ') {
                return true;
            }
        }
        return false;
    }

    public String getContent() {
        if (this.selectedIncludePart == null) {
            return "";
        }
        Node xml = this.selectedIncludePart.getXML();
        if (xml == null) {
            return "";
        }

        // Make sure xmlns declarations are present in top-level element
        if (xml instanceof Element) {
            Map<String, String> xmlnsMappings = configuration.getPrefixToNamespaceMappings();
            Element elem = (Element) xml;
            for (Iterator<String> i = xmlnsMappings.keySet().iterator(); i.hasNext();) {
                String prefix = i.next();
                String url = xmlnsMappings.get(prefix);
                elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, url);
            }
        }

        String serialized = Xml.serialize(xml, false, false);
        serialized = Xml.stripElement(serialized);
        StringBuffer output = new StringBuffer();

        for (int i = 0; i < serialized.length(); i++) {
            char c = serialized.charAt(i);
            output.append(this.unicodeTranslate(c));
        }

        if (output.length() > 0) {
            char firstChar = output.charAt(0);
            if (firstChar == ' ' || firstChar == '\t' || firstChar == '\n' || firstChar == '\r') {
                indentedContent = true;
                return fixIndention(output, 0);
            } else {
                indentedContent = false;
                return output.toString();
            }
        } else {
            indentedContent = true;
            return "";
        }
    }

    public boolean isContentIndented() {
        return indentedContent;
    }

    private String unicodeTranslate(char c) {
        if (c < 128 || (c >= 0xc0 && c <= 0xcf) || (c >= 0xd1 && c <= 0xd6) || (c >= 0xd9 && c <= 0xdd) || (c >= 0xdf && c <= 0xef) || (c >= 0xf1 && c <= 0xf6)
                || (c >= 0xf9 & c <= 0xfd) || c == 0xff) {
            return Character.toString(c);
        } else {
            return "&#" + Integer.toString(c) + ";";
        }
    }

    public void setContent(String content, boolean indent, String hash) throws SAXException, EditorException {
        this.lastDiff = null;

        this.indentedContent = indent;

        // Check whether hashcode has changed
        if (!this.selectedIncludePart.getMD5().equals(hash)) {
            String newHash = this.getMD5();
            String newContent = this.getContent();
            String merged = DiffUtil.merge(content, newContent);
            this.lastDiff = DiffUtil.compare(content, newContent);
            throw new EditorIncludeHasChangedException(merged, newHash);
        }

        StringBuffer xmlcode = new StringBuffer();
        xmlcode.append("<part");

        // Add predefined prefixes
        Map<String, String> xmlnsMappings = configuration.getPrefixToNamespaceMappings();
        for (Iterator<String> i = xmlnsMappings.keySet().iterator(); i.hasNext();) {
            String prefix = i.next();
            String url = xmlnsMappings.get(prefix);
            xmlcode.append(" xmlns:" + prefix + "=\"" + url + "\"");
        }

        xmlcode.append(">");
        if (indent) {
            xmlcode.append(fixIndention(content, 6));
        } else {
            xmlcode.append(content);
        }
        xmlcode.append("</part>");
        Document doc = Xml.parseStringMutable(xmlcode.toString());

        this.selectedIncludePart.setXML(doc.getDocumentElement(), indent);
    }

    public boolean createAndSelectBranch(String themeName) throws EditorIOException, EditorParsingException, EditorSecurityException {
        Theme theme = null;
        for (Iterator<Theme> i = this.getPossibleThemes(this.selectedIncludePart, projectsResource.getSelectedProject(),
                this.selectedIncludePart.getAffectedPages()).iterator(); i.hasNext();) {
            Theme theme2 = i.next();
            if (theme2.getName().equals(themeName)) {
                theme = theme2;
            }
        }

        if (theme == null) {
            return false;
        }

        if (!this.securityMayCreateIncludePartThemeVariant(this.selectedIncludePart.getIncludePart(), theme)) {
            return false;
        }

        this.selectedIncludePart = this.selectedIncludePart.getIncludePart().createThemeVariant(theme);

        return true;
    }

    public boolean deleteSelectedBranch() {
        try {
            this.selectedIncludePart.getIncludePart().deleteThemeVariant(this.selectedIncludePart);
        } catch (EditorSecurityException e) {
            return false;
        } catch (EditorIOException e) {
            return false;
        } catch (EditorParsingException e) {
            return false;
        }
        this.selectedIncludePart = null;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.frontend.resources.CommonIncludesResource#closeDirectoryTree(java.lang.String)
     */
    public void closeDirectoryTree(String name) {
        this.openDirectories.remove(name);
        // Close all file tree below this directory
        for (Iterator<String> i = this.openFiles.iterator(); i.hasNext();) {
            String file = i.next();
            try {
                String dir = file.substring(0, file.lastIndexOf('/'));
                if (dir.equals(name)) {
                    i.remove();
                }
            } catch (StringIndexOutOfBoundsException e) {
                // Okay, file does not contain a "/", so if we are closing the
                // special "/" directory, this file should be closed
                if (name.equals("/")) {
                    i.remove();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.frontend.resources.CommonIncludesResource#closeFileTree(java.lang.String)
     */
    public void closeFileTree(String name) {
        this.openFiles.remove(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.frontend.resources.CommonIncludesResource#openDirectoryTree(java.lang.String)
     */
    public SortedSet<IncludeFile> openDirectoryTree(String name) {
        SortedSet<IncludeFile> files = new TreeSet<IncludeFile>(this.getIncludeFilesInDirectory(name, projectsResource.getSelectedProject()));
        openDirectories.add(name);
        return files;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.frontend.resources.CommonIncludesResource#openFileTree(java.lang.String)
     */
    public SortedSet<IncludePartThemeVariant> openFileTree(String name) {
        // Make sure directory is open
        try {
            this.openDirectoryTree(name.substring(0, name.lastIndexOf('/')));
        } catch (StringIndexOutOfBoundsException e) {
            this.openDirectoryTree("/");
        }

        TreeSet<IncludePartThemeVariant> parts = new TreeSet<IncludePartThemeVariant>(this.getIncludePartsInFile(name, projectsResource.getSelectedProject()));
        openFiles.add(name);
        return parts;
    }

    protected boolean isDirectoryOpen(String name) {
        return openDirectories.contains(name);
    }

    protected boolean isFileOpen(String name) {
        return openFiles.contains(name);
    }

    private int countLeadingSpaces(CharSequence line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                return i;
            }
        }
        return -1;
    }

    private String fixIndention(CharSequence input, int targetIndentionLevel) {
        StringBuffer temp = new StringBuffer();

        // Iterate over all lines, doing the following tasks
        // - shorten lines containing only tabs and spaces to empty lines
        // - calculate number of trailing spaces on all (non-empty) lines
        // - convert \r and \r\n to \n
        // - convert \t to double space
        StringBuffer lineBuffer = new StringBuffer();
        int maxSpaces = -1;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\r' && (i + 1) < input.length() && input.charAt(i + 1) == '\n') {
                i++;
                c = '\n';
            }
            if (c == '\n' || c == '\r' || i == (input.length() - 1)) {
                // End of line detected
                // Handle special case where end of line is only end of string
                if (c != '\n' && c != '\r') {
                    lineBuffer.append(c);
                }

                if (checkLineNotEmpty(lineBuffer)) {
                    temp.append(lineBuffer);
                    int spaces = countLeadingSpaces(lineBuffer);
                    if (maxSpaces == -1) {
                        maxSpaces = spaces;
                    } else {
                        maxSpaces = Math.min(spaces, maxSpaces);
                    }
                }
                temp.append("\n");
                lineBuffer.setLength(0);
            } else if (c == '\t') {
                lineBuffer.append("  ");
            } else {
                lineBuffer.append(c);
            }
        }

        // Remove leading empty lines
        while (temp.length() > 0 && temp.charAt(0) == '\n') {
            temp.deleteCharAt(0);
        }

        // Remove trailing empty lines
        while (temp.length() > 0 && temp.charAt(temp.length() - 1) == '\n') {
            temp.deleteCharAt(temp.length() - 1);
        }

        // Iterate over all lines removing maxSpaces spaces and adding
        // targetIndentionLevel spaces
        StringBuffer output = new StringBuffer();
        StringBuffer spacesToAdd = new StringBuffer();
        for (int i = 0; i < targetIndentionLevel; i++) {
            spacesToAdd.append(' ');
        }
        for (int i = 0; i < temp.length(); i++) {
            char c = temp.charAt(i);
            if (c == '\n' || i == (temp.length() - 1)) {
                // End of line detected
                // Handle special case where end of line is only end of string
                if (c != '\n') {
                    lineBuffer.append(c);
                }

                output.append(spacesToAdd);
                if (lineBuffer.length() > maxSpaces) {
                    output.append(lineBuffer.substring(maxSpaces));
                }
                output.append("\n");
                lineBuffer.setLength(0);
            } else {
                lineBuffer.append(c);
            }
        }

        // Remove last trailing newline, but make sure string
        // is not empty
        if (output.length() > 0) {
            output.deleteCharAt(output.length() - 1);
        }

        return output.toString();
    }
    
    @Inject
    public void setConfigurationService(ConfigurationService configuration) {
        this.configuration = configuration;
    }
    
    @Inject
    public void setBackupService(BackupService backup) {
        this.backup = backup;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }

    @Inject
    public void setIncludesResource(IncludesResource includesResource) {
        this.includesResource = includesResource;
    }

    @Inject
    public void setSessionResource(SessionResource sessionResource) {
        this.sessionResource = sessionResource;
    }
}
