package org.pustefixframework.xmlgenerator.config.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModelElement implements ModelElement, ModelChangeListener {

	private List<ModelChangeListener> changeListeners = new ArrayList<ModelChangeListener>();
	
	public void addModelChangeListener(ModelChangeListener changeListener) {
		changeListeners.add(changeListener);
	}
	
	public void notifyModelChangeListeners(ModelChangeEvent event) {
		for(ModelChangeListener listener: changeListeners) {
			listener.modelChanged(event);
		}
	}
	
	public void modelChanged(ModelChangeEvent event) {
		List<ModelElement> affectedElements = new ArrayList<ModelElement>();
		affectedElements.add(this);
		ModelChangeEvent updateEvent = new ModelChangeEvent(ModelChangeEvent.Type.UPDATE, this, affectedElements);
		notifyModelChangeListeners(updateEvent);
	}
	
}
