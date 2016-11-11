/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package tlsattacker.fuzzer.modification;

import java.util.logging.Logger;
import tlsattacker.fuzzer.certificate.ClientCertificateStructure;

/**
 * A modification which indicates that the client certificate in the TestVector
 * was changed
 * 
 * @author Robert Merget - robert.merget@rub.de
 */
public class ChangeClientCertificateModification extends Modification {

    /**
     * The client certificate to which was changed
     */
    private final ClientCertificateStructure keyCertPair;

    public ChangeClientCertificateModification(ClientCertificateStructure keyCertPair) {
        super(ModificationType.CHANGE_CLIENT_CERT);
        this.keyCertPair = keyCertPair;
    }

    public ClientCertificateStructure getKeyCertPair() {
        return keyCertPair;
    }

    private static final Logger LOG = Logger.getLogger(ChangeClientCertificateModification.class.getName());
}
