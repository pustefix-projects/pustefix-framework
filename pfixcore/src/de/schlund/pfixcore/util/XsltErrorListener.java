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

import java.io.PrintStream;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMLocator;

import org.apache.tools.ant.Project;
import org.xml.sax.SAXException;


/**
 * This class is based on {@link com.icl.saxon.StandardErrorListener}.
 * Unlike its anchestor, it prints out the location of error in emacs syntax.
 * @author adam
 */
public class XsltErrorListener implements ErrorListener {
    
    public final static int RECOVER_SILENTLY = 0;
    public final static int RECOVER_WITH_WARNINGS = 1;
    public final static int DO_NOT_RECOVER = 2;

    protected int recoveryPolicy = RECOVER_WITH_WARNINGS;
    protected int warningCount = 0;
    protected PrintStream errorOutput = System.out;
    
    /**
     * may be null. Not used at the moment. Would be used for log().
     */
    protected Project project = null;
    
    public XsltErrorListener() {
        this(null);
    }   

    public XsltErrorListener(Project project) {
        setProject(project);
    }   
    
    /**
    * Set output destination for error messages (default is System.err)
    * @param writer The PrintStream to use for error messages
    */
    public void setErrorOutput(PrintStream writer) {
        errorOutput = writer;
    }

    /**
    * Set the recovery policy
    */
    public void setRecoveryPolicy(int policy) {
        recoveryPolicy = policy;
    }

    /**
     * @return the project or null
     */
    public Project getProject() {
        return project;
    }
    protected void setProject(Project project) {
        this.project = project;
    }
    
    /**
     * Receive notification of a warning.
     *
     * <p>Transformers can use this method to report conditions that
     * are not errors or fatal errors.  The default behaviour is to
     * take no action.</p>
     *
     * <p>After invoking this method, the Transformer must continue with
     * the transformation. It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */
     
    public void warning(TransformerException exception)
        throws TransformerException {

        if (recoveryPolicy==RECOVER_SILENTLY) {
            // do nothing
            return;            

        }
        String message = "";
        if (exception.getLocator()!=null) {
            message = getLocationMessage(exception) + "\n  ";
        } 
        message += getExpandedMessage(exception);
        
        if (recoveryPolicy==RECOVER_WITH_WARNINGS) {
            errorOutput.println("Recoverable error");
            errorOutput.println(message);
            warningCount++;
            if (warningCount > 25) {
                System.err.println("No more warnings will be displayed");
                recoveryPolicy = RECOVER_SILENTLY;
                warningCount = 0;
            }
        } else {
            errorOutput.println("Recoverable error");
            errorOutput.println(message);
            errorOutput.println("Processing terminated because error recovery is disabled");
            throw new TransformerException(exception);
        }
    }

    /**
     * Receive notification of a recoverable error.
     *
     * <p>The transformer must continue to provide normal parsing events
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * <p>The action of the standard error listener depends on the
     * recovery policy that has been set, which may be one of RECOVER_SILENTLY,
     * RECOVER_WITH_WARNING, or DO_NOT_RECOVER
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */
     
    public void error(TransformerException exception) throws TransformerException {
        String message = //"Error " +
                         getLocationMessage(exception) +
                         //"\n  " +
                         getExpandedMessage(exception);
        errorOutput.println(message);
    }

    /**
     * Receive notification of a non-recoverable error.
     *
     * <p>The application must assume that the transformation cannot
     * continue after the Transformer has invoked this method,
     * and should continue (if at all) only to collect
     * addition error messages. In fact, Transformers are free
     * to stop reporting events once this method has been invoked.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */
     
    public void fatalError(TransformerException exception) throws TransformerException {
        error(exception);
        throw exception;
    }

    /**
    * Get a string identifying the location of an error.
    */
    public static String getLocationMessage(TransformerException err) {
        SourceLocator loc = err.getLocator();
        if (loc==null) {
            return "";
        } else {
            String locmessage = "";
            String systemId = loc.getSystemId();
            if ( systemId != null ) {
                if ( systemId.startsWith("file:") ) {
                    systemId = systemId.substring(5);
                }
                if ( systemId.startsWith("///") ) {
                    systemId = systemId.substring(2);
                }
            }
            if (loc instanceof DOMLocator) {
                // location is a node
                locmessage += systemId + "[";
                locmessage += ((DOMLocator)loc).getOriginatingNode().getNodeName() + "]: ";
            }
            int line = loc.getLineNumber();
            int column = loc.getColumnNumber();
            String lineColumn = "";
            if (line<0 && column>0) {
                // location is a specific byte
                lineColumn += "at byte " + column + ": ";
            } else {
                // location is line
                lineColumn += line + ":";
                if (loc.getColumnNumber() != -1) {
                    lineColumn += column + ":";
                } else {
                    // column unknown, lets take "1"
                    lineColumn += "1:";
                }
            }
            locmessage += systemId + ":";
            locmessage += lineColumn;
            locmessage += " ";
            return locmessage;
        }
    }
    
    /**
    * Get a string containing the message for this exception and all contained exceptions
    */
    public static String getExpandedMessage(TransformerException err) {
        String message = "";
        Throwable e = err;
        while (true) {
            if (e == null) {
                break;
            }
            String next = e.getMessage();
            if (!next.equals("TRaX Transform Exception") && !message.endsWith(next)) {
                if (!message.equals("")) {
                    message += ": ";
                }
                message += e.getMessage();
            }
            if (e instanceof TransformerException) {
                e = ((TransformerException)e).getException();
            } else if (e instanceof SAXException) {
                e = ((SAXException)e).getException();
            } else {
                break;
            }
        }
        
        return message;
    }        
}
