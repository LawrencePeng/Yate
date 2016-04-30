package com.llpeng.yate.util;

import com.llpeng.yate.render.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lawrence on 16/4/27.
 */
public class Helper {
    private Helper() { }

    //find value by key, if it's not found, return null.
    public static Object
        lookup(final String name,
               final List<Map<String, Object>> contexts) {
        for (int i = contexts.size() - 1; i >= 0; i--) {
            if (contexts.get(i).containsKey(name)) {
                return contexts.get(i).get(name);
            }
        }
        return null;
    }

    //if the mustache 's in a StandAlone
    private static List<Integer>
        isStandAlone(final String text,
                     int start,
                     int end) {
        final int length = text.length();
        boolean left = false;
        boolean right = false;
        start--;
        while ((start >= 0) && isSpacesNotNewline(text.charAt(start))) {
            start--;
        }
        if (start < 0 || text.charAt(start) == '\n') {
            left = true;
        }

        while ((end < length) && isSpacesNotNewline(text.charAt(end))) {
            end++;
        }
        if (end == length || text.charAt(end) == '\n') {
            right = true;
        }

        if (left && right) {
            return Arrays.asList(start + 1, end);
        }

        return null;
    }

    //do the transforming process of a input html.
    public static String escape(final String s, final boolean quote) {
        String ret = s
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        if (quote) {
            return ret.replaceAll("\"", "&quot;");
        }
        return ret;
    }

    //lexing
    private static Pattern delimitersToRe(final String leftDelimiter,
                                          final String rightDelimiter) {
        String openTag = "";
        String closeTag = "";

        for (int i = 0; i < leftDelimiter.length(); i++) {
            char ch = leftDelimiter.charAt(i);
            if (isNumOrAlphabet(ch)) {
                openTag += ch;
            } else {
                   openTag += "\\" + ch;
            }
        }

        for (int i = 0; i < rightDelimiter.length(); i++) {
            char ch = rightDelimiter.charAt(i);
            if (isNumOrAlphabet(ch)) {
                closeTag += ch;
            } else {
                closeTag += "\\" + ch;
            }
        }

        return Pattern.compile(
                openTag
                        + "([#^>&{/!=]?)\\s*(.*?)\\s*([}=]?)"
                        + closeTag,
                Pattern.DOTALL);
    }

    //parse the template  to AST
    public static RootToken parse(final String template,
                                  final String leftDelimiter,
                                  final String rightDelimiter)
            throws Exception {
        int index = 0;
        final int length = template.length();
        boolean stripSpace = false;

        //the list save the stack statement.
        List<Token> tokens = new ArrayList<>();
        Stack<HashMap<String, Integer>> sections = new Stack<>();
        Stack<List<Token>> tokensStack = new Stack<>();

        //precompile pattern
        Pattern pattern =
                delimitersToRe(leftDelimiter, rightDelimiter);

        final Matcher matcher =
                pattern.matcher(template);

        //do the regex matching.
        while (matcher.find(index)) {
            Token token = null;
            LiteralToken lastLiteral = null;

            //eliminate redundant ""s
            if (matcher.start() > index) {
                //literal type token created.
                lastLiteral =
                        new LiteralToken(template.substring(index,
                                matcher.start()));
                //add to the tokens
                tokens.add(lastLiteral);
            }

            final String prefix = matcher.group(1);
            final String name = matcher.group(2);
            final String postfix = matcher.group(3);

            if (prefix.equals("{") && postfix.equals("}")) {
                token = new VariableToken(name, name, false);
            } else if (prefix.equals("") && postfix.equals("")) {
                token = new VariableToken(name, name, true);
            }  else if (prefix.equals("&")) {
                token = new VariableToken(name, name, true);
            }  else if (prefix.equals("!")) {
                token = new CommentToken();
            } else if (prefix.equals("^")) { // the inverted token matched
                token = new InvertedToken(name, name);
                tokens.add(token);

                // save the tokens onto stack.
                token = null;
                tokensStack.push(tokens);
                tokens = new ArrayList<>();

                sections.push(new HashMap<String, Integer>() {{
                    put(name, matcher.end());
                }});

                stripSpace = true;
            } else if (prefix.equals("#")) {
                token = new SectionToken(name, name);
                tokens.add(token);

                // save the tokens onto stack.
                token = null;
                tokensStack.push(tokens);
                tokens = new ArrayList<>();

                sections.push(new HashMap<String, Integer>() {{
                    put(name, matcher.end());
                }});

                stripSpace = true;

            } else if (prefix.equals("/")) {//pop stack.
                Map<String, Integer> pop = sections.pop();
                Iterator<Map.Entry<String, Integer>> iterator =
                        pop.entrySet().iterator();
                String tagName = null;
                int textEnd = 0;

                if (iterator.hasNext()) {
                    Map.Entry<String, Integer> next = iterator.next();
                    tagName = next.getKey();
                    textEnd = next.getValue();
                }

                if (tagName != null && !tagName.equals(name)) {
                    throw new Exception("unclosed tag: '\" "
                            + "+ name "
                            + "+ \"' Got:\""
                            + matcher.group());
                }

                //building the tree.
                List<Token> children = tokens;
                tokens = tokensStack.pop();

                Token section = tokens.get(tokens.size() - 1);
                section.setText(template.substring(textEnd, matcher.start()));
                section.setChildren(children);

            } else {
                throw new Exception("parse err, the matcher info is"
                        + matcher.toString());
            }

            if (token != null) {
                tokens.add(token);
            }

            //for the next match.
            index = matcher.end();

            //cut spaces which are the right part of a single line if stripSpace is true.
            if (stripSpace) {
                List<Integer> pos =
                        isStandAlone(template,
                                matcher.start(),
                                matcher.end());

                if (pos != null) {
                    index = pos.get(1);

                    if (lastLiteral != null) {
                        lastLiteral.setValue(
                                rStrip(lastLiteral.getValue())
                        );
                    }
                }
            }
        }

        if (index != length) {
            tokens.add(new LiteralToken(template.substring(index)));
        }
        return new RootToken("root", tokens);
    }

    private static String rStrip(final String s) {

        for (int i = s.length() - 1; i >= 0; i--) {
            if (!isSpacesNotNewline(s.charAt(i))) {
                return s.substring(0, i);
            }
        }
        return "";
    }

    private static boolean isNumOrAlphabet(final char ch) {
        return (ch >= '0' && ch <= '9')
                || (ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z');
    }

    public static boolean isSpacesNotNewline(final char ch) {
        boolean ret = false;
        for (char space : Constant.SPACE_NOT_NEWLINE) {
            if (ch == space) {
                ret = true;
            }
        }
        return ret;
    }

}
