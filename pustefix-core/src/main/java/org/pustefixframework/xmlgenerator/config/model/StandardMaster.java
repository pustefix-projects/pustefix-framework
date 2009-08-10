package org.pustefixframework.xmlgenerator.config.model;

import java.net.URI;
import java.util.List;



public class StandardMaster extends AbstractModelElement implements IncludeConfig, ParameterConfig {

	private String name;
	private ExtensibleList<IncludeDef> includes = new ExtensibleList<IncludeDef>();
	private ExtensibleList<Parameter> parameters = new ExtensibleList<Parameter>();
	private SourceInfo sourceInfo;
	
	public StandardMaster() {
		includes.addModelChangeListener(this);
		parameters.addModelChangeListener(this);
	}
	
	public List<IncludeDef> getIncludes() {
		return includes;
	}
	
	public void addInclude(URI uri) {
		includes.add(new IncludeDef(uri));
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void addParameter(String name, String value) {
		parameters.add(new Parameter(name,value));
	}
	
	public void setSourceInfo(SourceInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
	
	public SourceInfo getSourceInfo() {
		return sourceInfo;
	}
	
}
