package com.taobao.zeus.web;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Zhouxw on 2016/4/11.
 */

@RunWith(MockitoJUnitRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/applicationContext.xml"})

public abstract class SpringTest extends AbstractJUnit4SpringContextTests {

//    public <T> T getBean(Class<T> type) {
//        return applicationContext.getBean(type);
//    }

    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    protected ApplicationContext getContext() {
        return applicationContext;
    }


}