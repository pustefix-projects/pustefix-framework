package de.schlund.pfixcore.editor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author zaich
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CoreDocumentation {
    public static final String encode(String str) {
        final String ENCODING = "UTF-8";
        try {
            return URLEncoder.encode(str, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(ENCODING + ": " + e);
        }
    }

    private String id;
    private String match;
    private String name;
    private NodeList nodelist;
    private String stylesheet;
    private Document doc;
    private String mode;
    private String modus;
    private String value;

    public CoreDocumentation(String param) {
        this.id = encode(param);
        this.stylesheet = param.substring(0, param.indexOf("@"));

    }

    public void setModus(String modus) {
        this.modus = modus;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNodeList(NodeList nl) {
        this.nodelist = nl;
    }

    public void setStyleSheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public void setDocument(Document doc) {
        this.doc = doc;
    }

    public String getMode() {
        return this.mode;
    }

    public String getId() {
        return this.id;
    }

    public String getMatch() {
        return this.match;
    }

    public String getName() {
        return this.name;
    }

    public String getStyleSheet() {
        return this.stylesheet;
    }

    public NodeList getNodeList() {
        return this.nodelist;
    }

    public Document getDocument() {
        return this.doc;
    }

    public String getModus() {
        return this.modus;
    }

    public String getValue() {
        String result = "[ ";

        if (!getMatch().equals("")) {
            result = result + " Match=\"" + this.getMatch() + "\" ";
        }

        if (!getName().equals("")) {
            result = result + "Name=\"" + this.getName() + "\" ";
        }

        if (!getMode().equals("")) {
            result = result + "Mode=\"" + this.getMode() + "\" ";
        }

        result = result + " ] ";

        return result;

    }

}
