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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Parser for gettext PO files.
 */
public class POReader {

    private Logger LOG = Logger.getLogger(POReader.class);
    
    private final Pattern PO_LINE_PATTERN = 
            Pattern.compile("^(msgctxt|msgid|msgid_plural|msgstr|msgstr\\[(\\d+)\\])?\\s*\"(.*)\"$");

    /**
     * Reads a PO file and returns the message data.
     * 
     * @param input  PO file data
     * @param encoding  Encoding of the PO file data
     * @return Message data
     * @throws IOException
     */
    public POData read(InputStream input, String encoding) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, encoding))) {
            List<POMessage> messages = new ArrayList<>();
            Map<String, String> headers = new HashMap<>();
            ParserContext context = null;    
            String lastCommand = null;
            String line = null;
            while((line = reader.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) {
                    lastCommand = null;
                } else {
                    if(line.startsWith("#")) {
                        if(!"#".equals(lastCommand)) {
                            context = flush(messages, headers, context);
                        }
                        lastCommand = "#";
                    } else {
                        Matcher matcher = PO_LINE_PATTERN.matcher(line);
                        if(matcher.matches()) {
                            String cmd = matcher.group(1);
                            String str = unescape(matcher.group(3));
                            if(cmd == null) {
                                if("msgid".equals(lastCommand)) {
                                    context.messageId += str;
                                } else if("msgstr".equals(lastCommand)) {
                                    context.messageStrings[context.messageStringIndex] += str;
                                }
                            } else if(cmd.equals("msgctxt")) {
                                if(!"#".equals(lastCommand)) {
                                    context = flush(messages, headers, context);
                                }
                                context.messageContext = str;
                            } else if(cmd.equals("msgid")) {
                                if(!("#".equals(lastCommand) || "msgctxt".equals(lastCommand))) {
                                    context = flush(messages, headers, context);
                                }
                                context.messageId = str;
                            } else if(cmd.equals("msgid_plural")) {
                                context.messageIdPlural = str;
                            } else if(cmd.equals("msgstr")) {
                                context.messageStrings = new String[] {str};
                                context.messageStringIndex = 0;
                            } else {
                                int ind = Integer.parseInt(matcher.group(2));
                                if(ind > context.messageStrings.length - 1) {
                                    context.messageStrings = Arrays.copyOf(context.messageStrings, ind + 1);
                                }
                                context.messageStrings[ind] = str;
                                context.messageStringIndex = ind;
                            }
                            if(cmd != null) {
                                lastCommand = cmd;
                            }
                        } else {
                            LOG.warn("Invalid PO format line: " + line);
                        }
                    }
                }
            }
            flush(messages, headers, context);
            return new POData(messages, headers);
        }
    }

    private ParserContext flush(List<POMessage> messages, Map<String, String> headers, ParserContext context) throws IOException {
        if(context != null && context.messageId != null) {
            if(context.messageId.isEmpty() && messages.isEmpty()) {
                
                parseHeader(context.messageStrings[0], headers);
            }
            for(int i = 0; i < context.messageStrings.length; i++) {
                if(context.messageStrings[i].contains("{")) {
                    context.messageStrings[i] = context.messageStrings[i].replace("\\'", "''");
                } else {
                    context.messageStrings[i] = context.messageStrings[i].replace("\\'", "'");
                }
            }
            POMessage message = new POMessage(context.messageContext, context.messageId, 
                    context.messageIdPlural, context.messageStrings);
            messages.add(message);
        }
        return new ParserContext();
    }

    private void parseHeader(String str, Map<String, String> headers) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(str));
        String line = null;
        while((line = reader.readLine()) != null) {
            int ind = line.indexOf(':');
            if(ind > -1) {
                String key = line.substring(0,  ind).trim();
                String val = line.substring(ind + 1).trim();
                headers.put(key, val);
            }
        }
    }

    String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);
            if(ch == '\\' && i < text.length() - 1) {
                i++;
                char next = text.charAt(i);
                switch(next) {
                    case 'n':  ch = '\n';
                               break;
                    case 't':  ch = '\t';
                               break;
                    case 'b':  ch = '\b';
                               break;
                    case 'r':  ch = '\r';
                               break;
                    case 'f':  ch = '\f';
                               break;
                    case '\\': ch = '\\';
                               break;
                    case '"':  ch = '"';
                               break;
                    default:   i--;
                               break;
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private class ParserContext {

        String messageContext = null;
        String messageId = null;
        String messageIdPlural = null;
        String[] messageStrings = new String[0];
        int messageStringIndex = 0;
    }

}
