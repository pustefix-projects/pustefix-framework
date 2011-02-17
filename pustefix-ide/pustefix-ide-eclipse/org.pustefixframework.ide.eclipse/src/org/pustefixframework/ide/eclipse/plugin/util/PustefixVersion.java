package org.pustefixframework.ide.eclipse.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PustefixVersion {
	
    private static String VERSION_PATTERN_STR = "(\\d+)(\\.(\\d+))?(\\.(\\d+))?(-(\\w+))?";
    private static Pattern VERSION_PATTERN = Pattern.compile(VERSION_PATTERN_STR);
    
	private static String JAR_VERSION_PATTERN_STR = "pustefix-core-(" + VERSION_PATTERN_STR + ").jar";
	private static Pattern JAR_VERSION_PATTERN = Pattern.compile(JAR_VERSION_PATTERN_STR);
	
	private int majorVersion;
	private int minorVersion;
	private int microVersion;
	private String qualifier;
	
	public static PustefixVersion parseVersion(String version) {
		Matcher matcher = JAR_VERSION_PATTERN.matcher(version);
		if(matcher.matches()) {
			PustefixVersion pv = new PustefixVersion();
			pv.setMajorVersion(Integer.parseInt(matcher.group(2)));
			if(matcher.group(4) != null) pv.setMinorVersion(Integer.parseInt(matcher.group(4)));
			if(matcher.group(6) != null) pv.setMicroVersion(Integer.parseInt(matcher.group(6)));
			if(matcher.group(8) != null) pv.setQualifier(matcher.group(8));
			return pv;
		} else {
			matcher = VERSION_PATTERN.matcher(version);
			if(matcher.matches()) {
				PustefixVersion pv = new PustefixVersion();
				pv.setMajorVersion(Integer.parseInt(matcher.group(1)));
				if(matcher.group(3) != null) pv.setMinorVersion(Integer.parseInt(matcher.group(3)));
				if(matcher.group(5) != null) pv.setMicroVersion(Integer.parseInt(matcher.group(5)));
				if(matcher.group(7) != null) pv.setQualifier(matcher.group(7));
				return pv;
			}
		}
		return null;
	}
	
	public int getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}
	public int getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}
	public int getMicroVersion() {
		return microVersion;
	}
	public void setMicroVersion(int microVersion) {
		this.microVersion = microVersion;
	}
	
	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	
	@Override
	public String toString() {
		return majorVersion + "." + minorVersion + "." + microVersion
		        + (qualifier == null ? "" : "-" + qualifier);
	}
	
}
