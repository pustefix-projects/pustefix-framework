package de.schlund.pfixcore.util;


public interface TokenManager {

    public void invalidateToken(String tokenName);
    public String getToken(String tokenName);
    public boolean isValidToken(String tokenName,String token);
    
}
