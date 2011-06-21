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

import org.projectodd.stilts.StompException;
import org.projectodd.stilts.StompMessage;
import org.projectodd.stilts.stomp.protocol.StompFrame.Header;
import org.projectodd.stilts.stomp.spi.AcknowledgeableMessageSink;
import org.projectodd.stilts.stomp.spi.Acknowledger;
import org.projectodd.stilts.stomp.spi.Subscription.AckMode;
import org.projectodd.stilts.stomplet.AcknowledgeableStomplet;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.Subscriber;

public class DefaultSubscriber implements Subscriber {

    public DefaultSubscriber(Stomplet stomplet, String subscriptionId, String destination, AcknowledgeableMessageSink messageSink, AckMode ackMode) {
        this.stomplet = stomplet;
        this.subscriptionId = subscriptionId;
        this.destination = destination;
        this.messageSink = messageSink;
        this.ackMode = ackMode;

        if (this.ackMode == AckMode.CLIENT) {
            this.ackSet = new CummulativeAckSet();
        } else if (this.ackMode == AckMode.CLIENT_INDIVIDUAL) {
            this.ackSet = new IndividualAckSet();
        }
    }

    @Override
    public String getId() {
        return this.subscriptionId;
    }

    public AckMode getAckMode() {
        return this.ackMode;
    }

    @Override
    public void send(StompMessage message) throws StompException {

        send( message, null );
    }

    @Override
    public void send(StompMessage message, Acknowledger acknowledger) throws StompException {
        StompMessage dupe = message.duplicate();
        dupe.getHeaders().put( Header.SUBSCRIPTION, this.subscriptionId );
        
        if ((acknowledger == null) && (this.stomplet instanceof AcknowledgeableStomplet)) {
            acknowledger = new StompletAcknowledger( (AcknowledgeableStomplet) this.stomplet, this, dupe );
        }

        if (this.ackMode == AckMode.AUTO && acknowledger != null) {
            try {
                acknowledger.ack();
            } catch (Exception e) {
                throw new StompException( e );
            }
            this.messageSink.send( dupe );
        } else {
            this.messageSink.send( dupe, acknowledger );
        }
    }

    @Override
    public String getDestination() {
        return this.destination;
    }

    private Stomplet stomplet;
    private AcknowledgeableMessageSink messageSink;
    private String subscriptionId;
    private String destination;
    private AckMode ackMode;
    private AckSet ackSet;

}
