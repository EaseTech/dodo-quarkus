package com.example.jwt;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.microprofile.jwt.Claims;

import io.smallrye.jwt.build.Jwt;

/**
* This is a test class and should be removed before moving to production.
* You can use this class as follows to generate a JWT Token:
* mvn exec:java -Dexec.mainClass=com.example.jwt.GenerateToken -Dexec.classpathScope=test -Dsmallrye.jwt.sign.key-location=privateKey.pem
*/
public class GenerateToken {
    /**
    * Generate JWT token
    */
    public static void main(String[] args) {
        String token =
            Jwt.issuer("https://example.com/issuer")
            .upn("jdoe@quarkus.io")
            .groups(new HashSet<>(Arrays.asList("User", "Admin")))
            .claim(Claims.birthdate.name(), "2001-07-13")
            .sign();
        System.out.println(token);
    }
}