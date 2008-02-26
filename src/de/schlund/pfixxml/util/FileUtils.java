package de.schlund.pfixxml.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class contains a set of commonly used utility methods for files.
 * 
 * @author mleidig@schlund.de
 *
 */
public class FileUtils {

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
    
    
}
