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

package de.schlund.pfixcore.webservice.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author mleidig@schlund.de
 */
public class FileCacheData {

        String md5;
        byte[] bytes;
        
        public FileCacheData(byte[] bytes) {
            this.bytes=bytes;
            md5=getMD5Digest(bytes);
        }
        
        public FileCacheData(String md5,byte[] bytes) {
            this.md5=md5;
            this.bytes=bytes;
        }
        
        public byte[] getBytes() {
            return bytes;
        }
        
        public String getMD5() {
            return md5;
        }
        
        private static String getMD5Digest(byte[] bytes) {
            MessageDigest digest=null;
            try {
                digest=MessageDigest.getInstance("MD5");
            } catch(NoSuchAlgorithmException x) {
                throw new RuntimeException("MD5 algorithm not supported.",x);
            }
            digest.reset();
            digest.update(bytes);
            String md5sum=bytesToString(digest.digest());
            return md5sum;
        }
                                                                                                                                   
        private static String bytesToString(byte[] b) {
            StringBuffer sb=new StringBuffer();
            for(int i=0;i<b.length;i++) sb.append(byteToString(b[i]));
            return sb.toString();
        }
                                                                                                                                       
        private static String byteToString(byte b) {
            int b1=b & 0xF;
            int b2=(b & 0xF0) >> 4;
            return Integer.toHexString(b2)+Integer.toHexString(b1);
        }
        
}
