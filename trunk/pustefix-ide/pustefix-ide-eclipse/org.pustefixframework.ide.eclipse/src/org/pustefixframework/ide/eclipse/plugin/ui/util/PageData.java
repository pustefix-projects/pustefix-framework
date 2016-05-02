package org.pustefixframework.ide.eclipse.plugin.ui.util;

import java.util.HashMap;
import java.util.Map;

public class PageData {

	private Map<String,Object> map;
	
	public PageData() {
		map=new HashMap<String,Object>();
	}
	
	public Object get(String key) {
		return map.get(key);
	}
	
	public void put(String key,Object value) {
		map.put(key,value);
	}
	
}
