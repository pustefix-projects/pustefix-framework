/*
 * 03/01/2002 - 19:53:59
 *
 * FindReplace.java - The Jext's find dialog
 * Copyright (C) 1998-2001 Romain Guy
 * romain.guy@jext.org
 * www.jext.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.jext.search;

import gnu.regexp.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.JApplet;
//import org.jext.*;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.Props;
import de.schlund.pfixeditor.misc.*;

/**
 * The <code>FindReplace</code> class is a component which displays
 * a dialog for either finding either replacing text. It provides
 * two combo lists, which holds latest patterns, and many buttons
 * or check boxes for options.
 * @author Romain Guy
 */

public class FindReplace extends JDialog implements ActionListener
{
  // Constant declarations
  /** Defines a search only dialog */
  public static final int SEARCH = 1;
  /** Defines a search and replace dialog */
  public static final int REPLACE = 2;

  // Private declarations
  private int type;
  private Gui parent;
  private JComboBox fieldSearch;
  private JComboBox fieldReplace;
  private JTextField fieldSearchEditor, fieldReplaceEditor, script;
  private JextHighlightButton btnFind, btnReplace, btnReplaceAll, btnCancel;
  private JextCheckBox checkIgnoreCase, saveStates, useRegexp, allFiles, scripted;

  // This method is used to easily build the GridBagLayout

  private void buildConstraints(GridBagConstraints agbc, int agx, int agy, int agw, int agh,
                                int awx, int awy)
  {
    agbc.gridx = agx;
    agbc.gridy = agy;
    agbc.gridwidth = agw;
    agbc.gridheight = agh;
    agbc.weightx = awx;
    agbc.weighty = awy;
    agbc.insets = new Insets(2, 2, 2, 2);
  }

  /**
   * Constructs a new find dialog according to the specified
   * type of dialog requested. The dialog can be either a
   * FIND dialog, either a REPLACE dialog. In both cases, components
   * displayed remain the sames, but the ones specific to replace
   * feature are grayed out.
   * @param parent The window holder
   * @param type The type of the dialog: <code>FindReplace.FIND</code>
   *             or <code>FindReplace.REPLACE</code>
   * @param modal Displays dialog as a modal window if true
   */

/*
 Object parent;

public void init()
{
parent = getParent();
while (!(parent instanceof Frame))
parent = ((Component)parent).getParent();
open.addActionListener(this);
add(open);
}

public void actionPerformed(ActionEvent evt)
{
Object obj = evt.getSource();

if (obj == open)
{
setEnabled(false);
new AppletDialog((Frame)parent,this,"Applet Dialog",true);
}
}
}

class AppletDialog extends Dialog implements ActionListener
{
Button ok = new Button("OK");
AppletDialogTest parent;

AppletDialog(Frame owner, AppletDialogTest parent, String title, boolean modal)
{
super(owner,title,modal);

this.parent = parent;

ok.addActionListener(this);

addWindowListener(new WindowAdapter() {
public void windowClosing(WindowEvent evt) {
dispose();}});

add(BorderLayout.NORTH,new Label("This dialog box is attached to the browser window!"));
add(BorderLayout.SOUTH,ok);

pack();
setVisible(true);
}

public void actionPerformed(ActionEvent evt)
{
Object obj = evt.getSource();

if (obj == ok)
dispatchEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));

parent.setEnabled(true);
}
}

*/
  public FindReplace(Gui gui, Frame cmp_frame, int type, boolean modal)
  {     
    super(cmp_frame, type == REPLACE ? Props.getProperty("replace.title") :
                                    Props.getProperty("find.title"), modal);
    this.parent = gui;
    this.type = type;
    Log.println("Dialog: FindReplace instantiated ");
    fieldSearch = new JComboBox();
    fieldSearch.setRenderer(new ModifiedCellRenderer());
    fieldSearch.setEditable(true);
    fieldReplace = new JComboBox();
    fieldReplace.setRenderer(new ModifiedCellRenderer());
    fieldReplace.setEditable(true);
    KeyHandler handler = new KeyHandler();
    fieldSearchEditor = (JTextField) fieldSearch.getEditor().getEditorComponent();
    fieldSearchEditor.addKeyListener(handler);
    fieldReplaceEditor = (JTextField) fieldReplace.getEditor().getEditorComponent();
    fieldReplaceEditor.addKeyListener(handler);

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    getContentPane().setLayout(gridbag);
    ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    JLabel findLabel = new JLabel(Props.getProperty("find.label"));
    buildConstraints(constraints, 0, 0, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(findLabel, constraints);
    getContentPane().add(findLabel);

    buildConstraints(constraints, 1, 0, 1, 1, 25, 50);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(fieldSearch, constraints);
    getContentPane().add(fieldSearch);

    btnFind = new JextHighlightButton(Props.getProperty("find.button"));
    btnFind.setToolTipText(Props.getProperty("find.tip"));
    btnFind.setMnemonic(Props.getProperty("find.mnemonic").charAt(0));
    btnFind.addActionListener(this);
    buildConstraints(constraints, 2, 0, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(btnFind, constraints);
    getContentPane().add(btnFind);
    getRootPane().setDefaultButton(btnFind);

    btnCancel = new JextHighlightButton(Props.getProperty("general.cancel.button"));
    btnCancel.setMnemonic(Props.getProperty("general.cancel.mnemonic").charAt(0));
    btnCancel.addActionListener(this);
    buildConstraints(constraints, 3, 0, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(btnCancel, constraints);
    getContentPane().add(btnCancel);

    JLabel replaceLabel = new JLabel(Props.getProperty("replace.label"));
    buildConstraints(constraints, 0, 1, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(replaceLabel, constraints);
    getContentPane().add(replaceLabel);

    buildConstraints(constraints, 1, 1, 1, 1, 25, 50);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(fieldReplace, constraints);
    getContentPane().add(fieldReplace);

    if (type != REPLACE)
      fieldReplace.setEnabled(false);
    btnReplace = new JextHighlightButton(Props.getProperty("replace.button"));
    btnReplace.setToolTipText(Props.getProperty("replace.tip"));
    btnReplace.setMnemonic(Props.getProperty("replace.mnemonic").charAt(0));
    if (type != REPLACE)
      btnReplace.setEnabled(false);
    btnReplace.addActionListener(this);
    buildConstraints(constraints, 2, 1, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(btnReplace, constraints);
    getContentPane().add(btnReplace);

    btnReplaceAll = new JextHighlightButton(Props.getProperty("replace.all.button"));
    btnReplaceAll.setToolTipText(Props.getProperty("replace.all.tip"));
    btnReplaceAll.setMnemonic(Props.getProperty("replace.all.mnemonic").charAt(0));
    if (type != REPLACE)
      btnReplaceAll.setEnabled(false);
    btnReplaceAll.addActionListener(this);
    buildConstraints(constraints, 3, 1, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(btnReplaceAll, constraints);
    getContentPane().add(btnReplaceAll);

    //scripted = new JextCheckBox(Props.getProperty("replace.script"), Search.getPythonScript());
    /*
    if (type != REPLACE)
      scripted.setEnabled(false);
    else
    {
      fieldReplace.setEnabled(!scripted.isSelected());
      scripted.addActionListener(this);
    }
    */
    /*
    buildConstraints(constraints, 0, 2, 1, 1, 50, 50);
    constraints.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(scripted, constraints);
    getContentPane().add(scripted);
*/
/*
    script = new JTextField();
    if (type != REPLACE)
      script.setEnabled(false);
    else
      script.setEnabled(scripted.isSelected());
    script.setText(Search.getPythonScriptString());
    buildConstraints(constraints, 1, 2, 1, 1, 50, 50);
    constraints.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(script, constraints);
    getContentPane().add(script);
*/
    checkIgnoreCase = new JextCheckBox(Props.getProperty("find.ignorecase.label"), Search.getIgnoreCase());
    buildConstraints(constraints, 0, 3, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(checkIgnoreCase, constraints);
    getContentPane().add(checkIgnoreCase);

    JPanel cPane = new JPanel();
    saveStates = new JextCheckBox(Props.getProperty("find.savevalues.label"),
                               Props.getBooleanProperty("savestates"));
//    allFiles = new JextCheckBox(Props.getProperty("find.allFiles.label"),
//                             Props.getBooleanProperty("allfiles"));
    cPane.add(saveStates);
//    cPane.add(allFiles);

    buildConstraints(constraints, 1, 3, 1, 1, 25, 50);
    constraints.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(cPane, constraints);
    getContentPane().add(cPane);

    useRegexp = new JextCheckBox(Props.getProperty("find.useregexp.label"), Search.getRegexp());
    buildConstraints(constraints, 2, 3, 2, 1, 50, 50);
    constraints.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(useRegexp, constraints);
    getContentPane().add(useRegexp);

    load();

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addKeyListener(new AbstractDisposer(this));
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        exit();
      }
    });

    FontMetrics fm = getFontMetrics(getFont());
    fieldSearch.setPreferredSize(new Dimension(18 * fm.charWidth('m'),
                                 (int) fieldSearch.getPreferredSize().height));
    fieldReplace.setPreferredSize(new Dimension(18 * fm.charWidth('m'),
                                  (int) fieldReplace.getPreferredSize().height));

    pack();
    setResizable(false);
    Utilities.centerComponentChild(parent, this);

//patch by MJB 8/1/2002		
  btnFind.addKeyListener(handler);
	btnReplace.addKeyListener(handler);
	btnReplaceAll.addKeyListener(handler);
	btnCancel.addKeyListener(handler);
	checkIgnoreCase.addKeyListener(handler);
	saveStates.addKeyListener(handler);
	useRegexp.addKeyListener(handler);
	//allFiles.addKeyListener(handler);
	//scripted.addKeyListener(handler);
	//script.addKeyListener(handler);
//end MJB patch
		
    show();
  }

  // load the search and replace histories from user
  // properties. It also selects latest pattern from
  // the list.

  private void load()
  {
    String s;
    for (int i = 0; i < 25; i++)
    {
      s = Props.getProperty("search.history." + i);
      if (s != null)
        fieldSearch.addItem(s);
      else
        break;
    }

    PfixTextArea textArea = parent.getTextArea();
    if (!Props.getBooleanProperty("use.selection"))
    {
      s = Search.getFindPattern();
      if (s != null)
      {
        addSearchHistory(s);
        fieldSearch.setSelectedItem(s);
      }
    } else if ((s = textArea.getSelectedText()) != null) {

      char c = '\0';
      StringBuffer buf = new StringBuffer(s.length());
out:  for (int i = 0; i < s.length(); i++)
      {
        switch (c = s.charAt(i))
        {
          case '\n':
            break out;
          default:
            buf.append(c);
        }
      }

      s = buf.toString();
      addSearchHistory(s);
      fieldSearch.setSelectedItem(s);
    }

    if (type == REPLACE)
    {
      for (int i = 0; i < 25; i++)
      {
        s = Props.getProperty("replace.history." + i);
        if (s != null)
          fieldReplace.addItem(s);
        else
          break;
      }

      s = Search.getReplacePattern();
      if (s != null)
      {
        addReplaceHistory(s);
        fieldReplace.setSelectedItem(s);
      }
    }

    // selects contents
    fieldSearchEditor.selectAll();
  }

  // exits the dialog after having saved the search and
  // replace histories.

  private void exit()
  {
    if (saveStates.isSelected())
    {
      for (int i = 0; i < fieldSearch.getItemCount(); i++)
        Props.setProperty("search.history." + i, (String) fieldSearch.getItemAt(i));
      for (int i = fieldSearch.getItemCount(); i < 25; i++)
        Props.unsetProperty("search.history." + i);

      if (type == REPLACE)
      {
        for (int i = 0; i < fieldReplace.getItemCount(); i++)
          Props.setProperty("replace.history." + i, (String) fieldReplace.getItemAt(i));
        for (int i = fieldReplace.getItemCount(); i < 25; i++)
          Props.unsetProperty("replace.history." + i);
      }
    }

    Props.setProperty("savestates", (saveStates.isSelected() ? "on" : "off"));
    //Props.setProperty("allfiles", (allFiles.isSelected() ? "on" : "off"));

    dispose();
  }

  // adds current search pattern in the search history list

  private void addSearchHistory()
  {
    addSearchHistory(fieldSearchEditor.getText());
  }

  // adds a pattern in the search history list
  // the pattern to be added is specified by the param c

  private void addSearchHistory(String c)
  {
    if (c == null)
      return;

    for (int i = 0; i < fieldSearch.getItemCount(); i++)
    {
      if (((String) fieldSearch.getItemAt(i)).equals(c))
        return;
    }

    fieldSearch.insertItemAt(c, 0);
    if (fieldSearch.getItemCount() > 25)
    {
      for (int i = 24; i < fieldSearch.getItemCount(); i++)
        fieldSearch.removeItemAt(i);
    }

    //Search.setFindPattern(fieldSearchEditor.getText());
    fieldSearchEditor.setText((String) fieldSearch.getItemAt(0));
  }

  // adds current replace pattern in the replace history list

  private void addReplaceHistory()
  {
    addReplaceHistory(fieldReplaceEditor.getText());
  }

  // adds a pattern in the replace history list
  // the pattern to be added is given by the param c

  private void addReplaceHistory(String c)
  {
    if (c == null)
      return;

    for (int i = 0; i < fieldReplace.getItemCount(); i++)
    {
      if (((String) fieldReplace.getItemAt(i)).equals(c))
        return;
    }

    fieldReplace.insertItemAt(c, 0);
    if (fieldReplace.getItemCount() > 25)
    {
      for (int i = 24; i < fieldReplace.getItemCount(); i++)
        fieldReplace.removeItemAt(i);
    }

    //Search.setReplacePattern(fieldReplaceEditor.getText());
    fieldReplaceEditor.setText((String) fieldReplace.getItemAt(0));
  }

  // Catch the action performed and then look for its source
  // According to the source object we call appropriate methods

  public void actionPerformed(ActionEvent evt)
  {
    Object source = evt.getSource();
    if (source == btnCancel)
      exit();
    else if (source == btnFind)
      doFind();
    else if (source == btnReplace)
      doReplace();
    else if (source == btnReplaceAll)
      doReplaceAll();
    else if (source == scripted)
    {
      script.setEnabled(scripted.isSelected());
      fieldReplace.setEnabled(!scripted.isSelected());
    }
  }

  private void setSettings()
  {
    Search.setFindPattern(fieldSearchEditor.getText());
    Search.setIgnoreCase(checkIgnoreCase.isSelected());
    Search.setRegexp(useRegexp.isSelected());
    if (type == REPLACE)
    {
      Search.setReplacePattern(fieldReplaceEditor.getText());
      //Search.setPythonScript(scripted.isSelected());
      //Search.setPythonScriptString(script.getText());
    }
  }

  // replace all the occurences of search pattern by
  // the replace one. If 'All Files' is checked, this is
  // done in all the opened file in the component 'parent'

  private void doReplaceAll()
  {
    Utilities.setCursorOnWait(this, true);
    addReplaceHistory();
    addSearchHistory();

    try
    {
/*
      if (allFiles.isSelected())
      {
        parent.setBatchMode(true);

        PfixTextArea textArea=parent.getTextArea();
        //JextTextArea[] areas = parent.getTextAreas();

 //        for (int i = 0; i < areas.length; i ++)
 //       {
 //         textArea = areas[i];
          setSettings();
          Search.replaceAll(textArea, 0,  textArea.getLength());
 //       }

        parent.setBatchMode(false);
      } else  {*/
        PfixTextArea textArea = parent.getTextArea();
        setSettings();
        if (Search.replaceAll(textArea, 0, textArea.getLength()) == 0)
        {
          Utilities.beep();
        }
      //}

    } catch (Exception e) {
      // nothing
    } finally {
     Utilities.setCursorOnWait(this, false);
    }
  }

  // replaces specified search pattern by the replace one.
  // this is done only if a match is found.

  private void doReplace()
  {
    Utilities.setCursorOnWait(this, true);
    addReplaceHistory();
    addSearchHistory();

    try
    {

      PfixTextArea textArea = parent.getTextArea();
      setSettings();

      if (!Search.replace(textArea))
      {
        Utilities.beep();
      } else
        find(textArea);

    } catch (Exception e) {
      // nothing
    } finally {
      Utilities.setCursorOnWait(this, false);
    }
  }

  // finds the next occurence of current search pattern
  // the search is done in current text area

  private void doFind()
  {
    Utilities.setCursorOnWait(this, true);

    addSearchHistory();
    find(parent.getTextArea());

    Utilities.setCursorOnWait(this, false);
  }

  // finds the next occurence of the search pattern in a
  // a given text area. if match is not found, and if user
  // don't ask to start over from beginning, then the method
  // calls itself by specifying next opened text area.

  private void find(PfixTextArea textArea)
  {
    setSettings();

    try
    {
      if (!Search.find(textArea, textArea.getCaretPosition()))
      {
        String[] args = { "text" };
        Log.println("Dialog: matchnotfound");
        int response = JOptionPane.showConfirmDialog(null,
                       Props.getProperty("find.matchnotfound", args),
                       Props.getProperty("find.title"),
                       (false ?
                        JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION),
                       JOptionPane.QUESTION_MESSAGE);

        switch (response)
        {
          case JOptionPane.YES_OPTION:
            textArea.setCaretPosition(0);
            find(textArea);
            break;
          case JOptionPane.NO_OPTION:
            //if (allFiles.isSelected())
            {
             /*
              JextTabbedPane pane = parent.getTabbedPane();
              int index = pane.indexOfComponent(textArea);

              Component c = null;
              while (c == null && !(c instanceof JextTextArea))
              {
                index++;
                if (index == pane.getTabCount())
                  index = 0;
                c = pane.getComponentAt(index);
              }

              PfixTextArea area = (PfixTextArea) c;
              if (area != textArea)
                find(area);
              */
            }
            break;
          case JOptionPane.CANCEL_OPTION:
            return;
        }
      }
    } catch (Exception e) { }
  }

  class KeyHandler extends KeyAdapter
  {
    public void keyPressed(KeyEvent evt)
    {
      switch (evt.getKeyCode())
      {
        case KeyEvent.VK_ENTER:
          if (evt.getSource() == fieldSearchEditor)
            doFind();
          else if (evt.getSource() == fieldReplaceEditor)
            doReplace();
          break;
        case KeyEvent.VK_ESCAPE:
          exit();
      }
    }
  }

  /***************************************************************************
  Patch
     -> Memory management improvements : it may help the garbage collector.
     -> Author : Julien Ponge (julien@izforge.com)
     -> Date : 23, May 2001
  ***************************************************************************/
  protected void finalize() throws Throwable
  {
    super.finalize();

    parent = null;
    fieldSearch = null;
    fieldReplace = null;
    fieldSearchEditor = null;
    fieldReplaceEditor = null;
    btnFind = null;
    btnReplace = null;
    btnReplaceAll = null;
    btnCancel = null;
    checkIgnoreCase = null;
    saveStates = null;
    useRegexp = null;
    allFiles = null;
  }
  // End of patch
}

// End of FindReplace.java
