package com.github.pengpan.service;

import com.github.pengpan.App;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author pengpan
 */
public class CoreServiceTest {

    private CoreService coreService;

    @Before
    public void before() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);
        coreService = context.getBean(CoreService.class);
    }

}