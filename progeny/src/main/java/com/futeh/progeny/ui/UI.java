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

package com.futeh.progeny.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Arrays;

import com.futeh.progeny.ui.action.Redirect;
import com.futeh.progeny.util.Log;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Alejandro Revilla
 *
 * <p>jPOS UI main class</p>
 *
 * @see UIFactory
 *
 * See src/examples/ui/* for usage details
 */
public class UI implements UIFactory, UIObjectFactory {
    JFrame mainFrame;
    Map registrar, mapping;
    Element config;
    UIObjectFactory objFactory;

    Log log;
    boolean destroyed = false;
    static final ResourceBundle classMapping;

    static {
        classMapping = ResourceBundle.getBundle(UI.class.getName());
    }
    /**
     * Create a new UI object
     */
    public UI () {
        super ();
        registrar = new HashMap ();
        mapping = new HashMap ();
        setObjectFactory (this);
    }
    /**
     * Creates a new UI object
     * @param config configuration element
     */
    public UI (Element config) {
        this ();
        setConfig (config);
    }
    /**
     * Assigns an object factory use to create new object instances.
     * If no object factory is asigned, UI uses the default classloader
     *
     * @param objFactory reference to an Object Factory
     */
    public void setObjectFactory (UIObjectFactory objFactory) {
        this.objFactory = objFactory;
    }
    /**
     * @param config the Configuration element
     */
    public void setConfig (Element config) {
        this.config = config;
    }
    /**
     * @param Log an optional Log instance
     * @see Log
     */
    public void setLog (Log log) {
        this.log = log;
    }
    public Log getLog () {
        return log;
    }
    /**
     * UI uses a map to hold references to its components
     * ("id" attribute)
     *
     * @return UI component registrar
     */
    public Map getRegistrar () {
        return registrar;
    }
    /**
     * @param id Component id ("id" configuration attribute)
     * @return the Object or null
     */
    public Object get (String id) {
        return registrar.get (id);
    }
   /**
    * UI is itself a UIFactory. 
    * This strategy is used to recursively instantiate components
    * inside a container
    * 
    * @param ui reference to this UI instance
    * @param e free form configuration Element
    * @return JComponent
    */
    public JComponent create (UI ui, Element e) {
        return create (e);
    }
    /**
     * UIObjectFactory implementation.
     * uses default classloader
     * @param clazz the Clazzzz
     * @return the Object
     * @throws Exception throw exception if unable to instantiate
     */
    public Object newInstance (String clazz) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader ();
        Class type = cl.loadClass (clazz);
        return type.newInstance ();
    }
    /**
     * configure this UI object
     */
    public void configure () throws JDOMException {
        configure (config);
    } 
    /**
     * reconfigure can be used in order to re-configure components
     * inside a container (i.e. changing a panel in response to
     * an event).
     * @see Redirect
     *
     * @param elementName the element name used as new configuration
     * @param panelName panel ID (see "id" attribute)
     */
    public void reconfigure (String elementName, String panelName) {
        Container c = 
            panelName == null ? mainFrame.getContentPane() : ((JComponent) get (panelName));
        if (c != null) {
            c.removeAll ();
            c.add (
                createComponent (config.getChild (elementName))
            );
            if (c instanceof JComponent) {
                ((JComponent)c).revalidate ();
            }
            c.repaint ();
        }
    }
    /**
     * dispose this UI object
     */
    public void dispose () {
     /* This is the right code for the dispose, but it freezes in
        JVM running under WinXP (in linux went fine.. I didn't 
        test it under other OS's)
        (last version tested: JRE 1.5.0-beta2)
  
        if (mainFrame != null) {
            // dumpComponent (mainFrame);
            mainFrame.dispose ();
     */
        destroyed = true;

        Iterator it = (Arrays.asList(Frame.getFrames())).iterator();

        while (it.hasNext()) {
            JFrame jf = (JFrame) it.next();
            removeComponent(jf);
        }
    }
    /**
     * @return true if this UI object has been disposed and is no longer valid
     */
    public boolean isDestroyed () {
        return destroyed;
    }

    @SuppressWarnings("unchecked")
    protected void configure (Element ui) throws JDOMException {
        setLookAndFeel (ui);
        createMappings (ui);
        createObjects (ui, "object");
        createObjects (ui, "action");
        if (!"ui".equals (ui.getName())) {
            ui = ui.getChild ("ui");
        }
        if (ui != null) {
            JFrame frame = initFrame (ui);
            Element mb = ui.getChild ("menubar");
            if (mb != null) 
                frame.setJMenuBar (buildMenuBar (mb));

            frame.setContentPane (
                createComponent (ui.getChild ("components"))
            );
            if ("true".equals (ui.getAttributeValue ("full-screen"))) {
                GraphicsDevice device = GraphicsEnvironment
                                            .getLocalGraphicsEnvironment()
                                            .getDefaultScreenDevice();
                frame.setUndecorated (
                    "true".equals (ui.getAttributeValue ("undecorated"))
                );
                device.setFullScreenWindow(frame);
            } else {
                frame.setVisible (true);
            }
        }
    }

    private void removeComponent (Component c) {
        if (c instanceof Container) {
            Container cont = (Container) c;
            Component[] cc = cont.getComponents();
            
            for (int i=0; i<cc.length; i++) {
                removeComponent (cc[i]);
            }
            cont.removeAll();
        }
    }

    // ##DEBUG##
    private void dumpComponent (Component c) {
        System.out.println (c.getClass().getName() + ":" + c.getBounds().getSize().toString());
        if (c instanceof Container) {
            Component[] cc = ((Container) c).getComponents();
            for (int i=0; i<cc.length; i++) {
                dumpComponent (cc[i]);
            }
        }
    }

    private JFrame initFrame (Element ui) {
        Element caption = ui.getChild ("caption");
        mainFrame = caption == null ?  
            new JFrame () :
            new JFrame (caption.getText());

        JOptionPane.setRootFrame (mainFrame);

        mainFrame.getContentPane().setLayout(new BorderLayout());

        String close = ui.getAttributeValue ("close");

        if ("false".equals (close))
            mainFrame.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        else if ("exit".equals (close))
            mainFrame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(getDimension (ui, screenSize));
        locateOnScreen (mainFrame);
        return mainFrame;
    }

    private void locateOnScreen(Frame frame) {
        Dimension paneSize   = frame.getSize();
        Dimension screenSize = frame.getToolkit().getScreenSize();
        frame.setLocation(
            (screenSize.width  - paneSize.width)  / 2,
            (screenSize.height - paneSize.height) / 2);
    }
    private JMenuBar buildMenuBar (Element ui) {
        JMenuBar mb = new JMenuBar ();
        Iterator iter = ui.getChildren ("menu").iterator();
        while (iter.hasNext()) 
            mb.add (menu ((Element) iter.next()));

        return mb;
    }
    private JMenu menu (Element m) {
        JMenu menu = new JMenu (m.getAttributeValue ("id"));
        setItemAttributes (menu, m);
        Iterator iter = m.getChildren ().iterator();
        while (iter.hasNext()) 
            addMenuItem (menu, (Element) iter.next());
        return menu;
    }
    private void addMenuItem (JMenu menu, Element m) {
        String tag = m.getName ();

        if ("menuitem".equals (tag)) {
            JMenuItem item = new JMenuItem (m.getAttributeValue ("id"));
            setItemAttributes (item, m);
            menu.add (item);
        } else if ("menuseparator".equals (tag)) {
            menu.addSeparator ();
        } else if ("button-group".equals (tag)) {
            addButtonGroup (menu, m);
        } else if ("check-box".equals (tag)) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem (
                m.getAttributeValue ("id")
            );
            setItemAttributes (item, m);
            item.setState (
                "true".equals (m.getAttributeValue ("state"))
            );
            menu.add (item);
        } else if ("menu".equals (tag)) {
            menu.add (menu (m));
        }
    }
    private void addButtonGroup (JMenu menu, Element m) {
        ButtonGroup group = new ButtonGroup();
        Iterator iter = m.getChildren ("radio-button").iterator();
        while (iter.hasNext()) {
            addRadioButton (menu, group, (Element) iter.next());
        }
    }
    private void addRadioButton (JMenu menu, ButtonGroup group, Element m) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem
            (m.getAttributeValue ("id"));
        setItemAttributes (item, m);
        item.setSelected (
            "true".equals (m.getAttributeValue ("selected"))
        );
        group.add (item);
        menu.add (item);
    }
    private Dimension getDimension (Element e, Dimension def) {
        String w = e.getAttributeValue ("width");
        String h = e.getAttributeValue ("height");

        return new Dimension (
           w != null ? Integer.parseInt (w) : def.width,
           h != null ? Integer.parseInt (h) : def.height
        );
    }
    private void setItemAttributes (AbstractButton b, Element e) 
    {
        String s = e.getAttributeValue ("accesskey");
        if (s != null && s.length() == 1)
            b.setMnemonic (s.charAt(0));

        String icon = e.getAttributeValue ("icon");
        if (icon != null) {
            try {
                b.setIcon (new ImageIcon (new URL (icon)));
            } catch (MalformedURLException ex) {
                ex.printStackTrace ();
            }
        }
        b.setActionCommand (e.getAttributeValue ("command"));
        String actionId = e.getAttributeValue ("action");
        if (actionId != null) {
            b.addActionListener ((ActionListener) get (actionId));
        }
    }
    protected void setLookAndFeel (Element ui) {
        String laf = ui.getAttributeValue ("look-and-feel");
        if (laf != null) {
            try {
                UIManager.setLookAndFeel (laf);
            } catch (Exception e) {
                warn (e);
            };
        }
    }
    private JComponent createComponent (Element e) {
        if (e == null)
            return new JPanel ();

        JComponent component;
        UIFactory factory = null;
        String clazz = e.getAttributeValue ("class");
        if (clazz == null) 
            clazz = (String) mapping.get (e.getName());
        if (clazz == null) {

            try {
                clazz = classMapping.getString (e.getName());
            } catch (MissingResourceException ex) {
                // no class attribute, no mapping
                // let MBeanServer do the yelling
            }
        }
        try {
            if (clazz == null) 
                factory = this;
            else 
                factory = (UIFactory) objFactory.newInstance (clazz.trim());

            component = factory.create (this, e);
            setSize (component, e);
            if (component instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) component;
                b.setActionCommand (e.getAttributeValue ("command"));
                String actionId = e.getAttributeValue ("action");
                if (actionId != null) {
                    b.addActionListener ((ActionListener) get (actionId));
                }
            }
            put (component, e);

            Element script = e.getChild ("script");
            if (script != null) 
                component = doScript (component, script);

            if ("true".equals (e.getAttributeValue ("scrollable")))
                component = new JScrollPane (component);
        } catch (Exception ex) {
            ex.printStackTrace ();
            warn ("Error instantiating class " + clazz);
            warn (ex);
            component = new JLabel ("Error instantiating class " + clazz);
        }
        return component;
    }
    protected JComponent doScript (JComponent component, Element e) {
        return component;
    }
    private void setSize (JComponent c, Element e) {
        String w = e.getAttributeValue ("width");
        String h = e.getAttributeValue ("height");
        Dimension d = c.getPreferredSize ();
        double dw = d.getWidth ();
        double dh = d.getHeight ();
        if (w != null) 
            dw = Double.parseDouble (w);
        if (h != null) 
            dh = Double.parseDouble (h);
        if (w != null || h != null) {
            d.setSize (dw, dh);
            c.setPreferredSize (d);
        }
    }
    public JComponent create (Element e) {
        JComponent component = null;

        Iterator iter = e.getChildren().iterator();
        for (int i=0; iter.hasNext(); i++) {
            JComponent c = createComponent((Element) iter.next ());
            if (i == 0)
                component = c;
            else if (i == 1) {
                JPanel p = new JPanel ();
                p.add (component);
                p.add (c);
                component = p;
                put (component, e);
            } else {
                component.add (c);
            }
        }
        return component;
    }
    public JFrame getMainFrame() {
        return mainFrame;
    }
    
    private void createObjects (Element e, String name) {
        Iterator iter = e.getChildren (name).iterator ();
        while (iter.hasNext()) {
            try {
                Element ee = (Element) iter.next ();
                String clazz = ee.getAttributeValue ("class");
                Object obj = objFactory.newInstance (clazz.trim());
                if (obj instanceof UIAware) {
                    ((UIAware) obj).setUI (this, ee);
                }
                put (obj, ee);
            } catch (Exception ex) {
                warn (ex);
            }
        }
    }
    private void createMappings (Element e) {
        Iterator iter = e.getChildren ("mapping").iterator ();
        while (iter.hasNext()) {
            try {
                Element ee = (Element) iter.next ();
                String name  = ee.getAttributeValue ("name");
                String clazz = ee.getAttributeValue ("factory");
                mapping.put (name, clazz);
            } catch (Exception ex) {
                warn (ex);
            }
        }
    }
    protected void warn (Object obj) {
        if (log != null)
            log.warn (obj);
    }
    private void put (Object obj, Element e) {
        String id = e.getAttributeValue ("id");
        if (id != null) {
            registrar.put (id, obj);
        }
    }
}

