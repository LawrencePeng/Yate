package com.llpeng.yate.yate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.llpeng.yate.token.RootToken;
import com.llpeng.yate.util.Constant;
import com.llpeng.yate.util.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by lawrence on 16/4/28.
 * Renderer is the entry point of the token process.
 */
public class Renderer {

    //render the template by the json input.
    public static String render(final String template,
                  final String contexts,
                  final String leftDelimiter,
                  final String rightDelimiter)
            throws Exception {


        Object parsed = JSON.parse(contexts);
        List<Map<String, Object>> list;
        if (parsed instanceof JSONObject) {
            list = new ArrayList<>();
            list.add((Map<String, Object>) parsed);
        } else {
            list = (List<Map<String, Object>>) parsed;
        }
        return render(template,
                list,
                leftDelimiter,
                rightDelimiter);
    }

    public static String render(final String template,
                                final Object contexts,
                                final String leftDelimiter,
                                final String rightDelimiter)
            throws Exception {
        Object jsonObject = JSON.toJSON(contexts);
        List<Map<String, Object>> list;
        if (jsonObject instanceof JSONObject) {
            list = new ArrayList<>();
            list.add((Map<String, Object>) jsonObject);
        } else {
            list = (List<Map<String, Object>>) jsonObject;
        }
        return render(template,
                list,
                leftDelimiter,
                rightDelimiter);
    }

    public static String render(final String template,
                                final Object contexts)
            throws Exception {
        return render(template,
                contexts,
                Constant.LEFT_DEFAULT_DELIMITERS,
                Constant.RIGHT_DEFAULT_DELIMITERS);
    }

    public static String render(final String template,
                                final String contexts)
            throws Exception {
        return render(template,
                contexts,
                Constant.LEFT_DEFAULT_DELIMITERS,
                Constant.RIGHT_DEFAULT_DELIMITERS);
    }

    public static String render(final String template,
                                final List<Map<String, Object>> jsonObjects,
                                final String leftDelimiter,
                                final String rightDelimiter)
            throws Exception {
        RootToken root =
                Helper.parse(template,
                        leftDelimiter,
                        rightDelimiter);

        return root.render(jsonObjects);
    }

    //if the input's already a list, the overhead of transforming a string to json can be eliminated.
    public static String render(final String template,
                                final List<Map<String, Object>> jsonObjects)
            throws Exception {
        return render(template,
                jsonObjects,
                Constant.LEFT_DEFAULT_DELIMITERS,
                Constant.RIGHT_DEFAULT_DELIMITERS);
    }


    private Renderer() { }
}
