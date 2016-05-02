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
package de.schlund.pfixxml.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * This class contains a set of commonly used utility methods for the files.
 * 
 * @author mleidig@schlund.de
 *
 */
public class FileUtils {

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
     * Recursively deletes directory.
     * 
     * @param file directory to delete
     * @return true if directory was deleted
     */
    public static boolean delete(File file) {
        if(file.isDirectory()) {
            File[] files=file.listFiles();
            for(int i=0;i<files.length;i++) {
                delete(files[i]);
            }
        }
        return file.delete();
        
    }
    
    /**
     * Create new temporary directory relative to parent directory (or java.io.tmpdir if not specified) 
     * and register it for deletion on JVM exit.
     * 
     * @param parentDir parent directory or null for java.io.tmpdir
     * @return temporary directory
     */
    public static File createTemporaryDirectory(File parentDir) throws IOException {
    	if(parentDir == null) {
    		String tmp = System.getProperty("java.io.tmpdir");
    		if(tmp != null) {
    			parentDir = new File(tmp);
    		} else {
    			parentDir = new File(".");
    		}
    	}
    	if(parentDir.exists()) {
    		String suffix = "-" + System.nanoTime(); 
    		File tmpDir = new File(parentDir, "tmp" + suffix);
    		if(tmpDir.exists()) {
    			int index = 0;
    			do {
					tmpDir = new File(parentDir, "tmp" + suffix + index);
					index++;
				} while (index < 10 && tmpDir.exists());
    			if(tmpDir.exists()) {
    				throw new IOException("Temporary directory " + tmpDir.getAbsolutePath() + " already exists.");
    			}
    		}
    		tmpDir.mkdir();
    		tmpDir.deleteOnExit();
    		return tmpDir;
    	} else {
    		throw new FileNotFoundException("Parent directory " + parentDir.getAbsolutePath() + " doesn't exist.");
    	} 
    }

    /**
     * Check if file contains binary or text data
     * 
     * @param file data file
     * @return true if data file contains binary data
     * @throws IOException
     */
    public static boolean isBinary(File file) throws IOException {
    	FileInputStream in = new FileInputStream(file);
    	try {
    		return isBinary(in);
    	} finally {
    		in.close();
    	}
    }
    
    /**
     * Check if InputStream contains binary or text data
     * 
     * @param in data input
     * @return true if input contains binary data
     * @throws IOException
     */
    public static boolean isBinary(InputStream in) throws IOException {
		
    	//read up to 1kb
    	byte[] buffer = new byte[1024];
		int offset = 0;
		int len = 1024;
		int read = 0;
		while((read = in.read(buffer, offset, len)) != -1 && len > 0) {
			offset += read;
			len -= read;
		}
		
		//check if bytes contain null byte or too much control characters for normal text formats
		int byteNo = buffer.length - len;
		int controlCount = 0;
		for(int i=0; i<byteNo; i++) {
			byte b = buffer[i];
            if(b == 0) {
            	return true;
            }
            if(b<0x07 || (b>0x0d && b<0x20) || b>0x7E) {
                controlCount++;
            }
		}
		//more than 75% control characters
		if(byteNo > 0 && controlCount * 100 / byteNo > 75) {
            return true;
        }
        return false;
	}

}
