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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

/**
 * Representation of the PO file data, including message
 * entries and additional header information.
 */
public class POData {

    private Logger LOG = Logger.getLogger(POData.class);

    private Map<String, POMessage> messages;
    private PluralForms pluralForms;

    public POData(List<POMessage> messageList) {

        messages = new HashMap<>(messageList.size());
        for(POMessage message: messageList) {
            if(message.getMessageId() != null) {
                messages.put(message.getMessageId(), message);
            }
        }
    }

    public POData(List<POMessage> messageList, Map<String, String> headers) {

        this(messageList);
        String val = headers.get("Plural-Forms");
        if(val != null) {
            try {
                pluralForms = new PluralForms(val);
            } catch(ScriptException x) {
                LOG.warn("Can't compile plural form expression: " + val, x);
            }
        }
    }

    /**
     * Returns the translation of the msgid string.
     * 
     * @param msgid string to be translated
     * @return translation of the msgid string, or msgid if not available
     */
    public String getText(String msgid) {

        String text = findText(msgid);
        if(text == null) {
            return msgid;
        } else {
            return text;
        }
    }
    
    /**
     * Returns the translation of the msgid string.
     * 
     * @param msgid string to be translated
     * @return translation of the msgid string, or null if not available
     */
    public String findText(String msgid) {

        POMessage msg = messages.get(msgid);
        if(msg != null) {
            String[] text = msg.getMessageStrings();
            if(text.length > 0) {
                return text[0];
            }
        }
        return null;
    }

    /**
     * Returns the plural form translation of the msgid string.
     * 
     * @param msgid string to be translated
     * @param msgidPlural plural form of the string to be translated
     * @param n plural number
     * @return plural form translation of the msgid string, or msgid or msgidPlural if not available
     */
    public String getText(String msgid, String msgidPlural, int n) {

        POMessage msg = messages.get(msgid);
        if(msg != null) {
            int ind = n;
            if(pluralForms == null) {
                if(n == 1) {
                    ind = 0;
                } else {
                    ind = 1;
                }
            } else {
                try {
                    ind = pluralForms.getIndex(n);
                } catch(ScriptException x) {
                    LOG.warn("Can't get index from plural form", x);
                }
            }
            String[] text = msg.getMessageStrings();
            if(text.length > ind) {
                if(text[ind] != null) {
                    return text[ind];
                }
            }
        }
        return n == 1 ? msgid : msgidPlural;
    }

    static class PluralForms {

        CompiledScript script;

        PluralForms(String expression) throws ScriptException {

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");
            script = ((Compilable)engine).compile(expression);
        }

        public synchronized int getIndex(int n) throws ScriptException {

            Bindings bindings = new SimpleBindings();
            bindings.put("n", n);
            Object result = script.eval(bindings);
            if(result instanceof Boolean) {
                if(result == Boolean.TRUE) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return (int)result;
        }

    }

}

