/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.config.delegate;

import com.beust.jcommander.JCommander;
import de.rub.nds.tlsattacker.tls.workflow.TlsConfig;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class HostnameExtensionDelegateTest {

    private HostnameExtensionDelegate delegate;
    private JCommander jcommander;
    private String[] args;

    public HostnameExtensionDelegateTest() {
    }

    @Before
    public void setUp() {
        this.delegate = new HostnameExtensionDelegate();
        this.jcommander = new JCommander(delegate);
    }

    /**
     * Test of getSniHostname method, of class HostnameExtensionDelegate.
     */
    @Test
    public void testGetSniHostname() {
        args = new String[2];
        args[0] = "-server_name";
        args[1] = "its_me";
        assertFalse("its_me".equals(delegate.getSniHostname()));
        jcommander.parse(args);
        assertTrue("its_me".equals(delegate.getSniHostname()));
    }

    /**
     * Test of setSniHostname method, of class HostnameExtensionDelegate.
     */
    @Test
    public void testSetSniHostname() {
        assertFalse("123456".equals(delegate.getSniHostname()));
        delegate.setSniHostname("123456");
        assertTrue("123456".equals(delegate.getSniHostname()));
    }

    /**
     * Test of isServerNameFatal method, of class HostnameExtensionDelegate.
     */
    @Test
    public void testIsServerNameFatal() {
        args = new String[1];
        args[0] = "-servername_fatal";
        assertFalse(delegate.isServerNameFatal());
        jcommander.parse(args);
        assertTrue(delegate.isServerNameFatal());
    }

    /**
     * Test of setServerNameFatal method, of class HostnameExtensionDelegate.
     */
    @Test
    public void testSetServerNameFatal() {
        assertFalse(delegate.isServerNameFatal());
        delegate.setServerNameFatal(true);
        assertTrue(delegate.isServerNameFatal());
    }

    /**
     * Test of applyDelegate method, of class HostnameExtensionDelegate.
     */
    @Test
    public void testApplyDelegate() {
        args = new String[3];
        args[0] = "-server_name";
        args[1] = "its_me";
        args[2] = "-servername_fatal";
        jcommander.parse(args);
        TlsConfig config = new TlsConfig();
        config.setSniHostname(null);
        config.setSniHostnameFatal(false);
        delegate.applyDelegate(config);
        assertTrue(config.getSniHostname().equals("its_me"));
        assertTrue(config.isSniHostnameFatal());
    }

}
