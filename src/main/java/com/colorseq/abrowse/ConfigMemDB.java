package com.colorseq.abrowse;

import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// 此类暂时无作用，留此备用
@WebListener
@Component
public class ConfigMemDB implements ServletContextListener {

    public ConfigMemDB() {
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

        System.out.println("ServletContex 销毁");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {

    }
}
