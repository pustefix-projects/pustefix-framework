package org.pustefixframework.util.firedebug;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * FireDebug
 * 
 * FireDebug is an implementation of FirePHP for Java.
 * It enables you to send log messages to Firefox and display them via FireBug.
 * You can find more informations about FirePHP at http://www.firephp.org.
 * 
 * @author Holger Rüprich
 */

public class FireDebugImpl implements FireDebug {

    private JSONSerializer serializer;
    private ArrayList<Message> dumpMessages = new ArrayList<Message>();
    private ArrayList<Message> consoleMessages = new ArrayList<Message>();

    public void log(Object message) {
        addMessage(message, Message.LOG);
    }

    public void log(Object message, String label) {
        addMessage(message, label, Message.LOG);
    }

    public void info(Object message) {
        addMessage(message, Message.INFO);
    }

    public void info(Object message, String label) {
        addMessage(message, label, Message.INFO);
    }

    public void error(Object message) {
        addMessage(message, Message.ERROR);
    }

    public void error(Object message, String label) {
        addMessage(message, label, Message.ERROR);
    }

    public void warn(Object message) {
        addMessage(message, Message.WARN);
    }

    public void warn(Object message, String label) {
        addMessage(message, label, Message.WARN);
    }

    public void dump(Object variable, String label) {
        dumpMessages.add(new Message(variable, label, Message.DUMP, serializer));
    }

    private void addMessage(Object object, String type) {
        consoleMessages.add(new Message(object, type, serializer));
    }

    private void addMessage(Object object, String label, String type) {
        consoleMessages.add(new Message(object, label, type, serializer));
    }

    public void setJSONSerializer(JSONSerializer serializer) {
        this.serializer = serializer;
    }

    public HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-FirePHP-Data-100000000001", "{");

        if (dumpMessages.size() > 0) {
            headers.put("X-FirePHP-Data-200000000001", "\"FirePHP.Dump\":{");
            outputMessages(headers, dumpMessages);
            headers.put("X-FirePHP-Data-299999999999", "\"__SKIP__\":\"__SKIP__\"},");
        }

        if (consoleMessages.size() > 0) {
            headers.put("X-FirePHP-Data-300000000001", "\"FirePHP.Firebug.Console\":[");
            outputMessages(headers, consoleMessages);
            headers.put("X-FirePHP-Data-399999999999", "[\"__SKIP__\"]],");
        }

        headers.put("X-FirePHP-Data-999999999999", "\"__SKIP__\":\"__SKIP__\"}");
        return headers;
    }

    private void outputMessages(HashMap<String, String> headers,
            ArrayList<Message> messages) {
        for (Message message : messages) {
            message.addToResponseHeaders(headers);
        }
    }
    
    public void reset() {
        dumpMessages = new ArrayList<Message>();
        consoleMessages = new ArrayList<Message>();
    }

    public void init(Context context) throws Exception {
        setJSONSerializer(new PustefixJSONSerializer());
    }

    public void insertStatus(ResultDocument resdoc, Element elem)
            throws Exception {
    }

}
