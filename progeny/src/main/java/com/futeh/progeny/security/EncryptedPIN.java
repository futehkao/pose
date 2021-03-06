/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package com.futeh.progeny.security;

import java.io.PrintStream;
import java.io.Serializable;

import com.futeh.progeny.iso.ISOUtil;
import com.futeh.progeny.util.Loggeable;


/**
 * <p>
 * The PIN (Personal Identification Number), is used to authenticate card
 * holders. A user enters his/her PIN on the pin-pad (also called pin entry device)
 * of a terminal (whether ATM or POS). The terminal forms the PIN Block, which
 * is a mix of the PIN and the account number.<br>
 * In a typical environment, the PIN Block (not the PIN) gets encrypted and sent
 * to the acquirer. This Encrypted PIN Block is the typical content of the
 * PIN Data ISO Field (Field 52).
 * This class represents an encrypted PIN, no matter by whom it is encrypted.
 * Typically a PIN is encrypted under one of these three:<br>
 * 1- Under a terminal PIN key (like TPK or DUKPT)<br>
 * 2- Under an Interchange PIN key (like ZPK)<br>
 * 3- Under the the security module itself (i.e. under LMK)<br>
 * This class knows nothing about, who encrypted it.
 * </p>
 * <p>
 * This class represents an encrypted PIN using:<br>
 * 1- The PIN Block (encrypted)<br>
 * 2- The account number (the 12 right-most digits of the account number excluding the check digit)<br>
 * 3- The PIN Block Format<br>
 * </p>
 * <p>
 * The PIN Block Format specifies how the clear pin (as entered by the card holder)
 * and the account number get mixed to form the PIN Block.<br>
 * </p>
 * @author Hani Samuel Kirollos
 * @version $Revision$ $Date$
 * @see SMAdapter
 */
public class EncryptedPIN
        implements Serializable, Loggeable {
    /**
     * Account Number (the 12 right-most digits of the account number excluding the check digit)
     */
    String accountNumber;
    /**
     * This is the ByteArray holding the PIN Block
     * The PIN Block can be either clear or encrypted
     * It is typically DES or 3DES encrypted, with length 8 bytes.
     */
    byte[] pinBlock;
    /**
     * The PIN Block Format
     * value -1 means no block format defined
     */
    byte pinBlockFormat;

    public EncryptedPIN () {
    }

    /**
     *
     * @param pinBlock
     * @param pinBlockFormat
     * @param accountNumber (also functions correctly, if the complete account number with the check digit is passed)
     */
    public EncryptedPIN (byte[] pinBlock, byte pinBlockFormat, String accountNumber) {
        setPINBlock(pinBlock);
        setPINBlockFormat(pinBlockFormat);
        setAccountNumber(accountNumber);
    }

    /**
     *
     * @param pinBlockHexString the PIN Block represented as a HexString instead of a byte[]
     * @param pinBlockFormat
     * @param accountNumber (also functions correctly, if the complete account number with the check digit is passed)
     */
    public EncryptedPIN (String pinBlockHexString, byte pinBlockFormat, String accountNumber) {
        this(ISOUtil.hex2byte(pinBlockHexString), pinBlockFormat, accountNumber);
    }


    /**
     * dumps PIN basic information
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see Loggeable
     */
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<encrypted-pin");
        p.print(" format=\"0" + getPINBlockFormat() + "\"");
        p.println(">");
        p.println(inner + "<pin-block>" + ISOUtil.hexString(getPINBlock()) + "</pin-block>");
        p.println(inner + "<account-number>" + getAccountNumber() + "</account-number>");
        p.println(indent + "</encrypted-pin>");
    }

    /**
     *
     * @param pinBlock
     */
    public void setPINBlock (byte[] pinBlock) {
        this.pinBlock = pinBlock;
    }

    /**
     *
     * @return pinBlock
     */
    public byte[] getPINBlock () {
        return  pinBlock;
    }

    /**
     *
     * @param pinBlockFormat
     */
    public void setPINBlockFormat (byte pinBlockFormat) {
        this.pinBlockFormat = pinBlockFormat;
    }

    /**
     *
     * @return PIN Block Format
     */
    public byte getPINBlockFormat () {
        return  this.pinBlockFormat;
    }

    /**
     * Sets the 12 right-most digits of the account number excluding the check digit
     * @param accountNumber (also functions correctly, if the complete account number with the check digit is passed)
     */
    public void setAccountNumber (String accountNumber) {
        this.accountNumber = extractAccountNumberPart(accountNumber);
    }

    /**
     * @return accountNumber (the 12 right-most digits of the account number excluding the check digit)
     */
    public String getAccountNumber () {
        return  accountNumber;
    }

    /**
     * This method extracts the 12 right-most digits of the account number,
     * execluding the check digit.
     * @param accountNumber (PAN) consists of the BIN (Bank Identification Number), accountNumber
     * and a check digit.
     * @return the 12 right-most digits of the account number, execluding the check digit.
     */
    public static String extractAccountNumberPart (String accountNumber) {
        String accountNumberPart = null;
        if (accountNumber.length() > 12)
            accountNumberPart = accountNumber.substring(accountNumber.length() -
                    13, accountNumber.length() - 1);
        else
            accountNumberPart = accountNumber;
        return  accountNumberPart;
    }


}



