/*
 * Created on Sep 18, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.util;

import java.io.File;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;

import com.icl.saxon.TransformerFactoryImpl;

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

    /** Classpath to use when trying to load the XSL processor */
    protected Path classpath = null;

    // --- other members --- 

    /**
     *   
     */
    protected XsltTransformer transformer;

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
    public void execute() throws BuildException {

        try {
            executeSetup();
            StringBuffer sb = new StringBuffer(300);
            sb.append("style=\"");
            sb.append(stylefile);
            sb.append("\" srcdir=\"");
            sb.append(srcdirResolved);
            sb.append("\" destdir=");
            sb.append(destdirResolved);
            sb.append("\" using " + getTransformer());
            log(sb.toString(), Project.MSG_DEBUG);
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
        int count;
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
                uptodate = candidates;
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
                    count = doTransformationMaybe();
                    transformed = transformed + count;
                    uptodate = uptodate - count;
                }
            }
        } finally {
            // log only if not everything was uptodate
            if ( candidates != uptodate ) {
                log("Transformed "+transformed+" of "+candidates+" file"+(candidates==1 ? "" : "s")+", "+uptodate+" "+(uptodate==1 ? "was" : "were")+" up to date", Project.MSG_INFO);
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

        // TODO throw out? transformer is expensive
        // transformer is not yet initialized, as it only initializes itself
        // on transformation, therefore the firsttime transformer.toString()
        // is called, the internal transformer is null
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
        if ( transformer == null ) {
            TransformerFactory transformerFactory = loadTransformerFactory(getProject());
            transformer = new XsltTransformer(transformerFactory);
        }
        return transformer;
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
     * adds a configures instance of an XSLT parameter
     *
     * @param param a configured instance of the {@link XsltParam}
     */
    public void addConfiguredParam(XsltParam param) {
        getTransformer().setParameter(param);
    }
    
    // --- Helper methods ---
    protected static boolean isNothing(String s) {
        return (s == null) || (s.trim().length() == 0);
    }
    
    
    /**
     * @param  project needed for logging, if null logging to System.out
     * @throws BuildException
     */
    public static TransformerFactory loadTransformerFactory(Project project) {
        TransformerFactory transformerFactory;
        // using dynamic class loading to keep the ant task compilation
        // independent of additional jars
        Class factoryClass = null;
        try {
            factoryClass = Class.forName("com.icl.saxon.TransformerFactoryImpl");
        } catch (ClassNotFoundException e) {
            throw new BuildException("Could not load Saxon via Class.forName(), check classpath", e);
        }
        try {
            transformerFactory = (TransformerFactory) factoryClass.newInstance();
        } catch (Exception e) {
            throw new BuildException("Could not instantiate Saxon", e);
        }
        return transformerFactory;
    }

    /**
     * @param  project needed for logging, if null logging to System.out
     * @throws BuildException
     */
    public static DocumentBuilderFactory loadDocumentBuilderFactory(Project project) {
        DocumentBuilderFactory documentBuilderFactory = null;
        // using dynamic class loading to keep the ant task compilation
        // independent of additional jars
        Class factoryClass = null;
        Exception ex = null;
        try {
            factoryClass = Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl");
            documentBuilderFactory = (DocumentBuilderFactory) factoryClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BuildException("Could not load Xerces via Class.forName(), check classpath", e);
        } catch (Exception e) {
            throw new BuildException("Could not instantiate Xerces", e);
        }
        return documentBuilderFactory;
    }

}
