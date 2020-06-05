package com.stego.stegoscanner;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;


public class SecurityHelper {

    private static String getKey(InputStream filename) throws IOException {
        // Read key from file
        String strKeyPEM = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            strKeyPEM += line + "\n";
        }
        br.close();
        System.out.println(strKeyPEM);
        return strKeyPEM;
    }


    public static PublicKey getPublicKey(InputStream filename) throws IOException, GeneralSecurityException {
        String publicKeyPEM = getKey(filename);
        return getPublicKeyFromString(publicKeyPEM);
    }

    public static PublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {

        String publicKeyPEM = key;
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN RSA PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("\n-----END RSA PUBLIC KEY-----\n", "");
        System.out.println(publicKeyPEM);

        byte[] encoded = Base64.decode(publicKeyPEM, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(encoded));
        System.out.println(pubKey);
        return pubKey;
    }

    public static boolean verify(PublicKey publicKey, String message, String signature) throws SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Signature sign = Signature.getInstance("SHA1withRSA");
        sign.initVerify(publicKey);
        sign.update(message.getBytes("UTF-8"));
        return sign.verify(Base64.decode(signature, Base64.DEFAULT));
    }


}
