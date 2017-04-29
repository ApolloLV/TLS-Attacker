/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.config.delegate;

import com.beust.jcommander.Parameter;
import de.rub.nds.tlsattacker.core.workflow.TlsConfig;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class ClientAuthenticationDelegate extends Delegate {

    @Parameter(names = "-client_authentication", description = "YES or NO")
    private Boolean clientAuthentication;

    public ClientAuthenticationDelegate() {
    }

    public Boolean isClientAuthentication() {
        return clientAuthentication;
    }

    public void setClientAuthentication(boolean clientAuthentication) {
        this.clientAuthentication = clientAuthentication;
    }

    @Override
    public void applyDelegate(TlsConfig config) {
        if (clientAuthentication != null) {
            config.setClientAuthentication(clientAuthentication);
        }
    }

}