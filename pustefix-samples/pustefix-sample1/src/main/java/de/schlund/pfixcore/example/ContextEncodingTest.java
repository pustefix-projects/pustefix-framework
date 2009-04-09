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
package de.schlund.pfixcore.example;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author mleidig@schlund.de
 */
public class ContextEncodingTest {

    private final static String DEFAULT_TEXT = "abcd����";
    private String              text         = DEFAULT_TEXT;
    private String              encoding     = "";
    private File                file;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @InsertStatus
    public void serializeToXML(Element elem) {
        ResultDocument.addTextChild(elem, "encoding", encoding);
        ResultDocument.addTextChild(elem, "original", text);
        if (file != null) ResultDocument.addTextChild(elem, "file", file.getAbsolutePath());
        try {
            String utfEnc = URLEncoder.encode(text, "UTF-8");
            ResultDocument.addTextChild(elem, "urlenc-utf", utfEnc);
            String isoEnc = URLEncoder.encode(text, "ISO-8859-1");
            ResultDocument.addTextChild(elem, "urlenc-iso", isoEnc);
        } catch (UnsupportedEncodingException x) {}
        ResultDocument.addObject(elem, "alphabet", new RussianAlphabet());
    }

    public class RussianAlphabet {

        private String       description;
        private List<String> characters;

        public RussianAlphabet() {
            description = "Basic Russian Alphabet";
            characters = new ArrayList<String>();
            for (char ch = '\u0410'; ch < '\u0430'; ch++) {
                characters.add(ch + " " + Character.toLowerCase(ch));
            }
        }

        public String getDescription() {
            return description;
        }

        public List<String> getCharacters() {
            return characters;
        }
    }
}
