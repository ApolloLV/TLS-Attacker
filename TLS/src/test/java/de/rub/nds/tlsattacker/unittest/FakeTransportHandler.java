/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.unittest;

import de.rub.nds.tlsattacker.transport.SimpleTransportHandler;
import de.rub.nds.tlsattacker.transport.TransportHandler;
import java.io.IOException;

/**
 * 
 * @author ic0ns
 */
public class FakeTransportHandler extends TransportHandler {
    /**
     * Data that will be returned on a fetchData() call
     */
    private byte[] fetchableByte;
    private byte[] sendByte;

    public FakeTransportHandler() {
    }

    public byte[] getSendByte() {
        return sendByte;
    }

    public byte[] getFetchableByte() {
        return fetchableByte;
    }

    public void setFetchableByte(byte[] fetchableByte) {
        this.fetchableByte = fetchableByte;
    }

    @Override
    public void closeConnection() {
    }

    @Override
    public byte[] fetchData() throws IOException {
        return fetchableByte;
    }

    @Override
    public void initialize(String address, int port) throws IOException {
    }

    @Override
    public void sendData(byte[] data) throws IOException {
        sendByte = data;
    }

}
