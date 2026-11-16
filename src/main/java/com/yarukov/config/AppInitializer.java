package com.yarukov.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{HibernateConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // Веб-контекст DispatcherServlet (Spring MVC, контроллеры, WebSockets)
        return new Class<?>[]{WebConfig.class, WebSocketConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}