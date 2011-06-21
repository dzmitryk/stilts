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

package org.projectodd.stilts.stomp.protocol;

import org.jboss.netty.buffer.ChannelBuffers;
import org.projectodd.stilts.StompMessage;
import org.projectodd.stilts.stomp.protocol.StompFrame.Command;
import org.projectodd.stilts.stomp.protocol.StompFrame.Header;
import org.projectodd.stilts.stomp.spi.Headers;

public class StompFrames {
    
    public static StompFrame newAckFrame(Headers headers) {
        StompControlFrame frame = new StompControlFrame( Command.ACK );
        frame.setHeader( Header.MESSAGE_ID, headers.get( Header.MESSAGE_ID ) );
        frame.setHeader( Header.SUBSCRIPTION, headers.get( Header.SUBSCRIPTION ) );
        String transactionId = headers.get( Header.TRANSACTION );
        if ( transactionId != null ) {
            frame.setHeader( Header.TRANSACTION, transactionId );
        }
        return frame;
    }
    
    public static StompFrame newNackFrame(Headers headers) {
        StompControlFrame frame = new StompControlFrame( Command.NACK );
        frame.setHeader( Header.MESSAGE_ID, headers.get( Header.MESSAGE_ID ) );
        frame.setHeader( Header.SUBSCRIPTION, headers.get( Header.SUBSCRIPTION ) );
        String transactionId = headers.get( Header.TRANSACTION );
        if ( transactionId != null ) {
            frame.setHeader( Header.TRANSACTION, transactionId );
        }
        return frame;
    }
    
    
    public static StompFrame newSendFrame(StompMessage message) {
        StompContentFrame frame = new StompContentFrame( Command.SEND, message.getHeaders() );
        frame.setContent( ChannelBuffers.copiedBuffer( message.getContent() ) );
        return frame;
    }
    
    public static StompFrame newConnectedFrame(String sessionId) {
        StompControlFrame frame = new StompControlFrame( Command.CONNECTED );
        frame.setHeader( Header.SESSION, sessionId );
        return frame;
    }
    
    public static StompFrame newErrorFrame(String message, StompFrame inReplyTo) {
        StompContentFrame frame = new StompContentFrame( Command.ERROR );
        if ( inReplyTo != null ) {
            String receiptId = inReplyTo.getHeader( Header.RECEIPT );
            if ( receiptId != null ) {
                frame.setHeader( Header.RECEIPT_ID, receiptId );
            }
        }
        frame.setContent( ChannelBuffers.copiedBuffer( message.getBytes() ) );
        return frame;
    }

    public static StompFrame newReceiptFrame(String receiptId) {
        StompControlFrame receipt = new StompControlFrame( Command.RECEIPT );
        receipt.setHeader( Header.RECEIPT_ID, receiptId );
        return receipt;
    }

}
