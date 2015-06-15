/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rub.nds.tlsattacker.util;

import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @author Florian Pfützenreuter <Florian.Pfuetzenreuter@rub.de>
 */
public class ArrayConverterTest {

    /**
     * Test of longToUint64Bytes method, of class ArrayConverter.
     */
    @Test
    public void testLongToUint64Bytes() {
    }

    /**
     * Test of longToUint32Bytes method, of class ArrayConverter.
     */
    @Test
    public void testLongToUint32Bytes() {
    }

    /**
     * Test of intToBytes method, of class ArrayConverter.
     */
    @Test
    public void testIntToBytes() {
    }

    /**
     * Test of bytesToInt method, of class ArrayConverter.
     */
    @Test
    public void testBytesToInt() {
    }

    /**
     * Test of bytesToLong method, of class ArrayConverter.
     */
    @Test
    public void testBytesToLong() {
    }

    /**
     * Test of bytesToHexString method, of class ArrayConverter.
     */
    @Test
    public void testBytesToHexString_byteArr() {
    }

    /**
     * Test of bytesToHexString method, of class ArrayConverter.
     */
    @Test
    public void testBytesToHexString_byteArr_int() {
    }

    /**
     * Test of bytesToHexString method, of class ArrayConverter.
     */
    @Test
    public void testBytesToHexString_byteArr_boolean() {
    }

    /**
     * Test of bytesToHexString method, of class ArrayConverter.
     */
    @Test
    public void testBytesToHexString_3args() {
    }

    /**
     * Test of concatenate method, of class ArrayConverter.
     */
    @Test
    public void testConcatenate_GenericType() {
    }

    /**
     * Test of concatenate method, of class ArrayConverter.
     */
    @Test
    public void testConcatenate_byteArrArr() {
    }

    /**
     * Test of makeArrayNonZero method, of class ArrayConverter.
     */
    @Test
    public void testMakeArrayNonZero() {
    }

    /**
     * Test of bigIntegerToByteArray method, of class ArrayConverter.
     */
    @Test
    public void testBigIntegerToByteArray_3args() {
    }

    /**
     * Test of bigIntegerToByteArray method, of class ArrayConverter.
     */
    @Test
    public void testBigIntegerToByteArray_BigInteger() {
    }

    /**
     * Test of convertListToArray method, of class ArrayConverter.
     */
    @Test
    public void testConvertListToArray() {
    }

    /**
     * Test of hexStringToByteArray method, of class ArrayConverter.
     */
    @Test
    public void testHexStringToByteArray() {
	String hex = "01";
	assertArrayEquals("Testing simple one byte hex value", new byte[] { 0x01 },
		ArrayConverter.hexStringToByteArray(hex));
	hex = "FF";
	assertArrayEquals("Testing one byte hex value > 0x7f", new byte[] { (byte) 0xff },
		ArrayConverter.hexStringToByteArray(hex));
    }

    @Test
    public void testBigIntegerToNullPaddedByteArray() {
	BigInteger test = new BigInteger("1D42C86F7923DFEC", 16);

	assertArrayEquals("Check zero output size", new byte[0],
		ArrayConverter.bigIntegerToNullPaddedByteArray(test, 0));
	assertArrayEquals("Check check output size smaller than input", new byte[] { (byte) 0xEC },
		ArrayConverter.bigIntegerToNullPaddedByteArray(test, 1));
	assertArrayEquals("Check output size bigger than input size",
		ArrayConverter.hexStringToByteArray("0000000000000000000000001D42C86F7923DFEC"),
		ArrayConverter.bigIntegerToNullPaddedByteArray(test, 20));
    }
}