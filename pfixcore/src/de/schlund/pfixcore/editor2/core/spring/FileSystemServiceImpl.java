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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
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

}
