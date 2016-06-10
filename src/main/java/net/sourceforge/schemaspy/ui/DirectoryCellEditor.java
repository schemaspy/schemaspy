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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DirectoryCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
    private static final long serialVersionUID = 1L;
    private final DbConfigTableModel model;
    private final JTextField dirField;
    private final JPanel editor;
    private File selectedDir;
    private int selectedRow;
    private int selectedColumn;

    public DirectoryCellEditor(final DbConfigTableModel model, File startingDir) {
        this.model = model;
        dirField = new JTextField();
        dirField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                model.setValueAt(new File(dirField.getText()), selectedRow, selectedColumn);
            }

            public void removeUpdate(DocumentEvent evt) {
            }

            public void changedUpdate(DocumentEvent evt) {
                model.setValueAt(new File(dirField.getText()), selectedRow, selectedColumn);
            }
        });
        dirField.setBorder(null);

        final JFileChooser dirSelector = new JFileChooser(startingDir);
        dirSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton browseButton = new JButton("...");
        browseButton.setPreferredSize(new Dimension(12, 12));
        browseButton.setMinimumSize(browseButton.getPreferredSize());
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent action) {
                dirSelector.setCurrentDirectory(new File(dirField.getText()));
                if (dirSelector.showOpenDialog((JButton)action.getSource()) == JFileChooser.APPROVE_OPTION) {
                    dirField.setText(dirSelector.getSelectedFile().getPath());
                }
            }
        });

        editor = new JPanel();
        editor.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        editor.add(dirField, constraints);

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 0, 1);
        editor.add(browseButton, constraints);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        selectedRow = row;
        selectedColumn = column;
        selectedDir = (File)value;

        dirField.setText(selectedDir == null ? null : selectedDir.toString());
        editor.setToolTipText(model.getDescription(row));
        return editor;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public Object getCellEditorValue() {
        return model.getValueAt(selectedRow, selectedColumn);
    }
}