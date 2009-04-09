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

import java.io.File;

import de.schlund.pfixcore.example.iwrapper.EncodingTestUpload;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * @author mleidig@schlund.de
 */
public class EncodingTestUploadHandler implements IHandler {

	public void handleSubmittedData(Context context,IWrapper wrapper) throws Exception {
        EncodingTestUpload upload=(EncodingTestUpload)wrapper;
        File file=upload.getFile();
        String text=upload.getText();
        ContextEncodingTest ctx=context.getContextResourceManager().getResource(ContextEncodingTest.class);
        if(text!=null) ctx.setText(text);
        if(file!=null) ctx.setFile(file);
    }
    
    public void retrieveCurrentStatus(Context context,IWrapper wrapper) throws Exception {
        EncodingTestUpload upload=(EncodingTestUpload)wrapper;
        ContextEncodingTest ctx=context.getContextResourceManager().getResource(ContextEncodingTest.class);
        if(ctx.getText()!=null) upload.setText(ctx.getText());
    }
    
    public boolean needsData(Context context) throws Exception {
        return false;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }
    
}
