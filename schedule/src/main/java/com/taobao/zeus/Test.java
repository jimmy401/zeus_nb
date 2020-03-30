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
        String s = "Mem:         128717        7819      109799          97       11098      120077";
        Pattern pattern=Pattern.compile("\\d+");
        String line=s.substring(s.indexOf("Mem:"));
        Matcher matcher=pattern.matcher(line);
        double used=0d;
        double free=0d;
        int num=0;
        while(matcher.find()){
            if(num==0){
                num++;
                continue;
            }else
            if(num==1){
                used=Double.valueOf(matcher.group());
                num++;
                continue;
            }
            if(num==2){
                free=Double.valueOf(matcher.group());
                break;
            }
        }

        System.out.println(used);

        System.out.println(used/(used+free));

    }
}
