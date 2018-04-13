package com.taobao.zeus.web;

import com.taobao.zeus.util.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by lowry on 17/1/19.
 */

//public class Test {

public class Test extends  SpringTest {
    @Autowired
    Tbean t1;

    @Autowired
    Environment environment;
    @org.junit.Test
    public void t()
    {

        System.out.print(t1.getF1());
        System.out.print(Environment.getHost());
//        applicationContext.getBean("dataSource");
    }

//    @org.junit.Test
    public void t2()
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");


        context.start();
    }
}
