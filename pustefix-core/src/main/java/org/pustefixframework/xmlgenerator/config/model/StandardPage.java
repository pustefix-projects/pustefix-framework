package org.pustefixframework.xmlgenerator.config.model;

import java.net.URI;
import java.util.List;



public class StandardPage extends AbstractModelElement implements IncludeConfig, ThemeConfig, VariantConfig, ParameterConfig {

	private String name;
	private String master;
	private String metatags;
	private String[] themes;
	private String variant;
	private ExtensibleList<IncludeDef> includes = new ExtensibleList<IncludeDef>();
	private ExtensibleList<Parameter> parameters = new ExtensibleList<Parameter>();
	private String xml;
	private SourceInfo sourceInfo;
	
	public StandardPage() {
		includes.addModelChangeListener(this);
		parameters.addModelChangeListener(this);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getMaster() {
		return master;
	}
	
	public void setMaster(String master) {
		this.master = master;
	}

	public String getMetatags() {
		return metatags;
	}
	
	public void setMetatags(String metatags) {
		this.metatags = metatags;
	}
	
	public String[] getThemes() {
		return themes;
	}
	
	public void setThemes(String[] themes) {
		this.themes = themes;
	}

	public String getVariant() {
		return variant;
	}
	
	public void setVariant(String variant) {
		this.variant = variant;
	}
	
	public String getXML() {
		return xml;
	}
	
	public void setXML(String xml) {
		this.xml = xml;
	}
	
	public List<IncludeDef> getIncludes() {
		return includes;
	}
	
	public void addInclude(URI include) {
		includes.add(new IncludeDef(include));
	}

	public ExtensibleList<Parameter> getParameters() {
		return parameters;
	}

	public void addParameter(String name, String value) {
		parameters.add(new Parameter(name, value));
	}
	
	public SourceInfo getSourceInfo() {
		return sourceInfo;
	}
	
	public void setSourceInfo(SourceInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
	
	@Override
	public String toString() {
		return "StandardPage: " + name;
	}
	
}
