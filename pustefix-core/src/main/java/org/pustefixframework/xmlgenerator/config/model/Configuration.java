package org.pustefixframework.xmlgenerator.config.model;

import java.net.URI;
import java.util.List;


import de.schlund.pfixxml.util.XsltVersion;

public class Configuration extends AbstractModelElement implements IncludeConfig, ParameterConfig {

	private String project;
	private String language;
	private String[] themes;
	private XsltVersion xsltVersion;
	private ExtensibleList<IncludeDef> commonIncludes = new ExtensibleList<IncludeDef>();
	private ExtensibleList<Parameter> commonParameters = new ExtensibleList<Parameter>();
	private ExtensibleList<NamespaceDeclaration> namespaces = new ExtensibleList<NamespaceDeclaration>();
	private ExtensibleList<Page> pages = new ExtensibleList<Page>();
	private ExtensibleList<StandardMetatags> standardMetatags = new ExtensibleList<StandardMetatags>();
	private ExtensibleList<StandardMaster> standardMasters = new ExtensibleList<StandardMaster>();
	private ExtensibleList<StandardPage> standardPages = new ExtensibleList<StandardPage>();
	private ExtensibleList<TargetDef> targetDefs = new ExtensibleList<TargetDef>();
	
	public Configuration() {
		commonIncludes.addModelChangeListener(this);
		commonParameters.addModelChangeListener(this);
		namespaces.addModelChangeListener(this);
		pages.addModelChangeListener(this);
		standardMetatags.addModelChangeListener(this);
		standardMasters.addModelChangeListener(this);
		standardPages.addModelChangeListener(this);
		targetDefs.addModelChangeListener(this);
	}
	
	public void modelChanged(ModelChangeEvent event) {
		notifyModelChangeListeners(event);
	}
	
	public String getProject() {
		return project;
	}
	
	public void setProject(String project) {
		this.project = project;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String[] getThemes() {
		return themes;
	}
	
	public void setThemes(String[] themes) {
		this.themes = themes;
	}
	
	public XsltVersion getXsltVersion() {
		return xsltVersion;
	}
	
	public void setXsltVersion(XsltVersion xsltVersion) {
		this.xsltVersion = xsltVersion;
	}
	
	public List<IncludeDef> getIncludes() {
		return commonIncludes;
	}
	
	public void addInclude(URI uri) {
		commonIncludes.add(new IncludeDef(uri));
	}

	public List<Parameter> getParameters() {
		return commonParameters;
	}
	
	public void addParameter(String name, String value) {
		commonParameters.add(new Parameter(name, value));
	}
	
	public List<NamespaceDeclaration> getNamespaceDeclarations() {
		return namespaces;
	}
	
	public void addNamespaceDeclaration(NamespaceDeclaration namespace) {
		namespaces.add(namespace);
	}
	
	public List<Page> getPages() {
		return pages;
	}
	
	public void addPage(Page page) {
		pages.add(page);
	}

	public List<StandardMaster> getStandardMasters() {
		return standardMasters;
	}

	public void addStandardMaster(StandardMaster standardMaster) {
		standardMasters.add(standardMaster);
	}
	
	public List<StandardMetatags> getStandardMetatags() {
		return standardMetatags;
	}
	
	public void addStandardMetatags(StandardMetatags standardMetatags) {
		this.standardMetatags.add(standardMetatags);
	}

	public List<StandardPage> getStandardPages() {
		return standardPages;
	}

	public void addStandardPage(StandardPage standardPage) {
		standardPages.add(standardPage);
	}
	
	public List<TargetDef> getTargetDefs() {
		return targetDefs;
	}
	
	public void addTargetDef(TargetDef targetDef) {
		targetDefs.add(targetDef);
	}

}
