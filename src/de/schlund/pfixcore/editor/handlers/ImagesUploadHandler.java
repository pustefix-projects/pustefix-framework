/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.editor.handlers;

import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixxml.util.Path;
import de.schlund.util.statuscodes.*;
import java.io.*;
import org.apache.log4j.*;


/**
 * Handler responsible for uploading images.
 *
 *
 * Created: Mon Dec 03 23:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ImagesUploadHandler extends EditorStdHandler {
    private static       Category CAT    = Category.getInstance(ImagesUploadHandler.class.getName());
    private static       Category EDITOR = Category.getInstance("LOGGER_EDITOR");
    private static final String   BACKUP = "__backup__";
    
    // Overwrite this so we are only active when we already have a lock on the current image.
    public boolean isActive(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        AuxDependency          aux   = esess.getCurrentImage();
        if (aux == null) {
            return false;
        } else {
            return esess.isOwnLock(aux);
        }
    }

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        ImagesUpload           upload   = (ImagesUpload) wrapper;
        Boolean                haveupl  = upload.getHaveUpload();
        AuxDependency          aux      = esess.getCurrentImage();
        Path                   path     = aux.getPath();
        StatusCodeFactory      sfac     = new StatusCodeFactory("pfixcore.editor.imagesupload");
        StatusCode             scode    = null;
        Boolean                backup   = upload.getHaveBackup();
        String                 backfile = upload.getBackup();

        // We don't want to shadow the value back to the UI.
        upload.setStringValBackup(null);
        
        
        
        if (haveupl != null && haveupl.booleanValue()) {
            if(CAT.isDebugEnabled())
                CAT.debug("Checking access for image upload.");
            // call checkAccess only if we have really an upload attempt
            checkAccess(esess);
            
            
            File   file = null;
            String type = BACKUP;
            if (backup != null && backup.booleanValue() && backfile != null) {
                file = EditorHelper.getBackupImageFile(esess, aux, backfile);
            } else {
                file = upload.getUplImage();
                if (file != null) {
                    CAT.debug("*** HAVE A FILE!");
                    ImageInfo info = new ImageInfo();
                    info.setInput(new FileInputStream(file));
                    if (info.check()) {
                        type = info.getMimeType();
                    } else {
                        CAT.error("*** Couldn't check supplied image file ****");
                        file = null;
                    }
                    CAT.debug("*** Image upload: Filetype: " + type);
                }
            }
            if (file != null) {
                String to_suff = path.getSuffix();
                if (type.equals(BACKUP) ||
                    (to_suff.equals(".gif") && type.equals("image/gif"))  ||
                    (to_suff.equals(".jpg") && type.equals("image/jpeg")) ||
                    (to_suff.equals(".png") && type.equals("image/png"))) {
                    File to_file   = path.resolve();
                    createDirMaybe(esess, path);
                    EditorHelper.createBackupImage(esess, to_file);
                    FileInputStream  fin  = new FileInputStream(file);
                    FileOutputStream fout = new FileOutputStream(to_file);
                    byte[] b   = new byte[4096];
                    int    num = 0;
                    while ((num = fin.read(b)) != -1) {
                        fout.write(b, 0, num);
                    }
                    fout.close();
                    fin.close();
                    file.delete();
                    EDITOR.warn("IMG: " + esess.getUser().getId() + ": " + path);
                } else {
                    if (to_suff.equals(".gif")) {
                        scode = sfac.getStatusCode("IMAGEUPL_WRONGTYPEGIF");
                    } else if (to_suff.equals(".jpg")) {
                        scode = sfac.getStatusCode("IMAGEUPL_WRONGTYPEJPG");
                    } else if (to_suff.equals(".png")) {
                        scode = sfac.getStatusCode("IMAGEUPL_WRONGTYPEPNG");
                    } else {
                        scode = sfac.getStatusCode("IMAGEUPL_WRONGTYPE");
                    }
                }
            } else {
                scode = sfac.getStatusCode("IMAGEUPL_NOFILE");
            }
            
            if (scode != null) {
                upload.addSCodeHaveUpload(scode);
            }
        } else {
            if(CAT.isDebugEnabled())
                CAT.debug("This seems NOT to be a real image upload");
        }
    }
    
    /**
     * Creates directories for a file, if neccessary directories do not exist yet. 
     *  
     * @param esess 
     * @param path the complete path of a file
     * @return true if at least one directory had to be created,
     *         false if all neccessary directories existed. 
     * @throws Exception if directory could not be created
     *         (that is if {@link File#mkdirs()} returned false)
     */
    private boolean createDirMaybe(EditorSessionStatus esess, Path path) throws Exception {
        boolean dirCreated;
        String reldir = path.getDir();
        File base = path.getBase();
        File absdir;
        if ( reldir == null ) {
            absdir = base;
        } else {
            absdir = new File(base, reldir);
        }
        if ( !absdir.exists() ) { 
            dirCreated = absdir.mkdirs();
            if ( dirCreated == false ) {
                throw new Exception("Directory \""+absdir+"\" could not be created. Reason unknown.");
            }
        } else {
            dirCreated = false;
        }
        return dirCreated;
    }

    private void checkAccess(EditorSessionStatus esess) throws XMLException {
        if(CAT.isDebugEnabled())
            CAT.debug("checkAccess start");
                    
        EditorUserInfo user = esess.getUser().getUserInfo();
         
        if(! user.isImageEditAllowed(esess.getCurrentImage().getPath())) {
            throw new XMLException("Permission denied! "); 
        } 
                
        if(CAT.isDebugEnabled()) 
            CAT.debug("checkAccess end. Permission granted."); 
    
    }
    
}// ImagesHandler
