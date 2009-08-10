package org.pustefixframework.xmlgenerator.config.model;

import java.util.List;


/**
 * This class models a page within the navigation.
 * 
 * @author mleidig@schlund.de
 *
 */
public class Page extends AbstractModelElement implements ModelChangeListener {

	private String name;
	private String handler;
	private String accessKey;
	private ExtensibleList<Page> childPages = new ExtensibleList<Page>();
	
	public Page(String name, String handler, String accessKey) {
		this.name = name;
		this.handler = handler;
		this.accessKey = accessKey;
		childPages.addModelChangeListener(this);
	}
	
	public List<Page> getChildPages() {
		return childPages;
	}
	
	public void addChildPage(Page page) {
		childPages.add(page);
	}
	
	public String getName() {
		return name;
	}
	
	public String getHandler() {
		return handler;
	}
	
	public String getAccessKey() {
		return accessKey;
	}
	
}
