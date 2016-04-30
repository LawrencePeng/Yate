package com.llpeng.yate.render;

/**
 * Created by lawrence on 16/4/28.
 */
public class VariableToken extends Token {

    public VariableToken(final String name,
                         final String value,
                         final boolean escape) {
        super(name, Type.VARIABLE, value, null, null, escape);
    }
}
