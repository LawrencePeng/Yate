package com.llpeng.yate.render;

import java.util.List;

/**
 * Created by lawrence on 16/4/28.
 */
public class RootToken extends Token {

    public RootToken(final String name, final List<Token> children) {
        super(name, Type.ROOT, null, null, children, true);
    }


}
