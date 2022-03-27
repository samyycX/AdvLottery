package com.samyyc.lottery.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class TextUtil {

    public static String convertColor(String text) {
        if (text != null ) {
            return text.replaceAll("&", "§");
        } else {
            return null;
        }

    }

    public static List<String> convertColor(List<String> textList) {
        List<String> convertedTextList = new LinkedList<>();
        if ( textList != null ) {
            for (String text : textList) {
                convertedTextList.add(text.replaceAll("&","§"));
            }
            return convertedTextList;
        } else {
            return null;
        }
    }

}
