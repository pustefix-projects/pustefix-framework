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
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.pustefixframework.editor.common.dom.IncludeFile;

import de.schlund.pfixcore.editor2.core.spring.internal.DynIncludeFileImpl;

/**
 * Implementation of DynIncludeFactoryService
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynIncludeFactoryServiceImpl implements DynIncludeFactoryService {
    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private HashMap<String, IncludeFile> files;

    private ThemeFactoryService themefactory;

    private BackupService backup;

    private ConfigurationService configuration;

    private final static FileFilter FILTER_DIRECTORY = new FileFilter() {

        public boolean accept(File pathname) {
            return pathname.isDirectory() && !pathname.isHidden();
        }

    };

    private final static FileFilter FILTER_INCLUDE_FILE = new FileFilter() {

        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".xml") && !pathname.getName().equals("statuscodeinfo.xml");
        }

    };

    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }

    public void setPathResolverService(PathResolverService pathresolver) {
        this.pathresolver = pathresolver;
    }

    public void setThemeFactoryService(ThemeFactoryService themefactory) {
        this.themefactory = themefactory;
    }

    public void setBackupService(BackupService backup) {
        this.backup = backup;
    }

    public void setConfigurationService(ConfigurationService configuration) {
        this.configuration = configuration;
    }

    public void init() {
        File docroot = new File(this.pathresolver.resolve("."));
        File[] directories = docroot.listFiles(FILTER_DIRECTORY);
        HashSet<File> includeFiles = new HashSet<File>();
        for (int i = 0; i < directories.length; i++) {
            includeFiles.addAll(this.findIncludeFiles(directories[i], false));
        }

        this.files = new HashMap<String, IncludeFile>();

        for (Iterator<File> i = includeFiles.iterator(); i.hasNext();) {
            File file = (File) i.next();
            String path = file.getAbsolutePath().substring(
                    docroot.getAbsolutePath().length() + 1);
            IncludeFile incFile = new DynIncludeFileImpl(themefactory,
                    configuration, backup, filesystem, pathresolver,
                    path);
            this.files.put(path, incFile);
        }
    }

    private Collection<File> findIncludeFiles(File dir, boolean inDynTxt) {
        File[] directories = dir.listFiles(FILTER_DIRECTORY);
        HashSet<File> includeFiles = new HashSet<File>();
        for (int i = 0; i < directories.length; i++) {
            if (directories[i].getName().equals("dyntxt")) {
                includeFiles
                        .addAll(this.findIncludeFiles(directories[i], true));
            } else {
                includeFiles.addAll(this.findIncludeFiles(directories[i],
                        inDynTxt));
            }
        }
        if (inDynTxt) {
            File[] files = dir.listFiles(FILTER_INCLUDE_FILE);
            for (int i = 0; i < files.length; i++) {
                includeFiles.add(files[i]);
            }
        }
        return includeFiles;
    }

    public Collection<IncludeFile> getDynIncludeFiles() {
        return new TreeSet<IncludeFile>(this.files.values());
    }

    public IncludeFile getIncludeFile(String path) {
        return (IncludeFile) this.files.get(path);
    }

}
