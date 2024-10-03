package com.neu.edu.cloudapplication.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnsupportedMethodController {

    @RequestMapping(value = "/**", method = {RequestMethod.OPTIONS, RequestMethod.HEAD})
    public void handleUnsupportedMethods(){
        throw new UnsupportedOperationException("HTTP method not supported");
    }
}
