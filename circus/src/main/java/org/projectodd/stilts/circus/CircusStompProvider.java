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

package org.projectodd.stilts.circus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.TransactionManager;

import org.projectodd.stilts.StompException;
import org.projectodd.stilts.circus.xa.XAMessageConduitFactory;
import org.projectodd.stilts.helpers.OpenAuthenticator;
import org.projectodd.stilts.stomp.spi.AcknowledgeableMessageSink;
import org.projectodd.stilts.stomp.spi.Authenticator;
import org.projectodd.stilts.stomp.spi.Headers;
import org.projectodd.stilts.stomp.spi.StompConnection;
import org.projectodd.stilts.stomp.spi.StompProvider;

public class CircusStompProvider implements StompProvider {

    public CircusStompProvider(TransactionManager transactionManager, XAMessageConduitFactory messageConduitFactory) {
        this( transactionManager, messageConduitFactory, null );
    }

    public CircusStompProvider(TransactionManager transactionManager, XAMessageConduitFactory messageConduitFactory, Authenticator authenticator) {
        this.transactionManager = transactionManager;
        if (authenticator == null) {
            authenticator = OpenAuthenticator.INSTANCE;
        }
        this.authenticator = authenticator;
        this.messageConduitFactory = messageConduitFactory;
    }

    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public StompConnection createConnection(AcknowledgeableMessageSink messageSink, Headers headers) throws StompException {
        if (this.authenticator.authenticate( headers )) {
            try {
                CircusStompConnection connection = createStompConnection( messageSink, getNextSessionId(), headers );
                synchronized (this.connections) {
                    this.connections.add( connection );
                }
                return connection;
            } catch (Exception e) {
                throw new StompException( e );
            }
        }
        return null;
    }

    public void stop() throws Exception {
        HashSet<CircusStompConnection> disconnecting = new HashSet<CircusStompConnection>();
        synchronized (this.connections) {
            disconnecting.addAll( this.connections );
        }
        for ( CircusStompConnection each : disconnecting ) {
            each.disconnect();
        }
    }

    void unregister(CircusStompConnection circusStompConnection) {
        synchronized (this.connections) {
            this.connections.remove( circusStompConnection );
        }
    }

    protected String getNextSessionId() {
        return "session-" + sessionCounter.getAndIncrement();
    }

    protected CircusStompConnection createStompConnection(AcknowledgeableMessageSink messageSink, String sessionId, Headers headers) throws Exception {
        return new CircusStompConnection( this, this.messageConduitFactory.createXAMessageConduit( messageSink ), sessionId );
    }

    XAMessageConduitFactory getMessageConduitFactory() {
        return this.messageConduitFactory;
    }

    private XAMessageConduitFactory messageConduitFactory;
    private TransactionManager transactionManager;
    private Authenticator authenticator;
    private AtomicInteger sessionCounter = new AtomicInteger();
    private Set<CircusStompConnection> connections = new HashSet<CircusStompConnection>();

}
