/*
 * 10/29/2001 - 22:26:49
 *
 * JextHighlightButton.java - A modified button
 * Copyright (C) 2001 Romain Guy
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

package de.schlund.pfixeditor.view;

import javax.swing.Icon;
import javax.swing.JButton;

import java.awt.Color;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//import org.jext.Jext;

public class JextHighlightButton extends JButton
{
  private Color nColor;
  private MouseHandler _mouseListener;
  private static Color commonHighlightColor = new Color(192, 192, 210);
  private static boolean blockHighlightChange = false;

  public static void setHighlightColor(Color color)
  {
    if (!blockHighlightChange)
      commonHighlightColor = color;
  }

  public static void blockHighlightChange()
  {
    blockHighlightChange = true;
  }

  private void init()
  {
    //if (Jext.getButtonsHighlight())
    //{
      nColor = getBackground();
      addMouseListener(_mouseListener = new MouseHandler());
    //}
  }

  public JextHighlightButton()
  {
    super();
    init();
  }

  public JextHighlightButton(String label)
  {
    super(label);
    init();
  }

  public JextHighlightButton(Icon icon)
  {
    super(icon);
    init();
  }

  public JextHighlightButton(String label, Icon icon)
  {
    super(label, icon);
    init();
  }

  class MouseHandler extends MouseAdapter
  {
    public void mouseEntered(MouseEvent me)
    {
      if (isEnabled())
        setBackground(commonHighlightColor);
    }

    public void mouseExited(MouseEvent me)
    {
      if (isEnabled())
        setBackground(nColor);
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
    removeMouseListener(_mouseListener);
    super.finalize();

    nColor = null;
    _mouseListener = null;
  }
  // End of patch
}

// End of JextHighlightButton.java
