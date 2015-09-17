package de.schlund.pfixcore.example;

import javax.xml.bind.annotation.XmlAttribute;

import org.pustefixframework.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.Tenant;

public class ContextAppInfo {
	
	private Context context;
	private long startTime = System.currentTimeMillis();
	private long changeTime;
	
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
	
	public long getTime() {
	    return System.currentTimeMillis();
	}
	
	public long getStartTime() {
	    return startTime;
	}
	
	public long getChangeTime() {
	    return changeTime;
	}
	
	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}
	
    @Subscribe
    public void listen(AdultInfoChangeEvent event) {
        changeTime = System.currentTimeMillis();
    }

}
