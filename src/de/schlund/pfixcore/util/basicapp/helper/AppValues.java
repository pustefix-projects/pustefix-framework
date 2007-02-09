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

package de.schlund.pfixcore.util.basicapp.helper;


/**
 * All constants for building a new Project
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class AppValues {
    
    /** The property describing the path to the log4j.xml */
    public static final String LOG4JPROP       = "pustefix.newprjlog4j.config";
    /** The basic path set by ant while starting the app */
    public static final String BASICPATH       = System.getProperty("pustefix.docroot");
    /** A String array with basic items */
    public static final String[] ITEMS         = {"name", "language"};
    /** A String array with basic servlet items */
    public static final String[] SERVLETITEMS  = {"servletname", "servletpath"};
    /** An array containing the foldernames */
    public static final String[] FOLDERNAMES   = {"conf", "img", "xsl", "xml", 
            "txt", "htdocs"};
    /** A String describing the conf folder name */
    public static final String CONFFOLDER      = "/conf/";
    /** A String describing the xsl folder name */
    public static final String XSLFOLDER       = "/xsl/";
    /** A String describing the xml folder name */
    public static final String XMLCONSTANT       = "/xml/";
    /** The htdocs folder */
    public static final String HTDOCSFOLDER    = "/htdocs";
    /** String for depend.xml.in */
    public static final String DEPENDXML       = "depend.xml";
    /** String for the content.xml (the basic page) */
    public static final String CONTENTXML      = "content.xml";
    /** String for the frame.xml */
    public static final String FRAMEXML        = "frame.xml";
    /** String for procject.xml.in */
    public static final String PROJECTXMLIN    = "project.xml.in";
    /** String for procject.xml.in */
    public static final String PROJECTXML      = "project.xml";
    /** String for skin.xsl (the basic stylesheet) */
    public static final String SKINXSL         = "skin.xsl";
    /** String for metatags.xsl (first transformation xsl) */
    public static final String METATAGSXSL     = "metatags.xsl";
    /** String for default language */
    public static final String DEFAULTLNG      = "en_GB";
    /** String for default servlet name */
    public static final String DFSERVLETNAME   = "config";
    /** Suffix for the projects comment */
    public static final String PRJCOMMENTSUFF  = "comment";
    /** suffix for docroot tag */
    public static final String DOCROOTSUFFIX  = "cus";
    /** file suffix for prop */
    public static final String CFGFILESUFF    = ".conf.xml";
    /** default name for pages */
    public static final String PAGEDEFAULT   = "home";
    /** prefix for pages to be displayes */
    public static final String PAGEDEFPREFIX = "main_";
    /** and also the pages Suffix */
    public static final String PAGEDEFSUFFIX  = ".xml";
    /** All those values as an array */
    public static final String[] TEMPLATEARR   = {"config.tmpl", 
            "depend.tmpl", "frame.tmpl", "project.tmpl", 
            "skin.tmpl", "metatags.tmpl", "page.tmpl"};
    
    
    /** All Template strings */
    public static final String CONFIG_TMPL     = "config.tmpl";
    public static final String DEPEND_TMPL     = "depend.tmpl";
    public static final String CONTENT_TMPL    = "content.tmpl";
    public static final String FRAME_TMPL      = "frame.tmpl";
    public static final String PROJECT_TMPL    = "project.tmpl";
    public static final String SKIN_TMPL       = "skin.tmpl";
    public static final String METATAGS_TMPL   = "metatags.tmpl";
    public static final String PAGE_TMPL       = "page.tmpl";
    
    /** The template folder */   
    public static final String TEMPLFOLDERPATH = "core/prjtemplates/";
    /** A string for a subfolder of pages */
    public static final String TXTSUBFOLDER    = "/pages";
    /** A String for  the folder containing TXTSUBFOLDER*/
    public static final String TXTFOLDER       = "txt";
    /** A String for  the folder containing TXTSUBFOLDER*/
    public static final String IMGFOLDER       = "/img";
    /** A path to the pages folder */
    public static final String PATHTO_PAGES    = "/txt/pages/";
        
    /** Strings for config.prop.in */
    public static final String CONFIGTAG_SERVLETINFO  = "servletinfo";
    public static final String CONFIGATT_DEPEND       = "depend";
    public static final String CONFIGATT_NAME         = "name";
    public static final String CONFIGATT_NAMEPREFIX   = "pfixcore_project:";
    public static final String CONFIGATT_NAMEPOSTFIX  = "::servlet:";
    public static final String CONFIGTAG_FLOWSTEP     = "flowstep";
    public static final String CONFIGTAG_PAGEREQUEST  = "pagerequest";
    
    /** Strings for the depend.xml.in */
    public static final String DEPENDTAG_MAKE         = "make";
    public static final String DEPENDATT_PROJECT      = "project";
    public static final String DEPENDATT_LANG         = "lang";
    public static final String DEPENDTAG_PAGE         = "page";
    public static final String DEPENDTAG_NAVIGATION   = "navigation";
    public static final String DEPENDATT_NAME         = "name";
    public static final String DEPENDATT_HANDLER      = "handler";
    public static final String DEPENDATT_STYLESHEET   = "stylesheet";
    public static final String DEPENDTAG_INCLUDE      = "include";
    public static final String DEPENDTAG_STDPAGE      = "standardpage";
    public static final String DEPENDATT_XML          = "xml";
    public static final String DEPENDATT_HOME         = "home";
    public static final String DEPENTATT_HOME_DEF     = "home1";
    
    
    /** Strings for the project.xml.in */
    public static final String PROJECTTAG_DOCUMENTROOT= "documentroot";
    public static final String PROJECTTAG_PROJECT     = "project";
    public static final String PROJECTATT_NAME        = "name";
    public static final String PROJECTTAG_SERVERNAME  = "servername";
    public static final String PROJECTTAG_SERVERALIAS = "serveralias";
    public static final String PROJECTTAG_DEPEND      = "depend";
    public static final String PROJECTTAG_PROPFILE    = "propfile";
    public static final String PROJECTTAG_PASSTHROUGH = "passthrough";
    public static final String PROJECTTAG_COMMENT     = "comment";
    public static final String PROJECTTAG_DEFPATH     = "defpath";
    public static final String PROJECTTAG_SERVLET     = "servlet";
    public static final String PROJECTTAG_ACTIVE      = "active";
    public static final String PROJECTTAG_CLASS       = "class";
    public static final String PROJECTTAG_DOCROOT     = "cus:docroot";
    public static final String PROJECTVALUE_TRUE      = "true";
    public static final String PROJECTVALUE_CLASS     = "de.schlund.pfixxml.ContextXMLServlet";
    public static final String PROJECTPRRF_DOCROOT    = "cus"; 
    public static final String PROJECTATT_EDITOR      = "useineditor";
    
    /** Strings for frame xml */
    public static final String FRAMETAG_INCLUDE       = "pfx:include";
    public static final String FRAMEATT_HREF          = "href";
}
