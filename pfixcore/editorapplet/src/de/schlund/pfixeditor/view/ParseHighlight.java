/*
 * 20:10:43 05/05/00
 *
 * SearchHighlight.java - Highlights anchor
 * Copyright (C) 2000 Romain Guy
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

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.text.Element;

//import org.jext.*;
import de.schlund.pfixeditor.view.PfixTextArea;
import org.gjt.sp.jedit.textarea.*;
import de.schlund.pfixeditor.xml.XmlError;

public class ParseHighlight implements TextAreaHighlight
{
  private ArrayList errs;
  private PfixTextArea textArea;
  private TextAreaHighlight next;
  private boolean enabled = false;
  
  public void disable()
  {
    enabled = false;
  }

  public void enable()
  {
    enabled = true;
  }

  public void trigger(boolean on)
  {
    enabled = on;
  }

  public void setErrors(ArrayList errs)
  {
    this.errs = errs;
  }

  public void init(JEditTextArea textArea, TextAreaHighlight next)
  {
    this.textArea = (PfixTextArea) textArea;
    this.next = next;
  }

  public void paintHighlight(Graphics gfx, int line, int y)
  {
    if (enabled && errs != null)
    {
      gfx.setColor(Color.red);
      //gfx.setColor(Color.blue);

      Element lineElement;
      Element map = textArea.getDocument().getDefaultRootElement();

      FontMetrics fm = textArea.getPainter().getFontMetrics();

      int tokenStart = -1;
      int tokenEnd=-1;
      int width = fm.charWidth('w');
      int myY = y + fm.getHeight() + fm.getLeading() + fm.getMaxDescent() + 1;
      int horOffset = textArea.getHorizontalOffset();
      int _width = textArea.getWidth();

      for (int i = 0; i < errs.size(); i++)
      {
        tokenStart = ((XmlError) errs.get(i)).getTokenStart();
        tokenEnd   = ((XmlError) errs.get(i)).getTokenEnd();
        int matchLine = map.getElementIndex(tokenStart);

        if ((line == matchLine) && (tokenStart!=tokenEnd) && (tokenStart!=-1) && (tokenEnd!=-1))
        {
          lineElement = map.getElement(line);

          //int off = (pos[0] - lineElement.getStartOffset()) * width + horOffset;
          int off = textArea.offsetToX(line, tokenStart - lineElement.getStartOffset());

          if (off >= horOffset && off < horOffset + _width)
          {
            int matchWidth = (tokenEnd - tokenStart) * width + off;
  
            for ( ; off < matchWidth; off += 4)
            {
              gfx.drawLine(off, myY, off + 2, myY - 2);
              gfx.drawLine(off + 2, myY - 2, off + 4, myY);
            }
          }
        }
      }
    }

    if (next != null)
      next.paintHighlight(gfx, line, y);
  }

  public String getToolTipText(MouseEvent evt)
  {
    return null;
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
    
    errs = null;
    textArea = null;
    next = null;
  }
  // End of patch
}

// End of SearchHighlight.java
