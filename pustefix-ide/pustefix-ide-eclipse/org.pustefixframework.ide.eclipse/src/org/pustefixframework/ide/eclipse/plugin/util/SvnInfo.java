package org.pustefixframework.ide.eclipse.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class SvnInfo {
	
	private String url;
	private long revision;
	private long lastChangedRevision;
	
	public void setURL(String url) {
		this.url = url;
	}
	
	public String getURL() {
		return url;
	}
	
	public void setRevision(long revision) {
		this.revision = revision;
	}
	
	public long getRevision() {
		return revision;
	}
	
	
	
	public String toString() {
		return "URL: "+url+"\nRevision: "+revision;
	}
	
	public static SvnInfo readFromSvnEntries(IFile file) throws CoreException, IOException {
		return readFromSvnEntries(file.getContents(), 8);
	}
	
	public static SvnInfo readFromSvnEntries(InputStream in, int lineNo) throws CoreException, IOException {
		SvnInfo svnInfo = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		int lineCnt = 0;
		while( ((line = reader.readLine())!=null) && ++lineCnt<lineNo && !line.trim().equals("dir"));
		if(line!=null && line.equals("dir")) {
			line = reader.readLine();
			if(line!=null) {
				long revision = Long.parseLong(line);
				line = reader.readLine();
				if(line!=null) {
					String url = line.trim();
					svnInfo = new SvnInfo();
					svnInfo.setRevision(revision);
					svnInfo.setURL(url);
				}
			}
		}
		in.close();
		return svnInfo;
	}

	public static void main(String[] args) throws Exception {
		File file = new File("/data/checkouts/pustefix.svn.sourceforge.net/pfixcore_stable/.svn/entries");
		FileInputStream in = new FileInputStream(file);
		SvnInfo svnInfo = SvnInfo.readFromSvnEntries(in, 8);
		System.out.println(svnInfo);
	}
	
}
