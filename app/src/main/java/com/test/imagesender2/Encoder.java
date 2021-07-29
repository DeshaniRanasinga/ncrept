package com.test.imagesender2;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by VDARSSH on 7/6/2015.
 */
public class Encoder {
    KeyPairGenerator kpg;
    KeyPair kp;
    PublicKey publicKey;
    PrivateKey privateKey;
    byte[] encryptedBytes, decryptedBytes;
    Cipher cipher, cipher1;
    String encrypted, decrypted;

    //constructor of encoder
    public Encoder(String keySize) throws NoSuchAlgorithmException {
        int size=Integer.parseInt(keySize);
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(size);
        kp = kpg.genKeyPair();
        privateKey=kp.getPrivate();
        publicKey=kp.getPublic();

    }
    public Encoder(){

    }

    public KeyPair genNewKey(String keySize) throws NoSuchAlgorithmException {

        int size=Integer.parseInt(keySize);
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(size);
        kp = kpg.genKeyPair();



        return kp;
    }
    public String Encrypt (String plain) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(64);
        kp = kpg.genKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();

        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encryptedBytes = cipher.doFinal(plain.getBytes());

        encrypted = bytesToString(encryptedBytes);
        return encrypted;

    }

    public String Decrypt (String result) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {

        cipher1=Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedBytes = cipher1.doFinal(stringToBytes(result));
        decrypted = new String(decryptedBytes);
        return decrypted;

    }

    public static String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    public static  byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }

    public static String DecryptFromKey (String result,PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher decryptCipher;
        decryptCipher=Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = decryptCipher.doFinal(stringToBytes(result));
        String decrypted = new String(decryptedBytes);
        return decrypted;

    }
    public static String EncryptFromKey (String result,PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher encryptCipher;
        encryptCipher=Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = encryptCipher.doFinal(result.getBytes());

        String encrypted = bytesToString(encryptedBytes);
        return encrypted;

    }
}
