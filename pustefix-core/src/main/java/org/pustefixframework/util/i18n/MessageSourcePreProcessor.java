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
package org.pustefixframework.util.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * MessageSource preprocessor supporting replacement of message substrings
 * using regular expressions.
 */
public class MessageSourcePreProcessor {

    private List<Replacement> replacements = new ArrayList<>();

    public static MessageSourcePreProcessor create(Element root) {
        MessageSourcePreProcessor processor = new MessageSourcePreProcessor();
        List<Element> elems = DomUtils.getChildElementsByTagName(root, "replace");
        for(Element elem: elems) {
            String regex = elem.getAttribute("pattern");
            Pattern pattern = Pattern.compile(regex);
            String value = elem.getAttribute("value");
            Replacement rep = new Replacement();
            rep.pattern = pattern;
            rep.value= value;
            processor.replacements.add(rep);
        }
        return processor;
    }

    public String process(String message) {
        for(Replacement replacement: replacements) {
            message = replacement.pattern.matcher(message).replaceAll(replacement.value);
        }
        return message;
    }

    public String[] process(String[] messages) {
        for(int i=0; i<messages.length; i++) {
            messages[i] = process(messages[i]);
        }
        return messages;
    }


    static class Replacement {

        Pattern pattern;
        String value;

    }

}
