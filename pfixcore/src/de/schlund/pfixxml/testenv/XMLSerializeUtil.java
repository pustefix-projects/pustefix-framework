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

package de.schlund.pfixxml.testenv;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;


/**
 * Utility class for serializing XML-documents.
 * </br>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class XMLSerializeUtil {

    //~ Instance/static variables ..................................................................

    private static XMLSerializeUtil theInstance = new XMLSerializeUtil();

    //~ Methods ....................................................................................

    public static XMLSerializeUtil getInstance() {
        return theInstance;
    }

    /** 
     * Serialize a XML document to a string.
     * @param the document to serialize
     * @return a String containing the XML document as text
     * @throws IOException on all IO errors.
     */
    public String serializeToString(Document doc) throws IOException {
        if (doc == null) {
            throw new IllegalArgumentException("The parameter 'null' is not allowed here! "
                                               + "Can't serialize a " + doc
                                               + " document to a string!");
        }
        XMLSerializer ser        = new XMLSerializer();
        OutputFormat  out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        ser.setOutputFormat(out_format);
        StringWriter string_writer = new StringWriter();
        ser.setOutputCharStream(string_writer);
        ser.serialize(doc);
        return string_writer.getBuffer().toString();
    }

    /** 
     * Serialize a XML document to a specified filename. 
     * @param the document to serialize
     * @param the target filename
     * @throws FileNotFoundException if target is not a regular file, IOException on
     * all IO errors.
     */
    public void serializeToFile(Document doc, String path, int indent, boolean preserve_space) throws FileNotFoundException, IOException {
        if (doc == null) {
            throw new IllegalArgumentException("The parameter 'null' is not allowed here! "
                                               + "Can't serialize a " + doc
                                               + " document to a file!");
        }
        if (path == null || path.equals("")) {
            throw new IllegalArgumentException("The parameter 'null' or '\"\"' is not allowed here! "
                                               + "Can't serialize a document to " + path + "!");
        }
        OutputFormat out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(indent);
        out_format.setPreserveSpace(preserve_space);
        FileOutputStream out_stream = null;
        out_stream = new FileOutputStream(path);
        XMLSerializer ser = new XMLSerializer(out_stream, out_format);
        ser.serialize(doc);
    }
}