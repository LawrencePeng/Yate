package com.llpeng.yate.token;

/**
 * Created by lawrence on 16/4/30.
 */
public class InvertedToken extends Token {

    public InvertedToken(String name, String value) {
        super(name, Type.INVERTED, value, null, null, false);
    }
}
