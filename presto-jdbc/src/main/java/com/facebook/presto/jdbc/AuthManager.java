package com.facebook.presto.jdbc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Created by liubangchen on 2018/1/18.
 */
public class AuthManager {

    static char md5String[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};

    public static String distAuthInfo(String user, String password) throws RuntimeException {
        try {
            Mac hasher = Mac.getInstance("HmacSHA256");
            hasher.init(new SecretKeySpec("emr-presto".getBytes(), "HmacSHA256"));
            byte[] btInput = password.getBytes();
            byte[] hash = hasher.doFinal(password.getBytes("utf-8"));
            String enpawd =DatatypeConverter.printHexBinary(hash).toLowerCase();
            String authinfo = user + ":" + enpawd;
            return "Basic "+Base64.getEncoder().encodeToString(authinfo.getBytes("utf-8"));
        } catch (Exception e) {
            throw new RuntimeException("distAuthInfo exception", e);
        }


    }
}
