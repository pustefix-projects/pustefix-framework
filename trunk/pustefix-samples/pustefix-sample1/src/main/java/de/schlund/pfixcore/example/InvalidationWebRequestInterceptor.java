package de.schlund.pfixcore.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.pustefixframework.http.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

public class InvalidationWebRequestInterceptor implements WebRequestInterceptor {
	
	private ContextAdultInfo adultInfo;
	
	@Autowired
	public void setAdultInfo(ContextAdultInfo adultInfo) {
		this.adultInfo = adultInfo;
	}
	
	public void preHandle(WebRequest request) throws Exception {
		
		if(request instanceof ServletWebRequest) {
			HttpServletRequest httpRequest = ((ServletWebRequest)request).getRequest();
			HttpSession session = httpRequest.getSession(false);
			if(session != null) {
				String param = request.getParameter("invalidate");
				if(param != null) {
					if(adultInfo.getAdult() != null) {
						SessionUtils.invalidate(session);
					}
				}
			}
		}
	}

	public void postHandle(WebRequest request, ModelMap model) throws Exception {
	}
	
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
	}
	
}
