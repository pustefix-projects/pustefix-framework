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

package de.schlund.pfixxml.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.exception.PustefixRuntimeException;

/**
 * Data structure for holding and querying the resource 
 * index entries of an application or module.
 *  
 */
public class ResourceIndexMap {

	private DirEntry rootEntry = new DirEntry();
	private String resourcePath;

	public boolean exists(String path) {
		return getEntry(path) != null;
	}

	public FileEntry getEntry(String path) {

		if(path != null) {
			if(path.startsWith("/")) path = path.substring(1);
			if(resourcePath != null) {
				if(path.startsWith(resourcePath)) {
					path = path.substring(resourcePath.length() + 1);
				} else {
					return null;
				}
			}
			DirEntry parentEntry = rootEntry;
			String[] tokens = path.split("/");
			for(int i=0; i<tokens.length; i++) {
				FileEntry entry = parentEntry.entries.get(tokens[i]);
				if(i < tokens.length - 1) {
					if(entry == null || !(entry instanceof DirEntry)) {
						return null;
					}
					parentEntry = (DirEntry)entry;
				} else {
					return entry;
				}
			}
		}
		return null;	
	}

	void addEntry(String path, Date date, long size) {

		DirEntry parentEntry = rootEntry;
		String[] tokens = path.split("/");
		for(int i=0; i<tokens.length; i++) {
			FileEntry entry = parentEntry.entries.get(tokens[i]);
			if(i < tokens.length - 1 || path.endsWith("/")) {
				if(entry == null) {
					entry = new DirEntry();
					parentEntry.entries.put(tokens[i], entry);
				} else if(!(entry instanceof DirEntry)) {
					throw new PustefixRuntimeException("The same file entry must not exist for regular file and directory: " + path);
				}
				parentEntry = (DirEntry)entry;
			} else {
				if(entry == null) {
					entry = new FileEntry();
					parentEntry.entries.put(tokens[i], entry);
				} else if(entry instanceof DirEntry) {
					throw new PustefixRuntimeException("The same file entry must not exist for regular file and directory: " + path);
				}
			}
		}
	}


	public static ResourceIndexMap read(InputStream in) throws IOException {

		ResourceIndexMap index = new ResourceIndexMap();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS Z");
        try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = reader.readLine();
            if(line != null) {
            	if(!line.equals("/")) {
            		index.resourcePath = line;
            	}
            	while((line = reader.readLine()) != null) {
            		String[] tokens = line.split("\\|");
            		String path = tokens[0];
            		path = path.replaceAll("\\\\\\|", "|");
            		Date date;
            		try {
            			date = dateFormat.parse(tokens[1]);
            		} catch(ParseException e) {
            			throw new IOException("Error reading file date: " + line, e);
            		}
            		long size = Long.parseLong(tokens[2]);
            		index.addEntry(path, date, size);
            	}
            }
        } finally {
            in.close();
        }
		return index;
	}


	class FileEntry {

		Date date;
		long size;
	}

	class DirEntry extends FileEntry {

		Map<String, FileEntry> entries = new HashMap<String, FileEntry>();
	}

}
