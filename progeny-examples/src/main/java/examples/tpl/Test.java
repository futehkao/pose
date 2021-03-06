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

package examples.tpl;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.futeh.progeny.core.Configuration;
import com.futeh.progeny.core.SimpleConfiguration;
import com.futeh.progeny.tpl.PersistentEngine;
import com.futeh.progeny.util.Logger;
import com.futeh.progeny.util.SimpleLogListener;

public class Test {
    public static void main (String args[]) {
        try {
            Logger logger = new Logger();
            logger.addListener (new SimpleLogListener(System.out));
            Configuration cfg = new SimpleConfiguration
                ("src/examples/tpl/test.cfg");
            PersistentEngine engine =
                new PersistentEngine (cfg, logger, "persistent-engine");

            HCPeer peer = new HCPeer (engine);
            engine.create (new HC("6000000000000001"));
            engine.create (new HC("6000000000000002"));
            engine.create (new HC("6000000000000003"));
            engine.create (new HC("6000000000000004"));
            engine.create (new HC("6000000000000005"));
            engine.create (new HC("6000000000000006"));
            engine.create (new HC("6000000000000007"));
            engine.create (new HC("6000000000000008"));
            engine.create (new HC("6000000000000009"));
            engine.create (new HC("6000000000000010"));
            engine.remove (new HC("6000000000000005"));

            Iterator iter = 
                peer.findByRange ("6000000000000002", "6000000000000008")
                    .iterator();
            while (iter.hasNext()) {
                HC nf = (HC) iter.next();
                System.out.println ("> " +nf.getPan());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
