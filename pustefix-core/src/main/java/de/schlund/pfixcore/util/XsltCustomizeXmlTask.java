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

package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;

/**
 * Transforms projects.xml.in to projects.xml, doing customization.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class XsltCustomizeXmlTask extends XsltGenericTask {

    private String docroot;
    
    private Set<XsltParam> params = new HashSet<XsltParam>();

    @Override
    protected void executeSetup() {
        super.executeSetup();
        
        // Initialize PathFactory, as it is needed by the CustomizationHandler
        if (docroot == null) {
            throw new BuildException("Attribute docroot not set!");
        }
        try {
            GlobalConfigurator.setDocroot(docroot);
        } catch (IllegalStateException e) {
            // Ignore exception as there is no problem
            // if the docroot has already been configured
        }
    }
    
    public String getDocroot() {
        return docroot;
    }
    
    public void setDocroot(String value) {
        this.docroot = value;
    }

    @Override
    public void addConfiguredParam(XsltParam param) {
        this.params.add(param);
    }

    @Override
    protected void doTransformation() throws BuildException {
        URIResolver customizationResolver = new CustomizationResolver();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer(new StreamSource(stylefile));
            trans.setURIResolver(customizationResolver);
            File temp;
            try {
                temp = File.createTempFile("temptransform", ".xml");
            } catch (IOException e) {
                throw new BuildException("Could not create temporary file", e);
            }
            temp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(temp);
            StreamResult sr = new StreamResult(fos);
            customize(in, sr);

            // Flush output to make sure it is avaiable for the
            // next transformation step
            fos.flush();
            fos.close();
            
            for (XsltParam param : params) {
                trans.setParameter(param.getName(), param.getExpression());
            }
            
            // Pass FileOutputStream instead of File to
            // circumvent a bug in certain Xalan versions
            trans.transform(new StreamSource(temp), new StreamResult(new FileOutputStream(out)));
            temp.delete();
        } catch (TransformerConfigurationException e) {
            throw new BuildException("Could not create transformer", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new BuildException("Could not create transformer", e);
        } catch (IOException e) {
            throw new BuildException("I/O-Error during transformation", e);
        } catch (TransformerException e) {
            throw new BuildException("Parsing of " + in + " failed!", e);
        }
    }

    private static void customize(File input, Result result) throws FileNotFoundException, TransformerException {
        XMLReader xreader;
        try {
            xreader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException("Could not create XMLReader", e);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            TransformerHandler th;
            try {
                th = stf.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException("Failed to configure TransformerFactory!", e);
            }

            th.setResult(result);
            DefaultHandler dh = new TransformerHandlerAdapter(th);
            DefaultHandler cushandler = new CustomizationHandler(dh);
            xreader.setContentHandler(cushandler);
            xreader.setDTDHandler(cushandler);
            xreader.setErrorHandler(cushandler);
            xreader.setEntityResolver(cushandler);

            try {
                xreader.parse(new InputSource(new FileInputStream(input)));
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException e) {
                throw new TransformerException(e);
            } catch (SAXException e) {
                throw new TransformerException(e);
            }
        } else {
            throw new RuntimeException("Could not get instance of SAXTransformerFactory!");
        }

    }

    private class CustomizationResolver implements URIResolver {

        public Source resolve(String href, String base)
                throws TransformerException {
            DOMResult dr = new DOMResult();
            try {
                customize(new File(href), dr);
            } catch (FileNotFoundException e) {
                return null;
            }
            return new DOMSource(dr.getNode());
        }

    }

}
