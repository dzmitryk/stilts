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

package org.projectodd.stilts.stomp.protocol.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.projectodd.stilts.stomp.protocol.StompFrame;
import org.projectodd.stilts.stomp.protocol.StompFrame.Command;

public abstract class AbstractClientControlFrameHandler extends AbstractClientHandler {

    public AbstractClientControlFrameHandler(ClientContext clientContext, Command command) {
        super( clientContext );
        this.command = command;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext channelContext, MessageEvent e) throws Exception {
        log.trace(  "received: " + e.getMessage() );
        if ( e.getMessage() instanceof StompFrame ) {
            handleStompFrame( channelContext, (StompFrame) e.getMessage() );
        } 
        super.messageReceived( channelContext, e );
    }

    protected void handleStompFrame(ChannelHandlerContext channelContext, StompFrame frame) {
        if ( frame.getCommand().equals( this.command ) ) {
            handleControlFrame( channelContext, frame );
        }
    }
    
    protected abstract void handleControlFrame(ChannelHandlerContext channelContext, StompFrame frame);
    
    private Command command;


}
