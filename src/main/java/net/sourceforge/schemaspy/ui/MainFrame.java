/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sourceforge.schemaspy.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author John Currier
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JPanel dbConfigPanel = null;
    private JPanel buttonBar = null;
    private JButton launchButton = null;
    private JPanel header;

    /**
     * This is the default constructor
     */
    public MainFrame() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setContentPane(getJContentPane());
        setTitle("SchemaSpy");
        this.setSize(new Dimension(500, 312));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * This method initializes dbConfigPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getDbConfigPanel() {
        if (dbConfigPanel == null) {
            dbConfigPanel = new DbConfigPanel();
        }
        return dbConfigPanel;
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.weightx = 1.0;

            constraints.anchor = GridBagConstraints.CENTER;
            constraints.insets = new Insets(4, 0, 4, 0);
            jContentPane.add(getHeaderPanel(), constraints);
            constraints.insets = new Insets(0, 0, 0, 0);

            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.weighty = 1.0;
            //JScrollPane scroller = new JScrollPane();
            //scroller.setBorder(null);
            //scroller.setViewportView(getDbConfigPanel());
            //scroller.setViewportBorder(new BevelBorder(BevelBorder.LOWERED));
            //jContentPane.add(scroller, constraints);
            jContentPane.add(getDbConfigPanel(), constraints);

//            constraints.fill = GridBagConstraints.VERTICAL;
//            constraints.weighty = 0.0;
//            JLabel filler = new JLabel();
//            filler.setPreferredSize(new Dimension(0, 0));
//            filler.setMinimumSize(new Dimension(0, 0));
//            jContentPane.add(filler, constraints);

            constraints.anchor = GridBagConstraints.SOUTHEAST;
            constraints.fill = GridBagConstraints.NONE;
            constraints.weighty = 0.0;
            jContentPane.add(getButtonBar(), constraints);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonBar
     *
     * @return javax.swing.JPanel
     */
    private JPanel getButtonBar() {
        if (buttonBar == null) {
            buttonBar = new JPanel();
            buttonBar.setLayout(new FlowLayout(FlowLayout.TRAILING));
            buttonBar.add(getLaunchButton(), null);
        }
        return buttonBar;
    }

    private JPanel getHeaderPanel() {
        if (header == null) {
            header = new JPanel();
            header.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            header.add(new JLabel("SchemaSpy - Graphical Database Metadata Browser"), constraints);
            constraints.gridx = 0;
            constraints.gridy++;
            header.add(new JLabel("Select a database type and fill in the required fields"), constraints);
        }
        return header;
    }

    /**
     * This method initializes launchButton
     *
     * @return javax.swing.JButton
     */
    private JButton getLaunchButton() {
        if (launchButton == null) {
            launchButton = new JButton();
            launchButton.setText("Launch");
        }
        return launchButton;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
