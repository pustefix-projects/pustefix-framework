package org.pustefixframework.xmlgenerator.config.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;


/**
 * List implementation which can hold data objects and extension points mixed
 * together, but which behaves like a generic list of the data type by 
 * transparently mixing in the data objects retrieved from the extension points.
 * 
 * @author mleidig@schlund.de
 *
 * @param <T1>
 * @param <T2>
 */
public class ExtensibleList<T1 extends ModelElement> extends AbstractList<T1> implements ModelElement {

	private List<T1> elementList;
	private List<Object> mixedList;
	private ExtensionPointRegistrationListener<XMLExtensionPointImpl<T1>, XMLExtension<T1>> registrationListener;
	private List<ModelChangeListener> changeListeners;
	
	public ExtensibleList() {
		elementList = new ArrayList<T1>();
		mixedList = new ArrayList<Object>();
		registrationListener = createRegistrationListener();
		changeListeners = new ArrayList<ModelChangeListener>();
	}
	
	public void addModelChangeListener(ModelChangeListener changeListener) {
		changeListeners.add(changeListener);
	}
	
	public void notifyModelChangeListeners(ModelChangeEvent event) {
		for(ModelChangeListener changeListener:changeListeners) {
			changeListener.modelChanged(event);
		}
	}
	
	@Override
	public synchronized T1 get(int index) {
		return elementList.get(index);
	}
	
	@Override
	public synchronized int size() {
		return elementList.size();
	}

	@Override
	public void add(int index, T1 element) {
		throw new UnsupportedOperationException("Indexed modification access not allowed");
	}
	
	@Override
	public T1 set(int index, T1 element) {
		throw new UnsupportedOperationException("Indexed modification access not allowed");
	}
	
	@Override
	public boolean add(T1 element) {
		mixedList.add(element);
		rebuild();
		return true;
	}
	
	public void addExtensionPoint(XMLExtensionPointImpl<T1> extensionPoint) {
		mixedList.add(extensionPoint);
		rebuild();
		extensionPoint.registerListener(registrationListener);
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void rebuild() {
		List<T1> oldList = elementList;
		elementList = new ArrayList<T1>();
		for(Object obj:mixedList){
			if(obj instanceof XMLExtensionPointImpl<?>) {
				XMLExtensionPointImpl<T1> extPoint = (XMLExtensionPointImpl<T1>)obj;
				List<T1> list = extPoint.getElements();
				elementList.addAll(list);
			} else {
				elementList.add((T1)obj);
			}
		}
		if(oldList != null) {
			List<ModelElement> removedList = new ArrayList<ModelElement>();
			for(T1 elem:oldList) {
				if(!elementList.contains(elem)) removedList.add(elem);
			}
			if(removedList.size()>0) {
				ModelChangeEvent event = new ModelChangeEvent(ModelChangeEvent.Type.REMOVE, this, removedList);
				notifyModelChangeListeners(event);
			}
			List<ModelElement> addedList = new ArrayList<ModelElement>();
			for(T1 elem:elementList) {
				if(!oldList.contains(elem)) addedList.add(elem);
			}
			if(addedList.size()>0) {
				ModelChangeEvent event = new ModelChangeEvent(ModelChangeEvent.Type.ADD, this, addedList);
				notifyModelChangeListeners(event);
			}
		} else {
			if(elementList.size()>0) {
				ModelChangeEvent event = new ModelChangeEvent(ModelChangeEvent.Type.ADD, this, (List<ModelElement>)elementList);
				notifyModelChangeListeners(event);
			}
		}
	}
	
	private ExtensionPointRegistrationListener<XMLExtensionPointImpl<T1>, XMLExtension<T1>> createRegistrationListener() {
		return new ExtensionPointRegistrationListener<XMLExtensionPointImpl<T1>, XMLExtension<T1>>() {
			@Override
			public void afterRegisterExtension(XMLExtensionPointImpl<T1> extensionPoint, XMLExtension<T1> extension) {
				rebuild();
			}
			@Override
			public void afterUnregisterExtension(XMLExtensionPointImpl<T1> extensionPoint, XMLExtension<T1> extension) {
				rebuild();
			}
			@Override
			public void updateExtension(XMLExtensionPointImpl<T1> extensionPoint, XMLExtension<T1> extension) {
				rebuild();
			}
		};
	}
	
}
