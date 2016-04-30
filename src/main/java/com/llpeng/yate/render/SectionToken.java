package com.llpeng.yate.render;

/**
 * Created by lawrence on 16/4/28.
 */
public class SectionToken extends Token {

    public SectionToken(final String name,
                        final String value) {
        super(name, Type.SECTION, value, null, null, false);
    }
}
