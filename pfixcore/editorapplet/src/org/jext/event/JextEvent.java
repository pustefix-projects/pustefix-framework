/*
 * JextEvent.java - Jext event model
 * Copyright (C) 2000 Romain Guy
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

package org.jext.event;

//import org.jext.JextFrame;
//import org.jext.JextTextArea;

import de.schlund.pfixeditor.view.Gui;
import de.schlund.pfixeditor.view.PfixTextArea;

/**
 * The <code>JextEvent</code> emitted by Jext windows on the order
 * of a <code>Jext</code> instance or on the order of another class
 * (<code>JextTextArea</code> for example).
 * @author Romain Guy
 * @see org.jext.event.JextListener
 */

public class JextEvent
{
  /** Event type indicating a change of the options */
  public static final int PROPERTIES_CHANGED = 0;
  /** Event type indicating a change of the colorizing syntax mode */
  public static final int SYNTAX_MODE_CHANGED = 1;

  // text area specific
  /** Event type indicating a change in a text area */
  public static final int CHANGED_UPDATE = 2;
  /** Event type indicating an insertion in a text area */
  public static final int INSERT_UPDATE = 3;
  /** Event type indicating a removing in a text area */
  public static final int REMOVE_UPDATE = 4;

  /** Event type indicating a file was opened */
  public static final int FILE_OPENED = 10;
  /** Event type indicating a file was cleared (new) */
  public static final int FILE_CLEARED = 11;

  /** Event type indicating batch mode is on */
  public static final int BATCH_MODE_SET = 20;
  /** Event type indicating batch mode is off */
  public static final int BATCH_MODE_UNSET = 21;

  /** Event type indicating current selected text area has gained focus. UNUSED */
  public static final int TEXT_AREA_FOCUS_GAINED = 76;
  /** Event type indicating current selected text area has changed */
  public static final int TEXT_AREA_SELECTED = 77;
  /** Event type indicating a new text area is available */
  public static final int TEXT_AREA_OPENED = 78;
  /** Event type indicating a text area was closed */
  public static final int TEXT_AREA_CLOSED = 79;

  /** Event type indicating a new window was opened */
  public static final int OPENING_WINDOW = 98;
  /** Event type indicating a window was closed */
  public static final int CLOSING_WINDOW = 99;
  /** Event type indicating JVM is being killed */
  public static final int KILLING_JEXT = 101;

  // private
  private int event;
//  private JextFrame parent;
//  private JextTextArea textArea;
  private Gui parent;
  private PfixTextArea textArea;

  /**
   * Creates a new JextEvent, registering the parent of
   * this event, the type of the event and also the
   * text area which was selected when event was created.
   * @param parent <code>Jext</code> parent
   * @param eventType A int value which determine the nature of the event
   */

  public JextEvent(Gui parent, int eventType)
  {
    this.parent = parent;
    this.textArea = parent.getTextArea();
    this.event = eventType;
  }

  /**
   * Creates a new JextEvent, registering the parent of
   * this event, the type of the event and also the
   * text area which was selected when event was created.
   * @param parent <code>Jext</code> parent
   * @param The event related text area
   * @param eventType A int value which determine the nature of the event
   */

  public JextEvent(Gui parent, PfixTextArea textArea, int eventType)
  {
    this.parent = parent;
    this.textArea = textArea;
    this.event = eventType;
  }

  /**
   * Returns the type of event.
   * @return A int indicating the type of the fired event
   */

  public int getWhat()
  {
    return event;
  }

  /**
   * Returns the <code>Jext</code> parent from which the
   * event has been fired.
   * @return A <code>Jext</code> instance, indicating which window has fired the event
   */

  public Gui getJextFrame()
  {
    return parent;
  }

  /**
   * Returns the text area which was visible when the event has been
   * fired as the selected text area may have changed when event is received
   * due to the action of another listener.
   * @return A <code>JextTextArea</code> designating the selected text area when event
   * has been fired
   */

  public PfixTextArea getTextArea()
  {
    return textArea;
  }
}

// End of JextEvent.java
