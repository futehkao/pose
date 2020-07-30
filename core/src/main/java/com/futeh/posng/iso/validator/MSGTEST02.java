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

package com.futeh.posng.iso.validator;

import com.futeh.posng.iso.ISOException;
import com.futeh.posng.util.LogEvent;
import com.futeh.posng.util.Logger;
import com.futeh.posng.iso.ISOBaseValidator;
import com.futeh.posng.iso.ISOComponent;
import com.futeh.posng.iso.ISOMsg;
import com.futeh.posng.iso.ISOVError;
import com.futeh.posng.iso.ISOVMsg;

/**
 * VALIDATES FIELD INTERDEPENDENCY. IF FIELD 0 ENDS WITH 1 THEN
 * FIELD 1 MUST END WITH 0.
 * ONLY TEST PURPOSE.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class MSGTEST02 extends ISOBaseValidator {

    public MSGTEST02() {
        super();
    }

    public MSGTEST02( boolean breakOnError ) {
        super(breakOnError);
    }

    private String makeStrFromArray( int[] validFields ){
        if ( validFields == null ) return null;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < validFields.length; i++){
            result.append( validFields[i] );
            result.append( ", " );
        }
        result.delete( result.length()-2, result.length()-1 );
        return result.toString(  );
    }

    public ISOComponent validate(ISOComponent m) throws ISOException {
        LogEvent evt = new LogEvent( this, "validate" );
        try {
            super.validate ( m );
            ISOMsg msg = (ISOMsg)m;
            int[] validFields = { 0,1 };
            if ( !msg.hasFields( validFields ) ){
                ISOVError e = new ISOVError( "Fields " + makeStrFromArray( validFields ) + " must appear in msg.", "002" );
                if ( msg instanceof ISOVMsg )
                    ((ISOVMsg)msg).addISOVError( e );
                else
                    msg = new ISOVMsg( msg, e );
                if ( breakOnError )
                    throw new ISOVException ( "Error on msg. " , msg );
            }
            /** field interdependency **/
            if ( msg.getString( 0 ).endsWith( "1" )  && !msg.getString( 1 ).endsWith( "0" ) ){
                ISOVError e = new ISOVError( "If field 0 ends with 1 then field 1 must end with 0.", "003" );
                if ( msg instanceof ISOVMsg )
                    ((ISOVMsg)msg).addISOVError( e );
                else
                    msg = new ISOVMsg( msg, e );
                if ( breakOnError )
                    throw new ISOVException ( "Error on msg. " , msg );
            }
            return msg;
        } catch ( ISOVException ex ) {
            throw ex;
        } finally {
            Logger.log( evt );
        }
    }
}
