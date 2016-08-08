package com.huboyi.other;

import com.huboyi.other.dzbt.Dzbt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by admin on 2016/8/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/data/spring-data-load.xml", "/config/other/dzbt/dzbt-spring.xml"})
public class TestDzbt {

    @Autowired
    @Qualifier("dzbt")
    private Dzbt dzbt;

    @Test
    public void testExecute() throws InterruptedException {
        dzbt.execute();
        Thread.currentThread().join();
    }
}
