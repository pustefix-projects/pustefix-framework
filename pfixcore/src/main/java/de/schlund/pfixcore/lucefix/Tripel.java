/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.lucefix;


public class Tripel implements Comparable<Tripel> {

    private Type type;
    private String product;
    private String part;
    private String filename;


    /**
     * @param product
     * @param part
     * @param filename
     */
    public Tripel(String product, String part, String filename, Type type) {
        this.product = product;
        this.part = part;
        this.filename = filename;
        this.type = type;
    }
    public Tripel(String product, String part, String filename){
        this.product = product;
        this.part = part;
        this.filename = filename;
        this.type = Type.INSERT;
    }
    public Tripel(String fullpath, Type type){
        int schnippel2 = fullpath.lastIndexOf("/");
        int schnippel1 = fullpath.lastIndexOf("/",schnippel2-1);
        filename = fullpath.substring(0,schnippel1);
        part = fullpath.substring(schnippel1+1,schnippel2);
        product = fullpath.substring(schnippel2+1,fullpath.length());
        this.type = type;
    }
    public Type setType(Type newType) {
        Type retval = type;
        type = newType;
        return retval;
    }
    public Type getType(){
        return type;
    }
    public String getFilename() {
        return filename;
    }

    public String getPart() {
        return part;
    }

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

    public boolean equals(Object arg0) {
        if (arg0 instanceof Tripel) {
            Tripel c = (Tripel) arg0;
            return (c.getFilename().equals(getFilename()) && c.getPart().equals(getPart()) && c.getProduct().equals(getProduct()));
        }
        return false;
    }

    public int hashCode() {
        return getPath().hashCode();
    }
    
    public enum Type{
        INSERT, DELETE, EDITORUPDATE;
    }

    public int compareTo(Tripel a) {
        int result;
        result = a.getFilename().compareTo(this.getFilename());
        if (result == 0){
            result = a.getPart().compareTo(this.getPart());
            if (result == 0){
                result = a.getProduct().compareTo(this.getProduct());
            }
        }
        return result;
    }
}
