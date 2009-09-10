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
package org.pustefixframework.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class contains a set of commonly used utility methods for files.
 * 
 * @author mleidig@schlund.de
 *
 */
public class FileUtils {
	
	/**
	 * Deletes file. Non-empty directories are recursively removed.
	 * 
	 * @param file - the file or directory, which should be deleted
	 */
	public static void delete(File file) {
		if(file.isDirectory()) {
			File[] children = file.listFiles();
			for(File child: children) {
				delete(child);
			}
		}
		file.delete();
	}

    /**
     * Reads a text file into a string and replaces each substring that matches the regular
     * expression by a given replacement. Then the changed string is stored back to the file.
     * 
     * @param file - the text file
     * @param encoding - the file content's encoding
     * @param regexp - the regular expression to match substrings
     * @param replacement - the replacement for matched substrings
     */
    public static void searchAndReplace(File file, String encoding, String regexp, String replacement) {
        try {
            String content = load(file, encoding);
            content = content.replaceAll(regexp, replacement);
            save(content, file, encoding);
        } catch (IOException x) {
            throw new RuntimeException("Search and replace failed due to IO error", x);
        }
    }

    /**
     * Read a text file into a string.
     * 
     * @param file - the text file
     * @param encoding - text file content's encoding
     * @return the file content as string
     * @throws IOException
     */
    public static String load(File file, String encoding) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(fis, encoding);
        StringBuffer strBuf = new StringBuffer();
        char[] buffer = new char[4096];
        int i = 0;
        try {
            while ((i = reader.read(buffer)) != -1)
                strBuf.append(buffer, 0, i);
        } finally {
            fis.close();
        }
        return strBuf.toString();
    }

    /**
     * Saves a string to a text file.
     * 
     * @param fileContent - the file content string
     * @param file - the target file
     * @param encoding - the text encoding
     * @throws IOException
     */
    public static void save(String fileContent, File file, String encoding) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(fos, encoding);
        try {
            writer.write(fileContent);
            writer.flush();
        } finally {
            fos.close();
        }
    }
    
    /**
     * Copies files from source to destination dir. The files can be filtered by name using one
     * or more regular expressions (e.g. ".*gif", ".*jpg").
     * 
     * @param srcDir source directory
     * @param destDir destination directory
     * @param regexps regular expressions for file names
     * @throws IOException
     */
    public static void copyFiles(File srcDir, File destDir, String... regexps) throws IOException {
        if (!(srcDir.exists() && srcDir.isDirectory()))
            throw new IllegalArgumentException("Source directory doesn't exist: " + srcDir.getAbsolutePath());
        if (!(destDir.exists() && destDir.isDirectory()))
            throw new IllegalArgumentException("Destination directory doesn't exist: " + destDir.getAbsolutePath());
        File[] files = srcDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                boolean matches = true;
                for (String regexp : regexps) {
                    matches = name.matches(regexp);
                    if (matches) break;
                }
                if (matches) {
                    File destFile = new File(destDir, file.getName());
                    copyFile(file, destFile);
                }
            }
        }
    }
    
    /**
     * Copies a source file to a target file.
     * 
     * @param srcFile source file
     * @param destFile target file
     * @throws IOException
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (!(srcFile.exists() && srcFile.isFile())) throw new IllegalArgumentException("Source file doesn't exist: " + srcFile.getAbsolutePath());
        if (destFile.exists() && destFile.isDirectory())
            throw new IllegalArgumentException("Destination file is directory: " + destFile.getAbsolutePath());
        FileInputStream in = new FileInputStream(srcFile);
        FileOutputStream out = new FileOutputStream(destFile);
        byte[] buffer = new byte[4096];
        int no = 0;
        try {
            while ((no = in.read(buffer)) != -1)
                out.write(buffer, 0, no);
        } finally {
            in.close();
            out.close();
        }
    }
    
    /**
     * Creates symbolic link using the Unix ln command.
     * Links into subdirectories can be made relative.
     * (should be replaced in the future by the according java.nio.file facilities)
     * 
     * @param src - the link source (including the link name)
     * @param target - the link target
     * @param relative - specifies if links into subdirectories should be made relative
     * @return the link's file object
     * @throws IOException
     */
	public static File createSymbolicLink(File src, File target, boolean relative) throws IOException {
		if(src.exists()) throw new IllegalArgumentException("Link source file already exists: " + src.getCanonicalPath());
		if(!target.exists()) throw new IllegalArgumentException("Link target file doesn't exist: " + target.getCanonicalPath());
		StringWriter writer = new StringWriter();
		PrintWriter output = new PrintWriter(writer);
		String targetPath = target.getCanonicalPath();
		String srcPath = src.getCanonicalPath();
		if(relative) {
			targetPath = getRelativePath(src, target);
		}
		String cmd = "ln -s " + targetPath + " " + srcPath;
		int res = RuntimeExecutor.exec(cmd, output);
		if(res != 0) {
			throw new IOException("Error creating symbolic link: " + writer.toString());
		}
		output.close();
		return target;
	}
	
	/**
	 * Get relative path from source to target file (only supports
	 * forward-relativity, no backwards-walking with "..")
	 * 
	 * @param src - source file
	 * @param target - target file
	 * @return relative path from source to target file
	 * @throws IOException
	 */
	private static String getRelativePath(File src, File target) throws IOException {
		if(src.getParentFile() != null && target.getParentFile() != null) {
			String srcPath = src.getParentFile().getCanonicalPath();
			String targetPath = target.getParentFile().getCanonicalPath();
			if(targetPath.startsWith(srcPath)) {
				if(targetPath.length() == srcPath.length()) {
					return target.getName();
				} else {
					return targetPath.substring(srcPath.length() + 1) + "/" + target.getName();
				}
			}
		}
		return target.getCanonicalPath();
	}
    
}
