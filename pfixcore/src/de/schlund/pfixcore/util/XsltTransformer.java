/*
 * Created on Sep 19, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.util;


import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xslt;
import java.io.File;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.apache.tools.ant.BuildException;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XsltTransformer {

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

    /**
     * The Stylesheet needs to be set with {@link #setStylesheet(File)} when using the
     * default constructor.
     */
    public XsltTransformer() {
        this(null);
    }

    /**
     * @param transformerFactory if null, {@link TransformerFactory#newInstance()} is used
     * @param stylesheet may be null
     */
    public XsltTransformer(File stylesheet) {
        setStylesheet(stylesheet);
        factory = TransformerFactory.newInstance();
    }

    public void transform(File baseDir, String infilename, File destDir, String outfilename) {
        transform(new File(baseDir, infilename), new File(destDir, outfilename));
    }

    public void transform(File infile, File outfile) {
        StreamSource source = new StreamSource(infile);
        StreamResult result = new StreamResult(outfile);
        transform(source, result);
    }

    public void transform(Source source, Result result) {
        if (isValidParams == false || isValidStylesheet == false) {
            validate();
        }
        try {
            transformer.transform(source, result);
        } catch (TransformerException tex) {
            throw new BuildException("Transformation from source=" + source + " to result=" + result + " failed.", tex);
        }
    }

    /**
     * 
     * @throws BuildException
     */
    
    public void validate() {
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
        if (isValidParams == false) {
            // assert transformer != null : "Exception should have been thrown beforehand";
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

    public void clearParameters() {
        isValidParams = false;
        params.clear();
    }

    public String toString() {
        // TODO_AH throw out debug in XsltGenericTask or cache this String?
        String transformerString = (transformer == null) ? "<unused>" : String.valueOf(transformer); 
        return shortClassname(getClass().getName())+"[transformer="+transformer+"; params="+params+"]";
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
}
