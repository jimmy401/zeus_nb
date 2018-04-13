package com.taobao.zeus;

import com.taobao.zeus.util.DateUtil;

import java.text.ParseException;

/**
 * Created by lowry on 16/3/9.
 */
public class Test
{
    public static void main (String []args) throws ParseException {
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
