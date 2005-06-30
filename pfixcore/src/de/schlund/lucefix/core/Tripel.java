package de.schlund.lucefix.core;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public interface Tripel {
    public String getFilename();
    public String getPart();
    public String getProduct();
    public String getPath();
    public byte getType();
    public byte setType(byte newType);
}
