package de.schlund.pfixxml;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class AppVariantInfo {
    
    private List<AppVariant> appVariants;
    
    public AppVariant getMatchingAppVariant(HttpServletRequest req) {
        for(AppVariant appVariant: appVariants) {
            if(appVariant.matches(req)) {
                return appVariant;
            }
        }
        return null;
    }
    
    public void setAppVariants(List<AppVariant> appVariants) {
        this.appVariants = appVariants;
    }

}
