package org.pustefixframework.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.pustefixframework.config.contextxmlservice.ContextConfig;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixcore.workflow.SessionStatusListener;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.Variant;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Mock the Context for unit tests.
 * Provides methods to programmatically set nearly every aspect.
 * 
 * This class is just a very simple implementation of the Context
 * interface, which allows to set the current state for testing
 * single classes. But it doesn't implement the behaviour of the
 * real Context implementation, e.g. you can't test a complete
 * pageflow using this implementation.
 * 
 * @author mleidig@schlund.de
 *
 */
public class MockContext implements Context {

    private ContextResourceManager resourceManager;
    private Variant variant;
    private String lang;
    private String visitId;
    private Properties properties;
    private Properties currentPageProperties;
    private PfixServletRequest pfixReq;
    private Throwable lastException;
    private Cookie[] cookies;
    private boolean prohibitContinue;
    private String jumpToPageFlow;
    private String jumpToPage;
    private boolean precedingFlowNeedsData;
    private boolean stateMustSupplyFullDoc;
    private Authentication authentication;
    private PageRequest pageRequest;
    private PageRequestStatus pageRequestStatus;
    private List<StatusCodeInfo> messages   = new ArrayList<StatusCodeInfo>();
    private Map<Object,Properties> contextResourceProps = new HashMap<Object,Properties>();
    private Set<String> pageNeedsDataSet = new HashSet<String>();
    private Set<String> pageAccessibleSet = new HashSet<String>();
    
    
    public void addCookie(Cookie cookie) {
        //do nothing
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        messages.add(new StatusCodeInfo(scode, args, level));
    }

  
    public PageRequest createPageRequest(String name) {
        // not supported
        return null;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
    
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    
    public ContextConfig getContextConfig() {
        // not supported
        return null;
    }

    
    public PageRequest getCurrentPageRequest() {
        return pageRequest;
    }
    
    public void setCurrentPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }

    public PageRequestStatus getCurrentStatus() {
        return pageRequestStatus;
    }
    
    public void setCurrentStatus(PageRequestStatus pageRequestStatus) {
        this.pageRequestStatus = pageRequestStatus;
    }

    public String getLanguage() {
        return lang;
    }

    
    public Throwable getLastException() {
        return lastException;
    }
    
    public void setLastException(Throwable lastException) {
        this.lastException = lastException;
    }

    
    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    
    public Properties getPropertiesForContextResource(Object res) {
        return contextResourceProps.get(res);
    }
    
    public void setPropertiesForContextResource(Object res, Properties props) {
        contextResourceProps.put(res, props);
    }

    
    public Properties getPropertiesForCurrentPageRequest() {
        return currentPageProperties;
    }
    
    public void setPropertiesForCurrentPageRequest(Properties currentPageProperties) {
        this.currentPageProperties = currentPageProperties;
    }
    

    public Cookie[] getRequestCookies() {
        return cookies;
    }
    
    public void setRequestCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }
    

    public String getVisitId() {
        return visitId;
    }
    
    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }
   

    public void markSessionForCleanup() {
        // do nothing
    }

    
    public boolean precedingFlowNeedsData() throws PustefixApplicationException {
        return precedingFlowNeedsData;
    }

    public void setPrecedingFlowNeedsData(boolean precedingFlowNeedsData) {
        this.precedingFlowNeedsData = precedingFlowNeedsData;
    }
    
    
    public boolean isProhibitContinueSet() {
        return prohibitContinue;
    }
    
    public void prohibitContinue() {
        prohibitContinue = true;
    }

    
    public void addSessionStatusListener(SessionStatusListener l) {
        // do nothing
    }
    
    public void removeSessionStatusListener(SessionStatusListener l) {
        // do nothing  
    }

    public void setCurrentPageFlow(String pageflow) {
        // do nothing
    }

    
    public boolean isJumpToPageFlowSet() {
        return jumpToPageFlow != null;
    }

    public boolean isJumpToPageSet() {
        return jumpToPage != null;
    }
    
    public void setJumpToPage(String pagename) {
        jumpToPage = pagename;
    }

    public void setJumpToPageFlow(String pageflow) {
        jumpToPageFlow = pageflow;
    }

    
    public void setLanguage(String lang) {
        this.lang = lang;
    }


    public boolean stateMustSupplyFullDocument() {
        return stateMustSupplyFullDoc;
    }
    
    public void setStateMustSupplyFullDocument(boolean stateMustSupplyFullDoc) {
        this.stateMustSupplyFullDoc = stateMustSupplyFullDoc;
    }
    

    public boolean checkIsAccessible(PageRequest page) throws PustefixApplicationException {
        return checkIsAccessible(page.getName());
    }

    public boolean checkIsAccessible(String pagename) throws PustefixApplicationException {
        return pageAccessibleSet.contains(pagename);
    }
    
    public void setPageIsAccessible(String pageName) {
        pageAccessibleSet.add(pageName);
    }

    public boolean checkNeedsData(PageRequest page) throws PustefixApplicationException {
        return checkNeedsData(page.getName());
    }

    public boolean checkNeedsData(String pagename) throws PustefixApplicationException {
        return pageNeedsDataSet.contains(pagename);
    }
    
    public void setPageNeedsData(String pageName) {
        pageNeedsDataSet.add(pageName);
    }

    
    public ContextResourceManager getContextResourceManager() {
        return resourceManager;
    }
    
    public void setContextResourceManager(ContextResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    
    public PfixServletRequest getPfixServletRequest() {
        return pfixReq;
    }
    
    public void setPfixServletRequest(PfixServletRequest pfixReq) {
        this.pfixReq = pfixReq;
    }

    
    public Variant getVariant() {
        return variant;
    }
    
    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        this.variant = variant;
    }
    
}
