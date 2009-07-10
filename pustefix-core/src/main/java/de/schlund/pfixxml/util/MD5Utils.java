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
package de.schlund.pfixxml.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * Created on 05.07.2004
 */

/**
 * @author Niels Schelbach
 * 05.07.2004
 */
public class MD5Utils {
    
    private final static Logger logger = Logger.getLogger(MD5Utils.class);
 
    
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_LATIN1 = "ISO-8859-1";
    
    /*
     * Convert an array of little-endian words to a hex string.
     */
    public static String byteToHex(byte[] raw)
    {
      String hex_tab = "0123456789abcdef";
      StringBuffer sb = new StringBuffer();
      for(int i=0;i<raw.length; i++)
      {
          byte b = raw[i];
          sb.append(hex_tab.charAt((b & 0xF0) >> 4));
          sb.append(hex_tab.charAt(b & 0xF));
      }
      return sb.toString();
    }
    
    public static String hex_md5(String message){ 
        return hex_md5(message,CHARSET_LATIN1);
    }
    
    
    public static String hex_md5(String message, String charset){ 
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] raw = md.digest(new String(message).getBytes(charset));
            result = byteToHex(raw);
        }
        catch (NoSuchAlgorithmException ex) {
            logger.error("this should not happen!",ex);
            throw new RuntimeException("No Such Algorithm",ex);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("this should not happen!",ex);
            throw new RuntimeException("Unsupported Charset",ex);
        }
        return result;
    }

    public static String hex_md5(InputStream inputStream) throws IOException{ 
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(inputStream, md);
            byte[] tempBuffer = new byte[8192];
            while (dis.read(tempBuffer) != -1) {
                // Action is performed in condition
            }
            byte[] raw = md.digest();
            result = byteToHex(raw);
        }
        catch (NoSuchAlgorithmException ex) {
            logger.error("this should not happen!",ex);
            throw new RuntimeException("No Such Algorithm",ex);
        }
        catch (UnsupportedEncodingException ex) {
            logger.error("this should not happen!",ex);
            throw new RuntimeException("Unsupported Charset",ex);
        }
        return result;
    }
    
    public static String hex_md5(Node node) {
        String val = serializeNode(node);
        return hex_md5(val, "UTF-8");
    }
    
    private static String serializeNode(Node node) {
        StringBuffer buffer = new StringBuffer();
        String nodeType = null;
        Short nodeTypeNum = node.getNodeType();
        switch (nodeTypeNum) {
        case Node.ATTRIBUTE_NODE:
            nodeType = "ATTRIBUTE";
            break;
        case Node.CDATA_SECTION_NODE:
            nodeType = "CDATA";
            break;
        case Node.COMMENT_NODE:  
            nodeType = "COMMENT";
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            nodeType = "FRAGMENT";
            break;
        case Node.DOCUMENT_NODE:
            nodeType = "DOCUMENT";
            break;
        case Node.DOCUMENT_TYPE_NODE:
            nodeType = "DOCUMENT_TYPE";
            break;
        case Node.ELEMENT_NODE:
            nodeType = "ELEMENT";
            break;
        case Node.ENTITY_NODE:
            nodeType = "ENTITY";
            break;
        case Node.ENTITY_REFERENCE_NODE:
            nodeType = "ENTITY_REFERENCE";
            break;
        case Node.NOTATION_NODE:
            nodeType = "NOTATION";
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            nodeType = "PROCESSING_INSTRUCTION";
            break;
        case Node.TEXT_NODE:
            nodeType = "TEXT";
            break;
        default:
            throw new RuntimeException("Unexpected node type!");
        }
        buffer.append(nodeType);
        buffer.append('[');
        String nodeNamespace = node.getNamespaceURI();
        if (nodeNamespace != null) {
            buffer.append("NAMESPACE[\"");
            buffer.append(nodeNamespace);
            buffer.append("\"]");
        }
        String nodeName = node.getNodeName();
        if (nodeName != null) {
            buffer.append("NAME[\"");
            buffer.append(nodeName);
            buffer.append("\"]");
        }
        String nodeValue = node.getNodeValue();
        if (nodeValue != null) {
            buffer.append("VALUE[\"");
            buffer.append(nodeValue);
            buffer.append("\"]");
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            buffer.append(serializeNode(children.item(i)));
        }
        buffer.append(']');
        return buffer.toString();
    }
}
