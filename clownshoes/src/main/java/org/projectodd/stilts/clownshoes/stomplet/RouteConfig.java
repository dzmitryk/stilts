/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License 2.0. 
 * 
 * You should have received a copy of the Apache License 
 * along with this software; if not, please see:
 * http://apache.org/licenses/LICENSE-2.0.txt
 */

package org.projectodd.stilts.clownshoes.stomplet;

import java.util.Map;

public class RouteConfig {
    
    private String pattern;
    private String className;
    private Map<String, String> properties;

    public RouteConfig() {
        
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public String getPattern() {
        return this.pattern;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public void setProperties(Map<String,String> properties) {
        this.properties = properties;
    }
    
    public Map<String,String> getProperties() {
        return this.properties;
    }

}
