/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.pustefixframework.editor.common.exception.EditorIOException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;
import de.schlund.pfixxml.util.Xml;

/**
 * Implementation of FileSystemService.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class FileSystemServiceImpl implements FileSystemService {
    private HashMap<File, Object> locks;

    public FileSystemServiceImpl() {
        this.locks = new HashMap<File, Object>();
    }

    public Object getLock(File file) {
        synchronized (this.locks) {
            if (this.locks.containsKey(file)) {
                return this.locks.get(file);
            } else {
                Object lock = new Object();
                this.locks.put(file, lock);
                return lock;
            }
        }
    }

    public Document readXMLDocumentFromFile(File file)
            throws FileNotFoundException, SAXException, IOException {
        /*
         * DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         * dbf.setCoalescing(true); dbf.setNamespaceAware(true); DocumentBuilder
         * builder = dbf.newDocumentBuilder(); return builder.parse(new
         * FileInputStream(file));
         */
        return Xml.parseMutable(file);
    }
    
    public Document readCustomizedXMLDocumentFromFile(FileResource file, String namespace) throws FileNotFoundException, SAXException, IOException {
        DOMResult result = new DOMResult();
        try {
            customize(new InputSource(file.getInputStream()), result, namespace);
        } catch (TransformerException e) {
            throw new SAXException(e);
        }
        if (result.getNode().getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) result.getNode();
        } else {
            return result.getNode().getOwnerDocument();
        }
    }

    public void storeXMLDocumentToFile(File file, Document document)
            throws IOException {
        Xml.serialize(document, file, false, true);
    }

    public void makeDirectory(File directory, boolean makeParentDirectories)
            throws EditorIOException {
        if (directory.exists()) {
            throw new EditorIOException("Cannot create existing directory "
                    + directory.getPath() + "!");
        }
        boolean ret;
        if (makeParentDirectories) {
            ret = directory.mkdirs();
        } else {
            ret = directory.mkdir();
        }
        if (!ret) {
            String msg = "Could not create directory " + directory.getPath()
                    + "!";
            throw new EditorIOException(msg);
        }
    }

    public void copy(File source, File target) throws EditorIOException {
        try {
            if (!target.exists()) {
                target.createNewFile();
            }
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(target);
            byte[] buf = new byte[1024];
            int l;
            while ((l = fis.read(buf)) != -1) {
                fos.write(buf, 0, l);
            }
            fis.close();
            fos.close();
        } catch (IOException e) {
            String err = "Could not copy file \"" + source.getPath()
                    + "\" to \"" + target.getPath() + "\"!";
            throw new EditorIOException(err, e);
        }
    }
    
    private static void customize(InputSource input, Result result, String namespace) throws FileNotFoundException, TransformerException {
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
            DefaultHandler cushandler;
            if (namespace == null) {
                cushandler = new CustomizationHandler(dh);
            } else {
                cushandler = new CustomizationHandler(dh, namespace);
            }
            xreader.setContentHandler(cushandler);
            xreader.setDTDHandler(cushandler);
            xreader.setErrorHandler(cushandler);
            xreader.setEntityResolver(cushandler);

            try {
                xreader.parse(input);
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


}
