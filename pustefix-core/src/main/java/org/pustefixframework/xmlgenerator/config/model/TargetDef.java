package org.pustefixframework.xmlgenerator.config.model;

import java.util.List;



public class TargetDef extends AbstractModelElement implements ParameterConfig, ThemeConfig, VariantConfig {

	public enum Type {XML, XSL};
	
	private String name;
	private Type type;
	private String[] themes;
	private String page;
	private String variant;
	private String xml;
	private String xsl;
	private ExtensibleList<DependencyDef> auxDependencies = new ExtensibleList<DependencyDef>();
	private ExtensibleList<Parameter> parameters = new ExtensibleList<Parameter>();
	
	public TargetDef() {
		auxDependencies.addModelChangeListener(this);
		parameters.addModelChangeListener(this);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public String[] getThemes() {
		return themes;
	}
	
	public void setThemes(String[] themes) {
		this.themes = themes;
	}
	
	public String getPage() {
		return page;
	}
	
	public void setPage(String page) {
		this.page = page;
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

	public String getXSL() {
		return xsl;
	}
	
	public void setXSL(String xsl) {
		this.xsl = xsl;
	}

	public List<DependencyDef> getAuxiliaryDependencies() {
		return auxDependencies;
	}
	
	public void addAuxiliaryDependency(String auxDependency) {
		auxDependencies.add(new DependencyDef(auxDependency));
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}

	public void addParameter(String name, String value) {
		parameters.add(new Parameter(name, value));
	}
	
}
