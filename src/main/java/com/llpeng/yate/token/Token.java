package com.llpeng.yate.token;


import com.llpeng.yate.util.Constant;
import com.llpeng.yate.util.Helper;

import java.util.List;
import java.util.Map;

/**
 * Created by lawrence on 16/4/27.
 */
public abstract class Token {
    private String name;
    private Type type;
    private String value;
    private String text;
    private List<Token> children;
    private boolean escape;

    public String getValue() {
        return value;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public void setChildren(final List<Token> children) {
        this.children = children;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    //the type class of Token
    enum Type {
        LITERAL,
        VARIABLE,
        SECTION,
        INVERTED,
        COMMENT,
        ROOT,
    }

    Token(final String name,
          final Type type,
          final String value,
          final String text,
          final List<Token> children,
          final boolean escape) {

        this.name = name;
        this.type = type;
        this.value = value;
        this.text = text;
        this.children = children;
        this.escape = escape;
    }

    //token a token by its type
    public String render(final List<Map<String, Object>> contexts)
            throws Exception {
        switch (type) {
            case LITERAL:
                return renderLiteral();
            case VARIABLE:
                return renderVariable(contexts);
            case SECTION:
                return renderSection(contexts);
            case ROOT:
                return renderChildren(contexts);
            case COMMENT:
                return renderComment();
            case INVERTED:
                return renderInverted(contexts);
            default:
                throw new Exception("Invalid Token Type");
        }
    }

    //token Literal Type token.
    //need to Clean useless left spaces and '\n'
    private String renderLiteral() {
        return textClean(this.value);
    }

    //token Inverted Type token
    private String renderInverted(final List<Map<String, Object>> contexts)
            throws Exception {
        Object value;
        if (this.value.contains(".")) {
            value = _lookup_(this.value, contexts);
        } else {
            value = Helper.lookup(this.value, contexts);
        }

        if (value == null) {
            return Constant.EMPTY_STRING;
        }

        if (value instanceof Boolean) {
            if ((Boolean) value) {
                return Constant.EMPTY_STRING;
            }
        }
        return this.renderChildren(contexts);

    }

    //token Variable Type token.
    // Throw NPE if value not found.
    private String renderVariable(final List<Map<String, Object>> contexts) {
        if (this.value.contains(".")) {
            return _escape(_lookup_(this.value, contexts)
                    .toString());
        }
        return _escape(Helper.lookup(this.value, contexts)
                .toString());
    }

    private String renderComment() {
        return Constant.EMPTY_STRING;
    }

    private String renderChildren(final List<Map<String, Object>> contexts)
            throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (Token child : this.children) {
            stringBuilder.append(child.render(contexts));
        }
        return stringBuilder.toString();
    }

    private String renderSection(List<Map<String, Object>> contexts)
            throws Exception {

        //lookup value by key's input type
        Object value;
        if (this.value.contains(".")) {
            value = _lookup_(this.value, contexts);
        } else {
            value = Helper.lookup(this.value, contexts);
        }

        //not found
        if (value == null) {
            return Constant.EMPTY_STRING;
        }

        //is false
        if (value instanceof Boolean) {
            if (!(Boolean) value) {
                return Constant.EMPTY_STRING;
            }
        }

        //isArray
        if (value instanceof List) {
            List<Map<String, Object>> tmp =
                    (List<Map<String, Object>>) value;

            if (tmp.size() <= 0) {
                return Constant.EMPTY_STRING;
            }

            StringBuilder stringbuilder = new StringBuilder();
            for (Map<String, Object> item : tmp) {
                if (item instanceof Map) {
                    contexts.add(item);
                }
                stringbuilder.append(this.renderChildren(contexts));
                contexts.remove(item);

            }
            return stringbuilder.toString();
        }

        //other type
        return _escape(this.renderChildren(contexts));
    }

    //dirty checking
    private String textClean(String text) {

        //early return "\n" and ""
        if (text.equals("\n")) {
            return Constant.EMPTY_STRING;
        }
        if (text.equals(Constant.EMPTY_STRING)) {
            return Constant.EMPTY_STRING;
        }


        int length = text.length();
        int begin = 0;
        //find the first index which is not equal to '\n'
        for (; begin < length; begin++) {
            if (text.charAt(begin) != '\n') {
                break;
            }
        }

        if (begin == length) {
            return text;
        }

        //find the first index which is not equal to space.
        int end = begin;
        for (;end < length; end++) {
            if (!Helper.isSpacesNotNewline(text.charAt(end))) {
                break;
            }
        }

        //cut the spaces
        if (end == length) {
            return text.substring(0, begin);
        }
        return text.substring(0, begin) + text.substring(end);
    }

    // return string by the escape attr.
    private String _escape(final String s) {
        if (this.escape) {
            return Helper.escape(s, true);
        }
        return s;
    }

    // return value. offer the key like "a.b.c"
    private Object _lookup_(final String dotName,
                            final List<Map<String, Object>> contexts) {

        if (dotName.equals(".")) {
            return contexts.get(contexts.size() - 1);
        }

        if (dotName.startsWith("./")) {
            return contexts
                    .get(contexts.size() - 1)
                    .get(dotName.substring(2));
        }

        if (dotName.startsWith(".")) {
            return contexts
                    .get(contexts.size() - 1)
                    .get(dotName.substring(1));
        }

        String[] names = dotName.split("\\.");
        Object value = Helper.lookup(names[0], contexts);
        for (int i = 1; i < names.length; i++) {
            String name = names[i];
            Map tmp = (Map) value;
            if (tmp != null) {
                value = tmp.get(name);
            } else {
                break;
            }

            if (value == null) {
                break;
            }
        }

        return value;
    }


}
