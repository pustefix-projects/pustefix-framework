package de.schlund.lucefix.core;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class TripelImpl implements Tripel {
    
    public static final byte INDEX = 1;
//    public static final byte UPDATE = 2;
    public static final byte DELETE = 3;
//    private static Comparator comparator = new TripelImpl.TripelComparator();

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
//    public static class TripelComparator implements Comparator{
//
//        public int compare(Object arg0, Object arg1) {
//            if (arg0 instanceof TripelImpl) {
//                TripelImpl eins = (TripelImpl) arg0;
//                if (arg1 instanceof TripelImpl) {
//                    TripelImpl zwei = (TripelImpl) arg1;
//                    if (eins.equals(zwei))
//                        return 0;
//                }
//            }
//            return 1;
//        }
//        
//    }

    public boolean equals(Object arg0) {
        if (arg0 instanceof TripelImpl) {
            TripelImpl c = (TripelImpl) arg0;
            return (c.getFilename().equals(getFilename()) && c.getPart().equals(getPart()) && c.getProduct().equals(getProduct()));
        }
        return false;
    }

    public int hashCode() {
        return getPath().hashCode();
    }
    
    

//    public static Comparator getComparator() {
//        return comparator;
//    }
}
