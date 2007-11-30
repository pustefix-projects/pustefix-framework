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
package de.schlund.pfixcore.util;


import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.icl.saxon.TransformerFactoryImpl;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XsltTransformer {

    // sax feature ids, see http://xml.apache.org/xerces2-j/features.html 

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). See http://xml.apache.org/xerces2-j/features.html
     *  Xerces-Default: true */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Namespace prefixes feature id (http://xml.org/sax/features/namespace-prefixes). See http://xml.apache.org/xerces2-j/features.html 
     *  Xerces-Default: false */
    protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";

    /** Validation feature id (http://xml.org/sax/features/validation). See http://xml.apache.org/xerces2-j/features.html
     *  Xerces-Default: false */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). See http://xml.apache.org/xerces2-j/features.html
     *  Xerces-Default: false */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). See http://xml.apache.org/xerces2-j/features.html
     *  Xerces-Default: false */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    /** Dynamic validation feature id (http://apache.org/xml/features/validation/dynamic). See http://xml.apache.org/xerces2-j/features.html
     *  Xerces-Default: false */
    protected static final String DYNAMIC_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";
    
    /** Load external DTD feature id (http://apache.org/xml/features/nonvalidating/load-external-dtd). Does not seem to control loading of external xml schema as of xerces 2.6.2. See http://xml.apache.org/xerces2-j/features.html
     *  Xerces-Default: true */
    protected static final String LOAD_EXTERNAL_DTD_FEATURE_ID = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    protected static final String[] FEATURES = {
            NAMESPACES_FEATURE_ID,
            NAMESPACE_PREFIXES_FEATURE_ID,
            VALIDATION_FEATURE_ID,
            SCHEMA_VALIDATION_FEATURE_ID,
            SCHEMA_FULL_CHECKING_FEATURE_ID,
            DYNAMIC_VALIDATION_FEATURE_ID,
            LOAD_EXTERNAL_DTD_FEATURE_ID
    };

    protected TransformerFactory factory;
    protected Transformer transformer;
    protected File stylesheet;
    protected boolean isValidStylesheet = false;
    /** used by caching mechanism @see #validate() */
    protected File stylesheetOld;
    /** used by caching mechanism @see #validate() */
    protected long stylesheetOldLastModified;
    protected boolean cacheStylesheet = true;
    /** The In memory version of the stylesheet */
    protected Templates templates;
    /** holds additional {@link XsltParam} objects to be passed to the stylesheets */
    protected HashMap params = new HashMap(20);
    protected boolean isValidParams = false;
    protected boolean isValidParser = false;
    /** controls {@link #VALIDATION_FEATURE_ID} and {@link #SCHEMA_VALIDATION_FEATURE_ID}; defaults to false */
    protected boolean isValidate = false;
    /** controls {@link #DYNAMIC_VALIDATION_FEATURE_ID}; defaults to false */
    protected boolean isValidateDynamic = false;
    /** controls {@link #NAMESPACES_FEATURE_ID}; defaults to true */
    protected boolean isNamespaceAware = true;
    protected EntityResolver entitiyResolver = null;
    protected XMLReader xmlReader = null;
    protected XsltErrorListener errorListener = null;
    /** never null */
    protected Project project = null;
    
    /**
     * The Stylesheet needs to be set with {@link #setStylesheet(File)} when using the
     * default constructor.
     * @param project may not be null
     */
    public XsltTransformer(Project project) {
        this(project, null);
    }

    /**
     * @param project must not be null
     * @param stylesheet may be null
     */
    public XsltTransformer(Project project, File stylesheet) {
        if ( project == null ) {
            throw new NullPointerException("project="+project+"; stylesheet="+stylesheet);
        }
        setProject(project);
        setStylesheet(stylesheet);
        // force Saxon (com.icl.saxon.TransformerFactoryImpl) because we have extension functions
        factory = new TransformerFactoryImpl();
        errorListener = new XsltErrorListener(getProject());
    }

    public void transform(File baseDir, String infilename, File destDir, String outfilename) {
        transform(new File(baseDir, infilename), new File(destDir, outfilename));
    }

    public void transform(File infile, File outfile) {
        // We have to validate here for XML Reader to be correctly initialized
        if ( !isValid() ) {
            validate();
        }
        StreamResult result = new StreamResult(outfile);
        // TODO_AH check whether or not to prepend the systemid with file://
        InputSource is = new InputSource("file://"+infile.getAbsolutePath());
        SAXSource source = new SAXSource(xmlReader,is);        
        transform(source, result);
    }

    public void transform(Source source, Result result) {
        if ( !isValid() ) {
            validate();
        }
        try {
            transformer.transform(source, result);
        } catch (TransformerException tex) {
            // TODO_AH delete this misguided attempt to be nice. Seems like transformer never leaves a half-finished destination file.
            // Trying to set the last modified time of the target file
            // to 1970, so it get's transformed again on the next build.
            // (Allowing the developer to still be able to
            // take a look at the eventually half-finished result file).
//            try {
//                String systemId = result.getSystemId();
//                String filename = null;
//                if ( systemId != null && systemId.startsWith("file:") ) {
//                    filename = systemId.substring(5);
//                    if ( filename.startsWith("///") ) {
//                        filename = filename.substring(2);
//                    }
//                    File file = new File(filename);
//                    if ( file.exists() && file.canWrite() ) {
//                        file.setLastModified(0);
//                    }
//                }
//            } catch(Exception e) {
//                // this should not happen, anyways - better dump than hide it
//                e.printStackTrace();
//            }
            throw new BuildException(tex.getMessageAndLocation()+" - Transformation from source=" + source.getSystemId() + " to result=" + result.getSystemId() + " failed.", tex);
        }
    }

    protected boolean isValid() {
        return (isValidParams == true && isValidStylesheet == true && isValidParser == true);
    }
    
    /**
     * @throws BuildException on {@link #stylesheet} == null, {@link TransformerConfigurationException},  {@link XMLReader#setFeature(java.lang.String, boolean)}
     */
    protected void validate() {
        if (isValidStylesheet == false) {
            if (stylesheet == null) {
                throw new BuildException("No stylesheet specified");
            } else {
                try {
                    // check whether we have to reload the stylesheet or not
                    boolean reload = false;
                    long lastModified = stylesheet.lastModified();
                    reload = reload || (isCacheStylesheet() == false);
                    reload = reload || (stylesheet.equals(stylesheetOld) == false);
                    reload = reload || (lastModified > stylesheetOldLastModified);
                    if (reload) {
                        transformer = factory.newTransformer(new StreamSource(stylesheet));
                        // TODO_AH comment in custom ErrorListener
                        // transformer.setErrorListener(errorListener);
                        stylesheetOld = stylesheet;
                        stylesheetOldLastModified = lastModified;
                        isValidStylesheet = true;
                        isValidParams = false; // parameters have to be reapplied
                    }
                } catch (TransformerConfigurationException e) {
                    stylesheetOld = null;
                    stylesheetOldLastModified = 0;
                    throw new BuildException("Could not initialize XSLT Transformer (stylesheet=\""+stylesheet+"\")", e);
                }
            }
        }
        if (isValidParser == false) {
            // force Xerces (org.apache.xerces.parsers.SAXParser) because xml schema validation features
            this.xmlReader = new SAXParser();
            try {
                this.xmlReader.setFeature(VALIDATION_FEATURE_ID         , isValidate());
                this.xmlReader.setFeature(SCHEMA_VALIDATION_FEATURE_ID  , isValidate());
                this.xmlReader.setFeature(DYNAMIC_VALIDATION_FEATURE_ID , isValidateDynamic());
                this.xmlReader.setFeature(NAMESPACES_FEATURE_ID         , isNamespaceAware());
            } catch (Exception e) {
                throw new BuildException("There was a problem configuring "+this.xmlReader+". this="+this.toString(), e);
            }
            if ( getEntitiyResolver() != null ) {
                this.xmlReader.setEntityResolver(getEntitiyResolver());
            }
            isValidParser = true;
        }
        if (isValidParams == false) {
            // assert transformer != null : "Exception should have been thrown
            // beforehand";
            transformer.clearParameters();
            for (Iterator iter = params.values().iterator(); iter.hasNext();) {
                XsltParam param = (XsltParam) iter.next();
                transformer.setParameter(param.getName(), param.getExpression());
            }
        }
    }

    /**
     * @return stylesheet or null
     */
    public File getStylesheet() {
        return stylesheet;
    }

    /**
     * Sets a new Stylesheet File, which in turn creates a new {@link Transformer}.<br/>
     * Note: you have to re-apply your parameters, as they get lost with the old
     * transformer.
     * 
     * @param stylesheet or null
     * @see   #setParameter(String, Object)
     */
    public void setStylesheet(File stylesheet) {
        this.stylesheet = stylesheet;
        isValidStylesheet = false;
    }

    /**
     * @throw IllegalArgumentException if param or param.getName() are null
     */
    public void setParameter(XsltParam param) {
        isValidParams = false;
        if ( param == null || param.getName() == null ) {
            throw new IllegalArgumentException("param and param.getName() must not be null: "+String.valueOf(param));
        }
        params.put(param.getName(),param);
    }
    
    
    

    public EntityResolver getEntitiyResolver() {
        return entitiyResolver;
    }
    public void setEntitiyResolver(EntityResolver entitiyResolver) {
        if ( entitiyResolver != this.entitiyResolver ) {
            this.isValidParser = false;
        }
        this.entitiyResolver = entitiyResolver;
    }

    /**
     * Configures parser for input documents.
     * Affects following features:
     * <ul>
     * <li>{@link #VALIDATION_FEATURE_ID         }
     * <li>{@link #SCHEMA_VALIDATION_FEATURE_ID  }
     * </ul>  
     * Default: false  
     * @see   #validate()
     */
    public boolean isValidate() {
        return isValidate;
    }
    /**
     * Configures parser for input documents.
     * Affects following features:
     * <ul>
     * <li>{@link #VALIDATION_FEATURE_ID         }
     * <li>{@link #SCHEMA_VALIDATION_FEATURE_ID  }
     * </ul><br>
     * Default: false  
     * @see   #validate()
     */
    public void setValidate(boolean validate) {
        if ( validate != this.isValidate ) {
            this.isValidParser = false;
        }
        this.isValidate = validate;
    }

    /**
     * Configures parser for input documents.
     * Affects following features:
     * <ul>
     * <li>{@link #DYNAMIC_VALIDATION_FEATURE_ID }
     * </ul><br> 
     * Default: false  
     * @see   #validate()
     */
    public boolean isValidateDynamic() {
        return isValidateDynamic;
    }
    /**
     * Configures parser for input documents.
     * Affects following features:
     * <ul>
     * <li>{@link #DYNAMIC_VALIDATION_FEATURE_ID }
     * </ul><br>  
     * Default: false  
     * @see   #validate()
     */
    public void setValidateDynamic(boolean validateDynamic) {
        if ( validateDynamic != this.isValidateDynamic && this.isValidate == true) {
            this.isValidParser = false;
        }
        this.isValidateDynamic = validateDynamic;
    }

    /**
     * Configures parser for input documents.
     * Affects following features:
     * <ul>
     * <li>{@link #NAMESPACES_FEATURE_ID         }
     * </ul><br>  
     * Default: true  
     * @see   #validate()
     */
    public boolean isNamespaceAware() {
        return isNamespaceAware;
    }
    /**
     * Configures parser for input documents.
     * Affects following features:
     * <ul>
     * <li>{@link #NAMESPACES_FEATURE_ID         }
     * </ul><br>  
     * Default: true  
     * @see   #validate()
     */
    public void setNamespaceAware(boolean isNamespaceAware) {
        if ( isNamespaceAware != this.isNamespaceAware ) {
            this.isValidParser = false;
        }
        this.isNamespaceAware = isNamespaceAware;
    }

    public void clearParameters() {
        isValidParams = false;
        params.clear();
    }

    public Project getProject() {
        return project;
    }
    protected void setProject(Project project) {
        this.project = project;
    }

    public String toString() {
        return shortClassname(getClass().getName())+"[transformer="+transformer+"; isValidate="+isValidate+" isValidateDynamic="+isValidateDynamic+"; isNamespaceAware="+isNamespaceAware+"; params="+params+"]";
    }

    //
    //  Helper methods
    //

    /**
     * @return classname without package prefix
     */
    public static String shortClassname(String classname) {
        try {
            int idx = classname.lastIndexOf('.');
            if (idx >= 0) {
                classname = classname.substring(idx + 1, classname.length());
            }
        } catch (IndexOutOfBoundsException e) {
            // This should never happen
            e.printStackTrace();
        }
        return classname;
    }

    public boolean isCacheStylesheet() {
        return cacheStylesheet;
    }

    public void setCacheStylesheet(boolean cacheStylesheet) {
        if (cacheStylesheet == false) {
            isValidStylesheet = false;
            isValidParams = false;
        }
        this.cacheStylesheet = cacheStylesheet;
    }

    //-- xslt extensions
    // TODO: move into separate class?
    
    public static boolean exists(String file) {
    	return new File(file).exists();
    }
}
