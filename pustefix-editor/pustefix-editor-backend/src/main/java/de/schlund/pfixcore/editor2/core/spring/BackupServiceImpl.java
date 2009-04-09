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

package de.schlund.pfixcore.editor2.core.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Xml;

/**
 * Implementation of
 * {@link de.schlund.pfixcore.editor2.core.spring.BackupService} using a
 * directory on the filesystem to store backups.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class BackupServiceImpl implements BackupService {
    private final static String BACKUPDIR = ".editorbackup";

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }

    public void setPathResolverService(PathResolverService pathresolver) {
        this.pathresolver = pathresolver;
    }

    private File getBackupFile(String path) {
        return this.getBackupFile(path, this.getVersion());
    }

    private File getBackupFile(String path, String version) {
        String fullPath;
        if (path.startsWith(File.separator)) {
            fullPath = BACKUPDIR + path;
        } else {
            fullPath = BACKUPDIR + File.separator + path;
        }
        fullPath = fullPath + File.separator + version;
        return new File(pathresolver.resolve(fullPath));
    }

    private File getFile(String path) {
        return new File(pathresolver.resolve(path));
    }

    private File getBackupDir(String path) {
        String fullPath;
        if (path.startsWith(File.separator)) {
            fullPath = BACKUPDIR + path;
        } else {
            fullPath = BACKUPDIR + File.separator + path;
        }
        return new File(pathresolver.resolve(fullPath));
    }

    private String getVersion() {
        Date date = new Date();
        return date.toString();
    }

    public void backupImage(Image image) {
        File backupFile = this.getBackupFile(image.getPath());
        File imageFile = this.getFile(image.getPath());
        try {
            synchronized (this.filesystem.getLock(imageFile)) {
                synchronized (this.filesystem.getLock(backupFile)) {
                    File parentDir = backupFile.getParentFile();
                    if (!parentDir.exists()) {
                        this.filesystem.makeDirectory(parentDir, true);
                    }
                    this.filesystem.copy(imageFile, backupFile);
                }
            }
        } catch (EditorIOException e) {
            String err = "Could not create backup for image " + image.getPath()
                    + "!";
            Logger.getLogger(this.getClass()).error(err, e);
        }
    }

    public boolean restoreImage(Image image, String version) {
        // Make sure version string is safe
        if (version.indexOf(File.separator) != -1) {
            return false;
        }
        File backupFile = this.getBackupFile(image.getPath(), version);
        if (!backupFile.exists()) {
            return false;
        }
        try {
            synchronized (this.filesystem.getLock(backupFile)) {
                try {
                    image.replaceFile(backupFile);
                } catch (EditorSecurityException e) {
                    throw new RuntimeException("Unexpected exception", e);
                }
            }

        } catch (EditorIOException e) {
            String err = "Could not restore backup " + backupFile.getPath()
                    + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        }
        return true;
    }

    public Collection<String> listImageVersions(Image image) {
        File dir = this.getBackupDir(image.getPath());
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<String>();
        }
        File[] files = dir.listFiles();
        Comparator<File> comp = new Comparator<File>() {
            public int compare(File file0, File file1) {
                long ret = file0.lastModified() - file1.lastModified();
                if (ret == 0) {
                    return 0;
                } else if (ret < 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
        Arrays.sort(files, comp);
        ArrayList<String> filesList = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && !files[i].isHidden()) {
                filesList.add(files[i].getName());
            }
        }
        return filesList;
    }

    public void backupInclude(IncludePartThemeVariant include) {
        File backupFile = this.getBackupFile(include.getIncludePart()
                .getIncludeFile().getPath()
                + File.separator
                + include.getIncludePart().getName()
                + File.separator + include.getTheme().getName());
        try {
            File parentDir = backupFile.getParentFile();
            if (!parentDir.exists()) {
                this.filesystem.makeDirectory(parentDir, true);
            }
            try {
                Document doc = Xml.createDocument();
                doc.appendChild(doc.importNode(include.getXML(), true));
                this.filesystem.storeXMLDocumentToFile(backupFile, doc);
            } catch (IOException e) {
                String msg = "Could not write backup!";
                Logger.getLogger(this.getClass()).error(msg, e);
            }
        } catch (EditorIOException e) {
            String msg = "Could not write backup!";
            Logger.getLogger(this.getClass()).error(msg, e);
        }
    }

    public boolean restoreInclude(IncludePartThemeVariant include,
            String version) {
        // Make sure version string is safe
        if (version.indexOf(File.separator) != -1) {
            return false;
        }
        File backupFile = this.getBackupFile(include.getIncludePart()
                .getIncludeFile().getPath()
                + File.separator
                + include.getIncludePart().getName()
                + File.separator + include.getTheme().getName(), version);
        if (!backupFile.exists()) {
            return false;
        }
        try {
            synchronized (this.filesystem.getLock(backupFile)) {
                Document doc = this.filesystem
                        .readXMLDocumentFromFile(backupFile);
                include.setXML(doc.getDocumentElement());
            }
        } catch (FileNotFoundException e) {
            String err = "Could not restore backup!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        } catch (SAXException e) {
            String err = "Could not restore backup!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        } catch (IOException e) {
            String err = "Could not restore backup!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        } catch (EditorIOException e) {
            String err = "Could not restore backup!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        } catch (EditorParsingException e) {
            String err = "Could not restore backup!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        } catch (EditorSecurityException e) {
            String err = "Could not restore backup!";
            Logger.getLogger(this.getClass()).error(err, e);
            return false;
        }
        return true;
    }

    public Collection<String> listIncludeVersions(IncludePartThemeVariant include) {
        File dir = this.getBackupDir(include.getIncludePart().getIncludeFile()
                .getPath()
                + File.separator
                + include.getIncludePart().getName()
                + File.separator + include.getTheme().getName());
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<String>();
        }
        File[] files = dir.listFiles();
        Comparator<File> comp = new Comparator<File>() {
            public int compare(File file0, File file1) {
                long ret = file0.lastModified() - file1.lastModified();
                if (ret == 0) {
                    return 0;
                } else if (ret < 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
        Arrays.sort(files, comp);
        ArrayList<String> filesList = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && !files[i].isHidden()) {
                filesList.add(files[i].getName());
            }
        }
        return filesList;
    }

}
