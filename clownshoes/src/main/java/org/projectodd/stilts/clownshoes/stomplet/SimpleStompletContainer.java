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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectodd.stilts.StompException;
import org.projectodd.stilts.StompMessage;
import org.projectodd.stilts.stomp.spi.Headers;
import org.projectodd.stilts.stomplet.MessageRouter;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.StompletConfig;

public class SimpleStompletContainer implements StompletContainer, MessageRouter {

    public SimpleStompletContainer() {
    }
    
    public void start() throws Exception {
        this.stompletContext = new DefaultStompletContext( this );
    }
    
    public void stop() throws Exception {
        while ( ! this.routes.isEmpty() ) {
            Route route = this.routes.remove( 0 );
            destroy( route.getStomplet() );
        }
    }
    
    protected void destroy(Stomplet stomplet) throws StompException {
        stomplet.destroy();
    }
    
    public void addStomplet(String destinationPattern, Stomplet stomplet) throws StompException {
        addStomplet( destinationPattern, stomplet, new HashMap<String,String>() );
    }
    
    public void addStomplet(String destinationPattern, Stomplet stomplet, Map<String,String> properties) throws StompException {
        StompletConfig config = new DefaultStompletConfig( this.stompletContext, properties );
        stomplet.initialize( config );
        Route route = new Route( destinationPattern, stomplet );
        this.routes.add( route );
    }

    @Override
    public void send(StompMessage message) throws StompException {
        RouteMatch match = match( message.getDestination() );

        if (match != null) {
            Stomplet stomplet = match.getRoute().getStomplet();
            Map<String, String> matches = match.getMatches();
            Headers headers = message.getHeaders();
            for (String name : matches.keySet()) {
                headers.put( "stomplet." + name, matches.get( name ) );
            }
            stomplet.onMessage( message );
        }
    }

    public RouteMatch match(String destination) {
        RouteMatch match = null;
        for (Route route : this.routes) {
            match = route.match( destination );
            if (match != null) {
                break;
            }
        }

        return match;
    }


    private DefaultStompletContext stompletContext;
    private final List<Route> routes = new ArrayList<Route>();

}
