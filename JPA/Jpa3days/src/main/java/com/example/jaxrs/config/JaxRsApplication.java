package com.example.jaxrs.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rs")
public class JaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(com.example.jaxrs.day1.CustomerResource.class);
        classes.add(com.example.jaxrs.day1.OrderResource.class);
        classes.add(com.example.jaxrs.day1.ProductResource.class);
        classes.add(com.example.jaxrs.day2.ItemResource.class);
        classes.add(com.example.jaxrs.day2.SearchResource.class);
        classes.add(com.example.jaxrs.day2.CategoryResource.class);
        classes.add(com.example.jaxrs.day3.BookingResource.class);
        classes.add(com.example.jaxrs.day3.ConfigResource.class);
        classes.add(com.example.jaxrs.day3.EventResource.class);
        classes.add(JacksonConfigurator.class);
        return classes;
    }
}
