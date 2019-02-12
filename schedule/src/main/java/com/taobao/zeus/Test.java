package com.taobao.zeus;

import com.taobao.zeus.util.DateUtil;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lowry on 16/3/9.
 */
public class Test
{
    public static void main (String []args) throws ParseException {

test2();
    }

    private static void test2(){
        String content ="asfdas DROP table \n" +
                "wd_video asdfa drop \n" +
                "table  dws_video";
        String test = "201808281800000099";
        System.out.println(content);
        System.out.println("------------------");
        Pattern p=Pattern.compile("\r|\n");
        Matcher m=p.matcher(content);
        String strNoBlank = m.replaceAll("\t");
        System.out.println(strNoBlank);
        while(m.find()){
            System.out.println(m.group());
        }
    }

    private void test1() throws ParseException{
        String content ="asfdas DROP table dwd_video asdfa drop   table  dws_video";
        String test = "201808281800000099";
        System.out.println(test.substring(0,11));

        Pattern p=Pattern.compile("[Dd][Rr][Oo][Pp]\\s+[Tt][Aa][Bb][Ll][Ee]\\s+([Dd][Ww][Dd]_|[Dd][Ww][Ss]_|[Aa][Dd][Mm]_)");
        Matcher m=p.matcher(content);
        while(m.find()){
            System.out.println(m.group());
        }

        String tz="GMT+0800";
        long ts =  DateUtil.string2Timestamp(
                DateUtil.getDayEndTime(0, tz), null);

        System.out.println(ts);

        String dt =  DateUtil.getTimeStrByTimestamp(ts,
                DateUtil.getDefaultTZStr());

        System.out.println(dt);


        System.out.println("day start time :" +DateUtil.getDayStartTime(0, tz));

        System.out.println("day end time :" +DateUtil.getDayEndTime(0, tz));
    }
}
