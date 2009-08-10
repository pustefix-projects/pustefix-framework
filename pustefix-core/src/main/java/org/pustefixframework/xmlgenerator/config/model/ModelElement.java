package org.pustefixframework.xmlgenerator.config.model;

public interface ModelElement {

	public void addModelChangeListener(ModelChangeListener changeListener);
	public void notifyModelChangeListeners(ModelChangeEvent changeEvent);
	
}
