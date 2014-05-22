package org.pustefixframework.ide.eclipse.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;

import org.pustefixframework.ide.eclipse.plugin.ui.util.StatusInfo;

public class PathValidator {

	private static String filePatternStr="[^\\|\\\\\\?\\*<\":>/]+";
	private static String pathPatternStr="/?"+filePatternStr+"(/"+filePatternStr+")*"+"/?";
	
	private static Pattern pattern=Pattern.compile(pathPatternStr);
	
	public static IStatus validate(Object obj) {
		IStatus result=StatusInfo.OK_STATUS;
		if(obj instanceof String) {
			String path=(String)obj;
			Matcher matcher=pattern.matcher(path);
			if(!matcher.matches()) {
				result=new StatusInfo(IStatus.ERROR,"Invalid path: "+path);
			}
		}
		return result;
	}
	
}
