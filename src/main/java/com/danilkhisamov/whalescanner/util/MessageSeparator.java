package com.danilkhisamov.whalescanner.util;

import com.danilkhisamov.whalescanner.model.bsc.BscToken;

import java.util.ArrayList;
import java.util.List;

public class MessageSeparator {
    public static <R extends Object> List<String> separateMessages(List<R> objects) {
        List<String> messages = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        int tmpLength = 0;
        for (Object object : objects) {
            String str = object.toString() + "\n\n";
            int strLength = str.length();
            if (tmpLength + strLength >= Constants.TELEGRAM_MESSAGE_MAX_LENGTH) {
                messages.add(tmp.toString());
                tmp = new StringBuilder(str);
                tmpLength = strLength;
            } else {
                tmp.append(str);
                tmpLength += strLength;
            }
        }
        if (tmp.length() > 0) {
            messages.add(tmp.toString());
        }
        return messages;
    }
}
