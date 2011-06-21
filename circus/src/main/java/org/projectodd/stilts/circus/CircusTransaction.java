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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.projectodd.stilts.StompException;
import org.projectodd.stilts.StompMessage;
import org.projectodd.stilts.stomp.protocol.StompFrame.Header;
import org.projectodd.stilts.stomp.spi.Acknowledger;
import org.projectodd.stilts.stomp.spi.StompTransaction;

public class CircusTransaction implements StompTransaction {

    public CircusTransaction(CircusStompConnection clientAgent, Transaction transaction, String id) {
        this.stompConnection = clientAgent;
        this.transaction = transaction;
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public CircusStompConnection getStompConnection() {
        return this.stompConnection;
    }

    public Transaction getJTATransaction() {
        return this.transaction;
    }

    @Override
    public void commit() throws StompException {
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            tm.commit();
        } catch (SecurityException e) {
            throw new StompException( e );
        } catch (IllegalStateException e) {
            throw new StompException( e );
        } catch (RollbackException e) {
            throw new StompException( e );
        } catch (HeuristicMixedException e) {
            throw new StompException( e );
        } catch (HeuristicRollbackException e) {
            throw new StompException( e );
        } catch (SystemException e) {
            throw new StompException( e );
        } catch (InvalidTransactionException e) {
            throw new StompException( e );
        }
    }

    @Override
    public void abort() throws StompException {
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            tm.rollback();
        } catch (IllegalStateException e) {
            throw new StompException( e );
        } catch (SystemException e) {
            throw new StompException( e );
        } catch (InvalidTransactionException e) {
            throw new StompException( e );
        }
    }

    @Override
    public void send(StompMessage message) throws StompException {
        XAResource xaResource = this.stompConnection.getMessageConduit().getXAResource();
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            this.transaction.enlistResource( xaResource );
            message.getHeaders().remove( Header.TRANSACTION );
            this.stompConnection.send( message );
            this.transaction.delistResource( xaResource, XAResource.TMSUSPEND );
            tm.suspend();
        } catch (StompException e) {
            throw e;
        } catch (Exception e) {
            throw new StompException( e );
        }
    }

    public void ack(Acknowledger acknowledger) throws StompException {
        XAResource xaResource = this.stompConnection.getMessageConduit().getXAResource();
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            this.transaction.enlistResource( xaResource );
            acknowledger.ack();
            this.transaction.delistResource( xaResource, XAResource.TMSUSPEND );
            tm.suspend();
        } catch (Exception e) {
            throw new StompException( e );
        }
    }

    public void nack(Acknowledger acknowledger) throws StompException {
        XAResource xaResource = this.stompConnection.getMessageConduit().getXAResource();
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            this.transaction.enlistResource( xaResource );
            acknowledger.nack();
            tm.suspend();
        } catch (Exception e) {
            throw new StompException( e );
        }
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + ": " + id + "]";
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private Transaction transaction;
    private CircusStompConnection stompConnection;
    private String id;
}
