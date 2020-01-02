package com.mutil.sso;

import com.SSOApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes= SSOApplication.class)
@AutoConfigureMockMvc
public class RedirectUrlTest {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectUrlTest.class);

    @Test
    public void getRedirectUrlTest() {
        LOG.info("开始测试..");
    }
}
