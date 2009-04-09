/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example.webservices;

import de.schlund.pfixcore.beans.Alias;
import de.schlund.pfixcore.beans.Exclude;

public class WeirdBean {

    int foo=1;
    int bar=2;
    int baz=3;
    
    @Alias("public")
    public int pub=4;
    
    int ro=5;
    int wo=6;
    
    public static int staticMember=7;
    //Not supported by JAXWS:
    //public final int finalMember=8;
    
    @Exclude
    public int blah=9;
    
    public int getFoo() {return foo;}
    public void setFoo(int foo) {this.foo=foo;}

    @Exclude
    public int getBar() {return bar;}
    public void setBar(int bar) {this.bar=bar;}

    @Alias("test")
    public int getBaz() {return baz;}
    public void setBaz(int baz) {this.baz=baz;}
    
    public int getRo() {
        return ro;
    }
    
    public void setRo(int wo) {
        this.wo=wo;
    }
    
}
