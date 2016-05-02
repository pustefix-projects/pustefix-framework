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

package org.pustefixframework.webservices.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author mleidig@schlund.de
 */
public class XMLFormatter {

	final static String XSL=
		"<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"+
		"  <xsl:output method=\"xml\" indent=\"yes\"/>"+
		"  <xsl:strip-space elements=\"*\"/>"+
		"  <xsl:template match=\"/\">"+
		"    <xsl:copy-of select=\".\"/>"+
		"  </xsl:template>"+
		"</xsl:stylesheet>";

	static Templates templates;
	
	static synchronized Templates getTemplates() throws Exception {
		if(templates==null) {
			TransformerFactory tf=TransformerFactory.newInstance();
			templates=tf.newTemplates(new StreamSource(new StringReader(XSL)));
		}
		return templates;
	}
	
	public static String format(String message) {
		try {
			StringWriter sw=new StringWriter();
			Transformer t=getTemplates().newTransformer();
			t.transform(new StreamSource(new StringReader(message)),new StreamResult(sw));
			return sw.toString();
		} catch(Exception x) {
			//return unmodified message if formatting fails
			return message;
		}
	}
	
}
