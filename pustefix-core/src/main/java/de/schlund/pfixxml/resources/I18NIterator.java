package de.schlund.pfixxml.resources;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.pustefixframework.util.LocaleUtils;

public class I18NIterator implements Iterator<String> {

    private String tenantName;
    private String language;
    private String path;
    private String languagePart;
    private String pathStart;
    private String pathEnd;
    private int step;

    public I18NIterator(String tenantName, String language, String path) {
        this.tenantName = tenantName;
        if(tenantName != null && tenantName.length() == 0) {
        	this.tenantName = null;
        }
        this.language = language;
        this.path = path;
        if(language != null) {
            languagePart = LocaleUtils.getLanguagePart(language);
            if(language.equals(languagePart)) {
                languagePart = null;
            }
        }
        int ind = path.lastIndexOf('/');
        if(ind > -1) {
            pathStart = path.substring(0, ind + 1);
            pathEnd = path.substring(ind + 1);
        } else {
            pathStart = "";
            pathEnd = path;
        }
    }

    @Override
    public boolean hasNext() {
        return step < 6;
    }

    @Override
    public String next() {
        if(step == 0) {
            step++;
            if(tenantName != null && language != null) {
                return pathStart + tenantName + "/" + language + "/" + pathEnd;
            }
        }
        if(step == 1) {
            step++;
            if(tenantName != null && languagePart != null) {
                return pathStart + tenantName + "/" + languagePart + "/" + pathEnd;
            }
        }
        if(step == 2) {
            step++;
            if(tenantName != null) {
                return pathStart + tenantName + "/" + pathEnd;
            }
        }
        if(step == 3) {
            step++;
            if(language != null) {
                return pathStart + language + "/" + pathEnd;
            }
        }
        if(step == 4) {
            step++;
            if(languagePart != null) {
                return pathStart + languagePart + "/" + pathEnd;
            }
        }
        if(step == 5) {
            step++;
            return path;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        //ignore
    }

}