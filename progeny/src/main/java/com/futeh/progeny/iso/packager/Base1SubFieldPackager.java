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

package com.futeh.progeny.iso.packager;

import java.util.*;

import com.futeh.progeny.iso.*;
import com.futeh.progeny.util.LogEvent;
import com.futeh.progeny.util.Logger;

/**
 * ISO 8583 v1987 BINARY Packager 
 * customized for VISA Base1 subfields
 * 
 *
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @version $Id$
 * @see ISOBasePackager
 * @see ISOComponent
 */

public class Base1SubFieldPackager extends ISOBasePackager 
{
    // These methods are identical to ISOBasePackager
    // except that fld[1] has been replaced with fld[0]
    // and a secondard bitmap is not allowed

    protected boolean emitBitMap()
    {
        return (fld[0] instanceof ISOBitMapPackager);
    }

    protected int getFirstField() {
        if (fld[0] == null)
            return 1;
        return (fld[0] instanceof ISOBitMapPackager) ? 1 : 0;
    }

    protected ISOFieldPackager getBitMapfieldPackager() 
    {
        return fld[0];
    }

    /**
     * Unpack a packed subfield into
     * its corresponding ISOComponent
     */

    public int unpack (ISOComponent m, byte[] b) throws ISOException 
    {
        LogEvent evt = new LogEvent (this, "unpack");
        try 
        {
            if (m.getComposite() != m) 
                throw new ISOException ("Can't call packager on non Composite");
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (b));

            int consumed=0;
            ISOBitMap bitmap = new ISOBitMap (-1);

            BitSet bmap = null;
            int maxField = fld.length;
            if (emitBitMap())
            {
                consumed += getBitMapfieldPackager().unpack(bitmap,b,consumed);
                bmap = (BitSet) bitmap.getValue();
                m.set (bitmap);
                maxField = bmap.size();
            }
            for (int i=getFirstField(); i<maxField && consumed < b.length; i++) 
            {
                if (bmap == null || bmap.get(i)) 
                {
                    ISOComponent c = fld[i].createComponent(i);
                    consumed += fld[i].unpack (c, b, consumed);
                    m.set(c);
                }
            }
            if (b.length != consumed) 
            {
                evt.addMessage (
                "WARNING: unpack len=" +b.length +" consumed=" +consumed);
            }
            return consumed;
        } 
        catch (ISOException e) 
        {
            evt.addMessage (e);
            throw e;
        } 
        finally 
        {
            Logger.log (evt);
        }
    }

    /**
     * Pack the subfield into a byte array
     */ 

    public byte[] pack (ISOComponent m) throws ISOException 
    {
        LogEvent evt = new LogEvent (this, "pack");
        try 
        {
            ISOComponent c;
            List v = new ArrayList();
            Map<Integer, Object> fields = m.getChildren();
            int len = 0;
            byte[] b;

            boolean emitBitMap = emitBitMap();
            if (emitBitMap)
            {
                // BITMAP (-1 in Map)
                c = (ISOComponent) fields.get (-1);
                b = getBitMapfieldPackager().pack(c);
                len += b.length;
                v.add (b);
            }

            for (int i=getFirstField(); i<=m.getMaxField(); i++) 
            {
                c = (ISOComponent) fields.get (i);
                if (c == null && !emitBitMap) {
                    if (fld[i] instanceof ISOBinaryFieldPackager) {
                        c = new ISOBinaryField();
                        c.setValue(new byte[0]);
                    } else if (fld[i] instanceof ISOBitMapPackager) {
                        c = new ISOBitMap(fld[i].getLength());
                        c.setValue(new byte[fld[i].getLength()]);
                    } else if (fld[i] instanceof ISOMsgFieldPackager) {
                        c = new ISOMsg();
                    } else {
                        c = new ISOField();
                        c.setValue("");
                    }
                }

                if (c != null) {
                    try {
                        b = fld[i].pack(c);
                        len += b.length;
                        v.add (b);
                    } catch (Exception e) {
                        evt.addMessage ("error packing field "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                    }
                }
            }
            int k = 0;
            byte[] d = new byte[len];
            for (int i=0; i<v.size(); i++) 
            {
                b = (byte[]) v.get(i);
                for (int j=0; j<b.length; j++)
                d[k++] = b[j];
            }
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (d));
            return d;
        } 
        catch (ISOException e) 
        {
            evt.addMessage (e);
            throw e;
        } 
        finally 
        {
            Logger.log(evt);
        }
    }
}


