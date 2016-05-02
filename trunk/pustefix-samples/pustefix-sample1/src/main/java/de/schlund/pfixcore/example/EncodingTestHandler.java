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
package de.schlund.pfixcore.example;

import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.example.iwrapper.EncodingTestWrapper;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class EncodingTestHandler implements InputHandler<EncodingTestWrapper> {
    
    @Autowired
    private ContextEncodingTest encTest;
    
    public void handleSubmittedData(EncodingTestWrapper test) {
        String encoding = test.getEncoding();
        if (encoding == null || encoding.trim().equals("") || encoding.equals("none")) {
            encTest.setText(test.getText());
        }
    }
    
    public void retrieveCurrentStatus(EncodingTestWrapper test) {
        if (encTest.getText() != null) test.setText(encTest.getText());
    }
    
    public boolean needsData() {
        return false;
    }
    
    public boolean prerequisitesMet() {
        return true;
    }

    public boolean isActive() {
        return true;
    }
    
}
