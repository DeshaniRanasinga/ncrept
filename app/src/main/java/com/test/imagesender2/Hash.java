package com.test.imagesender2;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by VDARSSH on 7/16/2015.
 */
public class Hash {
        private static String convertToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();
            for (byte b : data) {
                int halfbyte = (b >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                    halfbyte = b & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            byte[] returnarray=md.digest();
            for (int i = 0; i < sha1hash.length; i++) {
                returnarray = new byte[sha1hash.length];  // <---- wipes out previous values
                returnarray[i] = (byte) (((int) sha1hash[i]) ^ ((int) sha1hash[i]));
            }
            return convertToHex(returnarray);
        }

}
