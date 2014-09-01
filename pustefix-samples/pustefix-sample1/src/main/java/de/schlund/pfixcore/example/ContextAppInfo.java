package de.schlund.pfixcore.example;

import javax.xml.bind.annotation.XmlAttribute;

import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.Tenant;

public class ContextAppInfo {
	
	private Context context;
	
	public String getLanguage() {
		return context.getLanguage();
	}
	
	@XmlAttribute
	public String getTenant() {
		Tenant tenant = context.getTenant();
		if(tenant != null) {
			return tenant.getName();
		}
		return null;
	}
	
	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}

}
