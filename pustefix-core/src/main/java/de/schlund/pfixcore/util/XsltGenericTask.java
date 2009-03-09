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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XsltGenericTask extends MatchingTask {

    // --- Attribute names ---
    public static String ATTR_SRCDIR = "srcdir";
    public static String ATTR_DESTDIR = "destdir";
    public static String ATTR_INFILE = "infile";
    public static String ATTR_OUTFILE = "outfile";
    public static String ATTR_CATALOGFILE = "catalogfile";
    public static String ATTR_VALIDATE = "validate";
    
    // --- Attribute values ---
    public static String VAL_VALIDATE_TRUE = "true"; 
    public static String VAL_VALIDATE_DYNAMIC = "dynamic";
    
    // --- Attributes ---

    /** destination directory */
    protected File destdir = null;

    /** where to find the source XML file, default is the project's basedir */
    protected File srcdir = null;

    /** single inputfile */
    protected File infile = null;

    /** single outputfile */
    protected File outfile = null;

    /** XSL stylesheet relative to project's basedir */
    protected String style = null;

    /** extension of the files produced by XSL processing */
    protected String targetExtension = null;

    ///** Classpath to use when trying to load the XSL processor */
    //protected Path classpath = null;
    
    /** XML Schema Validation. Default: false. */
    protected String validate = "false";
    
    /** Catalog file compliant to http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd - optional */
    protected String catalogfile = null;

    // --- other members --- 

    /**
     *   
     */
    protected XsltTransformer transformer;

    /**
     * 
     */
    protected PfixXmlCatalogEntityResolver pfixResolver;
    
    /**
     * existent stylesheet (handled by {@link #executeSetup()})
     */
    protected File stylefile;

    /**
     * resolved from project's base directory (handled by {@link #executeSetup()})
     */
    protected File srcdirResolved;

    /**
     * resolved from project's base directory (handled by {@link #executeSetup()})
     */
    protected File destdirResolved;

    protected void executeSetup() {
        
        if ( infile != null ) {
            if ( getSrcdir() != null ) {
                log(ATTR_SRCDIR+"/"+ATTR_DESTDIR+" ignored as "+ATTR_INFILE+" specified as "+infile, Project.MSG_WARN);
            }
            if ( outfile == null ) {
                throw new BuildException("no correspondig "+ATTR_OUTFILE+" for "+ATTR_INFILE+"="+infile);
            }
        } else {
            // srcdir 
            srcdirResolved = getSrcdir();
            if (srcdirResolved == null) {
                throw new BuildException("neither "+ATTR_INFILE+" nor "+ATTR_SRCDIR+" set");
            }
            // destdir
            destdirResolved = getDestdir();
            if (destdirResolved == null) {
                destdirResolved = srcdirResolved;
                log("destdir attribute not specified, using value of srcdir attribute", Project.MSG_VERBOSE);
            }
        }
        // style
        String styleTmp = getStyle();
        if (styleTmp == null) {
            throw new BuildException("no stylesheet specified");
        } else {
            stylefile = getProject().resolveFile(styleTmp);
            if (stylefile.exists() == false) {
                throw new BuildException("stylesheet " + stylefile + " does not exist.");
            }
        }
        getTransformer().setStylesheet(stylefile);
    }
    protected void executeCleanup() {
        srcdirResolved = null;
        destdirResolved = null;
        stylefile = null;
        if ( isTransformerInstantiated() ) {
            getTransformer().clearParameters();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {

        try {
            executeSetup();
            StringBuffer tmp = new StringBuffer(300);
            tmp.append("style=\"");
            tmp.append(stylefile);
            tmp.append("\" srcdir=\"");
            tmp.append(srcdirResolved);
            tmp.append("\" destdir=");
            tmp.append(destdirResolved);
            tmp.append("\" using " + getTransformer());
            log(tmp.toString(), Project.MSG_DEBUG);
            // delme log("Transforming from directory \""+srcdirResolved+"\" to \""+destdirResolved+"\" with style \""+stylefile+"\" using "+transformer, Project.MSG_VERBOSE);        
            doTransformations();
        } finally {
            executeCleanup();
        }

    }

    // TEMP ssed -e 's/^.* \(.*\)\( =\|;\)/\1 = null;/'
    // Variables used in transformXXX() 
    protected DirectoryScanner scanner;
    protected String[] infilenames; /* input filenames relative to srcdirResolved */
    protected String inname; /* filename relative to srcdirResolved */
    protected String infilenameNoExt; /* filename relative to srcdirResolved without extension */
    protected String outname; /* filename relative to srcdirResolved */
    protected File in;
    protected File out;
    protected long inLastModified;
    protected long outLastModified;
    protected long styleLastModified;
    protected StringBuffer sb = new StringBuffer(300);

    /**
     * checks attributes, sets {@link #in}, {@link #out} and calls {@link #doTransformationMaybe()}
     * for each input file.    
     */
    protected void doTransformations() {

        int transformed = 0;
        int uptodate = 0;
        int candidates = 0;
        int count; // number of files transformed per doTransformationMaybe() call, 0 or 1
        int dotPos;
        
        try {
            if ( infile != null ) {
                candidates = 1;
                in = infile;
                out = outfile;
                transformed = doTransformationMaybe();
                uptodate = candidates - transformed;
            } else {
                scanner = getDirectoryScanner(srcdirResolved);
                infilenames = scanner.getIncludedFiles();
                candidates = infilenames.length;
                uptodate = 0;
                for (int i = 0; i < infilenames.length; i++) {
    
                    inname = infilenames[i];
                    in = new File(srcdirResolved, inname);
    
                    dotPos = inname.lastIndexOf('.');
                    if (dotPos > 0) {
                        infilenameNoExt = inname.substring(0, inname.lastIndexOf('.'));
                    } else {
                        infilenameNoExt = inname;
                    }
                    outname = infilenameNoExt + getExtension();
                    out = new File(destdirResolved, outname);
                    count = doTransformationMaybe(); // count is 0 or 1
                    transformed = transformed + count;
                    if ( count <= 0 ) {
                        uptodate++;
                    }
                }
            }
        } finally {

            // log only if not everything was uptodate
            if ( candidates != uptodate ) {
                log("Transformed "+transformed+" of "+candidates+" file"+(candidates==1 ? "" : "s")+", "+uptodate+" "+(uptodate==1 ? "has been" : "have been")+" up to date", Project.MSG_INFO);
            }
            scanner = null;
            infilenames = null; /* input filenames relative to srcdirResolved */
            inname = null; /* filename relative to srcdirResolved */
            infilenameNoExt = null; /* filename relative to srcdirResolved without extension */
            outname = null; /* filename relative to srcdirResolved */
            in = null;
            out = null;
            inLastModified = 0;
            outLastModified = 0;
            styleLastModified = 0;
        }
    }
    
    /**
     * must be initialized: {@link #in}, {@link #out},
     *
     * @return number of files transformed,
     *         either 1 if file has been transformed or 0 if it was up to date   
     */
    protected int doTransformationMaybe() {
        inLastModified = in.lastModified();
        outLastModified = out.lastModified();
        styleLastModified = getStyleLastModified();

        // TODO_AH throw out? transformer is expensive
        // transformer is not yet initialized, as it only initializes itself
        // on transformation, therefore the firsttime transformer.toString()
        // is called, the internal transformer variable of XsltTransformer
        // is still null
        sb.setLength(0);
        sb.append("styleLastModified=");
        sb.append(new Date(styleLastModified));
        sb.append(" inLastModified=");
        sb.append(new Date(inLastModified));
        sb.append(" outLastModified=");
        sb.append(new Date(outLastModified));
        sb.append(" in=");
        sb.append(in);
        sb.append(" out=");
        sb.append(out);
        sb.append(" transformer=");
        sb.append(getTransformer());
        log(sb.toString(), Project.MSG_DEBUG);
        if ((outLastModified < styleLastModified) || (outLastModified < inLastModified)) {
            sb.setLength(0);
            sb.append("transform ");
            sb.append(in);
            sb.append(" ===> ");
            sb.append(out);
            log(sb.toString(), Project.MSG_VERBOSE);
            doTransformation();
            return 1;
        } else {
            sb.setLength(0);
            sb.append("uptodate  ");
            sb.append(in);
            sb.append(" <==> ");
            sb.append(out);
            log(sb.toString(), Project.MSG_VERBOSE);
            return 0;
        }
    }

    protected void doTransformation() throws BuildException {
        getTransformer().transform(in, out);
    }
   
    /**
     * @return lastModified time of {@link #stylefile}, 0 if {@link #stylefile} not set
     * @see File#lastModified()
     */
    protected long getStyleLastModified() {
        long lm = 0;
        if (stylefile != null) {
            lm = stylefile.lastModified();
        }
        return lm;
    }

    /**
     * Used to check if cleanup is necessary. 
     */
    protected boolean isTransformerInstantiated() {
        return transformer != null;
    }
    protected XsltTransformer getTransformer() {
        // TODO set dynamic val
        if ( transformer == null ) {
            transformer = new XsltTransformer(getProject());
            transformer.setValidate(isValidate());
            transformer.setValidateDynamic(isValidateDynamic()); // Xerces-Default: false
            //transformer.setNamespaceAware(true);               // Xerces-Default: true
        }
        if ( pfixResolver == null ) {
            if ( getCatalogfile() != null ) {
                try {
                    pfixResolver = new PfixXmlCatalogEntityResolver(getCatalogfile());
                } catch (MalformedURLException e) {
                    throw new BuildException("Unable to initialize PfixXmlCatalogEntityResolver with catalogfile \""+getCatalogfile()+"\"",e);
                } catch (IOException e) {
                    throw new BuildException("Unable to initialize PfixXmlCatalogEntityResolver with catalogfile \""+getCatalogfile()+"\"",e);
                }
                transformer.setEntitiyResolver(pfixResolver);
            }
        }
        return transformer;
    }
    protected void invalidateTransformer() {
        transformer = null;
    }
    protected void invalidateXmlCatalog() {
        pfixResolver = null;
    }
    
    public File getInfile() {
        return infile;
    }

    public void setInfile(File infile) {
        this.infile = infile;
    }

    public File getOutfile() {
        return outfile;
    }

    public void setOutfile(File outfile) {
        this.outfile = outfile;
    }

    /**
     * Set the Source directory;
     * required.
     *
     * @param srcdir the base directory
     **/
    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }
    public File getSrcdir() {
        return this.srcdir;
    }

    /**
     * Set the destination directory into which the XSL result
     * files should be copied to;
     * optional, defaults to srcdir if not specified
     * 
     * @param destdir the name of the destination directory
     **/
    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }
    public File getDestdir() {
        return destdir;
    }

    /**
     * Set the desired file extension to be used for the target;
     * optional, default is "".
     * 
     * @param name the extension to use
     **/
    public void setExtension(String name) {
        targetExtension = name;
    }
    /**
     * @return the target extension or "" if none set
     */

    public String getExtension() {
        return (targetExtension == null) ? "" : targetExtension;
    }

    /**
     * Name of the stylesheet to use - given either relative
     * to the project's basedir or as an absolute path; required.
     *
     * @param style the stylesheet to use
     */
    public void setStyle(String style) {
        this.style = style;
    }
    public String getStyle() {
        return style;
    }

    /**
     * adds a configured instance of an XSLT parameter
     *
     * @param param a configured instance of the {@link XsltParam}
     */
    public void addConfiguredParam(XsltParam param) {
        getTransformer().setParameter(param);
    }
    
    public boolean isValidate() {
        return getValidate().equals(VAL_VALIDATE_TRUE) ||
               getValidate().equals(VAL_VALIDATE_DYNAMIC);
    }
    public boolean isValidateDynamic() {
        return getValidate().equals(VAL_VALIDATE_DYNAMIC);
    }
    public String getValidate() {
        return validate;
    }
    public void setValidate(String validate) {
        if (validate == null) {
            throw new IllegalArgumentException("attribute "+ATTR_VALIDATE+" is null; has to be specified as "+VAL_VALIDATE_TRUE+"|false|"+VAL_VALIDATE_DYNAMIC);
        }
        if ( this.validate.equals(validate) == false) {
            invalidateTransformer();
        }
        this.validate = validate;
    }
    public String getCatalogfile() {
        return catalogfile;
    }
    public void setCatalogfile(String catalogfile) {
        if ( XsltGenericTask.equals(this.catalogfile, catalogfile) == false ) {
            invalidateXmlCatalog();
        }
        this.catalogfile = catalogfile;
    }

    // --- Helper methods ---
    protected static boolean isNothing(String s) {
        return (s == null) || (s.trim().length() == 0);
    }
    
    /**
     * Test for equality of {@param object1} and {@param object2}. 
     * @param object1, may be null
     * @param object2. may be null
     * @return true if {@param object1} and {@param object2} are equal
     */
    protected static boolean equals(Object object1, Object object2) {
        if ( object1 != null ) {
            return object1.equals(object2);
        } else {
            // o1 == null
            if ( object2 != null ) {
                return object2.equals(object1);
            } else {
                // o1 == null && o2 == null
                return true;
            }
        }
    }
    
}
