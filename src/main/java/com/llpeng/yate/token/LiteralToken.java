package com.llpeng.yate.token;

/**
 * Created by lawrence on 16/4/28.
 */
public class LiteralToken extends Token {
    public LiteralToken(final String value) {
        super("literal", Type.LITERAL, value, null, null, true);
    }
}
