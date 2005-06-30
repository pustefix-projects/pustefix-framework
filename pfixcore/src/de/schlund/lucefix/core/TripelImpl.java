package de.schlund.lucefix.core;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class TripelImpl implements Tripel {
    
    public static final byte INDEX = 1;
//    public static final byte UPDATE = 2;
    public static final byte DELETE = 3;

    private String product;
    private String part;
    private String filename;
    
    private byte type = 0;

    public byte getType() {
        return type;
    }
    
    public byte setType(byte newType) {
        byte retval = type;
        type = newType;
        return retval;
    }

    /**
     * @param product
     * @param part
     * @param filename
     */
    public TripelImpl(String product, String part, String filename, byte type) {
        super();
        this.product = product;
        this.part = part;
        this.filename = filename;
        this.type = type;
    }
    public TripelImpl(String product, String part, String filename){
        super();
        this.product = product;
        this.part = part;
        this.filename = filename;
        this.type = TripelImpl.INDEX;
    }
    public TripelImpl(String fillpath, byte type){
        super();
        int schnippel2 = fillpath.lastIndexOf("/");
        int schnippel1 = fillpath.lastIndexOf("/",schnippel2-1);
        filename = fillpath.substring(0,schnippel1);
        part = fillpath.substring(schnippel1+1,schnippel2);
        product = fillpath.substring(schnippel2+1,fillpath.length());
        this.type = type;
        
    }
    /*
     * @see de.schlund.lucefix.core.Tripel#getFilename()
     */
    public String getFilename() {
        return filename;
    }

    /*
     * @see de.schlund.lucefix.core.Tripel#getPart()
     */
    public String getPart() {
        return part;
    }

    /*
     * @see de.schlund.lucefix.core.Tripel#getProduct()
     */
    public String getProduct() {
        return product;
    }
    
    

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getFilename());
        sb.append("|");
        sb.append(getPart());
        sb.append("|");
        sb.append(getProduct());
        return sb.toString();
    }
    /**
     * @param current
     * @return
     */
    public String getPath() {
        StringBuffer sb = new StringBuffer(getFilename());
        // TODO: seperator irgendwo herholen?
        sb.append("/");
        sb.append(getPart());
        sb.append("/");
        sb.append(getProduct());
        return sb.toString();
    }
}
