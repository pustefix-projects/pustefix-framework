package de.schlund.pfixcore.editor;

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.util.*;
import de.schlund.util.FactoryInit;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import java.net.URLEncoder;

/**
 * @author zaich
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CoreDocumentation {

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

        this.id = URLEncoder.encode(param);
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
        String value = "[ ";

        if (!this.getMatch().equals("")) {
            value = value + " Match=\"" + this.getMatch() + "\" ";
        }

        if (!this.getName().equals("")) {
            value = value + "Name=\"" + this.getName() + "\" ";
        }

        if (!this.getMode().equals("")) {
            value = value + "Mode=\"" + this.getMode() + "\" ";
        }

        value = value + " ] ";

        return value;

    }

}
