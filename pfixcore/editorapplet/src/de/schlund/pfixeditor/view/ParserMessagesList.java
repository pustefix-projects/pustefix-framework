package de.schlund.pfixeditor.view;

import java.util.Vector;
import javax.swing.JList;
import javax.swing.ListModel;
/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserMessagesList extends JList {

private Gui gui;

    /**
     * Constructor for ParserMessagesList.
     * @param dataModel
     */
    public ParserMessagesList(ListModel dataModel) {
        super(dataModel);
    }


    /**
     * Constructor for ParserMessagesList.
     * @param listData
     */
    public ParserMessagesList(Object[] listData) {
        super(listData);
    }


    /**
     * Constructor for ParserMessagesList.
     * @param listData
     */
    public ParserMessagesList(Vector listData) {
        super(listData);
    }


    /**
     * Constructor for ParserMessagesList.
     */
    public ParserMessagesList() {
        super();
    }


public ParserMessagesList(Gui gui) {    
    super();
    this.gui=gui;
}


/**
 * Returns the gui.
 * @return Gui
 */
public Gui getGui() {
    return gui;
}


/**
 * Sets the gui.
 * @param gui The gui to set
 */
public void setGui(Gui gui) {
    this.gui = gui;
}


}

