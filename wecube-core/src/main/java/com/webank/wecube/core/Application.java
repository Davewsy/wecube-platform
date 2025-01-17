package com.webank.wecube.core;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.webank.wecube.core.config.SpringAppConfig;
import com.webank.wecube.core.config.SpringWebConfig;

@SpringBootApplication
@EnableProcessApplication
public class Application extends AbstractAnnotationConfigDispatcherServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { SpringAppConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { SpringWebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/" };
    }

}
