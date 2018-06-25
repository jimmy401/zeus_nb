package com.taobao.zeus.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentUtil {
    //[Dd][Rr][Oo][Pp]\s+[Tt][Aa][Bb][Ll][Ee]\s+([Dd][Ww][Dd]_|[Dd][Ww][Ss]_|[Aa][Dd][Mm]_)
    public static boolean containInvalidContent(String content) {
        String regexString="[Dd][Rr][Oo][Pp]\\s+[Tt][Aa][Bb][Ll][Ee]\\s+([Dd][Ww][Dd]_|[Dd][Ww][Ss]_|[Aa][Dd][Mm]_)";
        boolean ret = false;
        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(content);
        while (m.find()) {
            ret = true;
            break;
        }
        return ret;
    }

    public static int containRmCnt(String content) {
        String regexString="rm\\s+-rf";
        int cnt = 0;
        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(content);
        while (m.find()) {
            cnt++;
        }
        return cnt;
    }

    public static int contentValidRmCnt(String content,String validString) {
        int cnt=0;
        String[] parts = validString.split(",");
        for (String item:parts) {
            Pattern p = Pattern.compile(item);
            Matcher m = p.matcher(content);
            while (m.find()) {
                cnt++;
            }
        }
        return cnt;
    }
}
