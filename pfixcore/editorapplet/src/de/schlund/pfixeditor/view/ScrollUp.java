/*
 * 01/13/2001 - 13:21:13
 *
 * ScrollUp.java - Scrolls up of a line without changing caret position
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//import org.jext.*;
//import org.gjt.sp.jedit.textarea.*;
import de.schlund.pfixeditor.view.*;

public final class ScrollUp extends MenuAction
{
  public ScrollUp()
  {
    super("scroll_up");
  }

  public void actionPerformed(ActionEvent evt)
  {
    PfixTextArea textArea = getTextArea(evt);
    if (textArea.getFirstLine() > 0)
      textArea.setFirstLine(textArea.getFirstLine() - 1);
  }
}

// End of ScrollUp.java
