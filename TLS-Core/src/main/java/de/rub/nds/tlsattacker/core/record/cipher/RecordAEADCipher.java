/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.record.cipher;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.constants.RecordByteLength;
import de.rub.nds.tlsattacker.core.constants.Tls13KeySetType;
import de.rub.nds.tlsattacker.core.crypto.cipher.CipherWrapper;
import de.rub.nds.tlsattacker.core.exceptions.CryptoException;
import de.rub.nds.tlsattacker.core.record.cipher.cryptohelper.DecryptionRequest;
import de.rub.nds.tlsattacker.core.record.cipher.cryptohelper.DecryptionResult;
import de.rub.nds.tlsattacker.core.record.cipher.cryptohelper.EncryptionRequest;
import de.rub.nds.tlsattacker.core.record.cipher.cryptohelper.EncryptionResult;
import de.rub.nds.tlsattacker.core.record.cipher.cryptohelper.KeySet;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Robin: ADAPT CLASS FOR CHACHA!
public class RecordAEADCipher extends RecordCipher {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * sequence Number length in byte
     */
    public static final int SEQUENCE_NUMBER_LENGTH = 8;
    /**
     * tag lengths in byte
     */
    public static final int GCM_TAG_LENGTH = 16;
    public static final int CHACHAPOLY_TAG_LENGTH = 16;
    /**
     * iv lengths in byte
     */
    public static final int GCM_IV_LENGTH = 12;
    public static final int CHACHAPOLY_IV_LENGTH = 12;

    public RecordAEADCipher(TlsContext context, KeySet keySet) {
        super(context, keySet);
        ConnectionEndType localConEndType = context.getConnection().getLocalConnectionEndType();
        encryptCipher = CipherWrapper.getEncryptionCipher(cipherSuite, localConEndType, getKeySet());
        decryptCipher = CipherWrapper.getDecryptionCipher(cipherSuite, localConEndType, getKeySet());
    }

    @Override
    public EncryptionResult encrypt(EncryptionRequest request) {
        try {
            if (version.isTLS13() || context.getActiveKeySetTypeWrite() == Tls13KeySetType.EARLY_TRAFFIC_SECRETS) {
                return encryptTLS13(request);
            } else {
                return encryptTLS12(request);
            }
        } catch (CryptoException E) {
            LOGGER.warn("Could not encrypt Data with the provided parameters. Returning unencrypted data.");
            LOGGER.debug(E);
            return new EncryptionResult(request.getPlainText());
        }
    }

    @Override
    public DecryptionResult decrypt(DecryptionRequest decryptionRequest) {
        try {
            byte[] decrypted;
            if (version.isTLS13() || context.getActiveKeySetTypeRead() == Tls13KeySetType.EARLY_TRAFFIC_SECRETS) {
                decrypted = decryptTLS13(decryptionRequest);
            } else {
                decrypted = decryptTLS12(decryptionRequest);
            }
            return new DecryptionResult(null, decrypted, null);
        } catch (CryptoException E) {
            LOGGER.warn("Could not decrypt Data with the provided parameters. Returning undecrypted data.");
            LOGGER.debug(E);
            return new DecryptionResult(null, decryptionRequest.getCipherText(), false);
        }
    }

    // TODO Robin: TLS1.3 Adapt for chacha???
    private EncryptionResult encryptTLS13(EncryptionRequest request) throws CryptoException {
        byte[] sequenceNumberByte = ArrayConverter.longToBytes(context.getWriteSequenceNumber(),
                RecordByteLength.SEQUENCE_NUMBER);
        byte[] nonce = ArrayConverter.concatenate(new byte[GCM_IV_LENGTH - RecordByteLength.SEQUENCE_NUMBER],
                sequenceNumberByte);
        byte[] encryptIV = prepareAeadParameters(nonce, getEncryptionIV());
        LOGGER.debug("Encrypting GCM with the following IV: {}", ArrayConverter.bytesToHexString(encryptIV));
        byte[] cipherText;
        if (version == ProtocolVersion.TLS13 || version == ProtocolVersion.TLS13_DRAFT25
                || version == ProtocolVersion.TLS13_DRAFT26 || version == ProtocolVersion.TLS13_DRAFT27
                || version == ProtocolVersion.TLS13_DRAFT28) {
            cipherText = encryptCipher.encrypt(encryptIV, GCM_TAG_LENGTH * 8, request.getAdditionalAuthenticatedData(),
                    request.getPlainText());
        } else {
            cipherText = encryptCipher.encrypt(encryptIV, GCM_TAG_LENGTH * 8, request.getPlainText());
        }
        return new EncryptionResult(encryptIV, cipherText, false);
    }

    // TODO Robin: TLS1.3 Adapt for chacha???
    private byte[] prepareAeadParameters(byte[] nonce, byte[] iv) {
        LOGGER.info("PREPARING AEAD PARAMs");
        byte[] param = new byte[GCM_IV_LENGTH];
        for (int i = 0; i < GCM_IV_LENGTH; i++) {
            param[i] = (byte) (iv[i] ^ nonce[i]);
        }
        return param;
    }

    /**
     * Different handling for "GCM-Mode" AEAD Ciphers and ChaCha20Poly1305
     */
    private EncryptionResult encryptTLS12(EncryptionRequest request) throws CryptoException {
        // ChaCha20Poly1305 is used as AEAD Cipher
        if (cipherSuite.usesCHACHA20POLY1305()) {
            LOGGER.warn("Write Seq.no:" + Long.toString(context.getWriteSequenceNumber()));
            LOGGER.warn("Read Seq.no:" + Long.toString(context.getReadSequenceNumber()));
            encryptCipher.setNonce(context.getWriteSequenceNumber());
            LOGGER.warn("Using write-seqno");
            LOGGER.info("Encrypting ChaCha20Poly1305 with the following AAD: {}",
                    ArrayConverter.bytesToHexString(request.getAdditionalAuthenticatedData()));

            LOGGER.info("encrypting...");
            byte[] iv = getKeySet().getWriteIv(context.getConnection().getLocalConnectionEndType());
            byte[] ciphertext = encryptCipher.encrypt(iv, CHACHAPOLY_TAG_LENGTH,
                    request.getAdditionalAuthenticatedData(), request.getPlainText());

            LOGGER.info("returning EncryptionResult...");
            return new EncryptionResult(ciphertext);
        }
        // else: Cipher runs in GCM-mode:
        byte[] nonce = ArrayConverter.longToBytes(context.getWriteSequenceNumber(), RecordByteLength.SEQUENCE_NUMBER);
        byte[] iv = ArrayConverter.concatenate(
                getKeySet().getWriteIv(context.getConnection().getLocalConnectionEndType()), nonce);
        LOGGER.info("Encrypting GCM with the following IV: {}", ArrayConverter.bytesToHexString(iv));
        LOGGER.info("Encrypting GCM with the following AAD: {}",
                ArrayConverter.bytesToHexString(request.getAdditionalAuthenticatedData()));
        byte[] ciphertext = encryptCipher.encrypt(iv, GCM_TAG_LENGTH * 8, request.getAdditionalAuthenticatedData(),
                request.getPlainText());
        return new EncryptionResult(iv, ciphertext, true);
    }

    // TODO Robin: TLS1.3 Adapt for chacha???
    private byte[] decryptTLS13(DecryptionRequest decryptionRequest) throws CryptoException {
        LOGGER.debug("Decrypting using SQN:" + context.getReadSequenceNumber());
        byte[] sequenceNumberByte = ArrayConverter.longToBytes(context.getReadSequenceNumber(),
                RecordByteLength.SEQUENCE_NUMBER);
        byte[] nonce = ArrayConverter.concatenate(new byte[GCM_IV_LENGTH - RecordByteLength.SEQUENCE_NUMBER],
                sequenceNumberByte);
        byte[] decryptIV = prepareAeadParameters(nonce, getDecryptionIV());
        LOGGER.debug("Decrypting GCM with the following IV: {}", ArrayConverter.bytesToHexString(decryptIV));
        LOGGER.debug("Decrypting the following GCM ciphertext: {}",
                ArrayConverter.bytesToHexString(decryptionRequest.getCipherText()));
        if (version == ProtocolVersion.TLS13 || version == ProtocolVersion.TLS13_DRAFT25
                || version == ProtocolVersion.TLS13_DRAFT26 || version == ProtocolVersion.TLS13_DRAFT27
                || version == ProtocolVersion.TLS13_DRAFT28) {
            return decryptCipher.decrypt(decryptIV, GCM_TAG_LENGTH * 8,
                    decryptionRequest.getAdditionalAuthenticatedData(), decryptionRequest.getCipherText());
        } else {
            return decryptCipher.decrypt(decryptIV, GCM_TAG_LENGTH * 8, decryptionRequest.getCipherText());
        }
    }

    // TODO Robin: Adapt for chacha
    /**
     * Different handling for "GCM-Mode" AEAD Ciphers and ChaCha20Poly1305
     */
    private byte[] decryptTLS12(DecryptionRequest decryptionRequest) throws CryptoException {

        if (decryptionRequest.getCipherText().length < SEQUENCE_NUMBER_LENGTH) {
            LOGGER.warn("Could not DecryptCipherText. Too short. Returning undecrypted Ciphertext");
            return decryptionRequest.getCipherText();
        }
        // ChaCha20Poly1305 is used as AEAD Cipher
        if (cipherSuite.usesCHACHA20POLY1305()) {
            if (cipherSuite.usesCHACHA20POLY1305()) {
                LOGGER.warn("Write Seq.no:" + Long.toString(context.getWriteSequenceNumber()));
                LOGGER.warn("Read Seq.no:" + Long.toString(context.getReadSequenceNumber()));
                LOGGER.warn("Using read-seqno");
                decryptCipher.setNonce(context.getReadSequenceNumber());

                LOGGER.info("decrypting...");
                byte[] iv = getKeySet().getReadIv(context.getConnection().getLocalConnectionEndType());
                LOGGER.warn("Decrypting with the following AAD: {}",
                        ArrayConverter.bytesToHexString(decryptionRequest.getAdditionalAuthenticatedData()));
                byte[] plaintext = decryptCipher.decrypt(iv, CHACHAPOLY_TAG_LENGTH,
                        decryptionRequest.getAdditionalAuthenticatedData(), decryptionRequest.getCipherText());

                LOGGER.info("returning EncryptionResult...");
                return plaintext;
            }
        } else {
            // Cipher runs in GCM-mode:
            byte[] nonce = Arrays.copyOf(decryptionRequest.getCipherText(), SEQUENCE_NUMBER_LENGTH);
            byte[] data = Arrays.copyOfRange(decryptionRequest.getCipherText(), SEQUENCE_NUMBER_LENGTH,
                    decryptionRequest.getCipherText().length);
            byte[] iv = ArrayConverter.concatenate(
                    getKeySet().getReadIv(context.getConnection().getLocalConnectionEndType()), nonce);
            LOGGER.info("Decrypting GCM with the following IV: {}", ArrayConverter.bytesToHexString(iv));
            LOGGER.info("Decrypting GCM with the following AAD: {}",
                    ArrayConverter.bytesToHexString(decryptionRequest.getAdditionalAuthenticatedData()));
            LOGGER.info("Decrypting the following GCM ciphertext: {}", ArrayConverter.bytesToHexString(data));
            return decryptCipher.decrypt(iv, GCM_TAG_LENGTH * 8, decryptionRequest.getAdditionalAuthenticatedData(),
                    data);
        }
        return new byte[1];
    }

    @Override
    public boolean isUsingPadding() {
        return version.isTLS13() || context.getActiveKeySetTypeWrite() == Tls13KeySetType.EARLY_TRAFFIC_SECRETS
                || context.getActiveKeySetTypeRead() == Tls13KeySetType.EARLY_TRAFFIC_SECRETS;
    }

    @Override
    public boolean isUsingMac() {
        return false;
    }

    @Override
    public boolean isUsingTags() {
        return true;
    }

    @Override
    public int getTagSize() {
        if (cipherSuite.usesCHACHA20POLY1305()) {
            return CHACHAPOLY_TAG_LENGTH;
        } else {
            return SEQUENCE_NUMBER_LENGTH + GCM_TAG_LENGTH;
        }
    }

    @Override
    public byte[] getEncryptionIV() {
        byte[] nonce = ArrayConverter.longToBytes(context.getWriteSequenceNumber(), SEQUENCE_NUMBER_LENGTH);
        return ArrayConverter.concatenate(getKeySet().getWriteIv(context.getConnection().getLocalConnectionEndType()),
                nonce);
    }

    @Override
    public byte[] getDecryptionIV() {
        byte[] nonce = ArrayConverter.longToBytes(context.getReadSequenceNumber(), SEQUENCE_NUMBER_LENGTH);
        return ArrayConverter.concatenate(getKeySet().getReadIv(context.getConnection().getLocalConnectionEndType()),
                nonce);
    }
}
