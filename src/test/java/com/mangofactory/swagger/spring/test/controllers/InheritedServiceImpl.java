package com.mangofactory.swagger.spring.test.controllers;

import org.springframework.stereotype.Component;



@Component
public class InheritedServiceImpl implements InheritedService {

    @Override
    public String getSomething(String parameter) {
        return parameter;
    }

    
}