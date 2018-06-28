/*
 * This file is part of AutoTag.
 *
 * AutoTag is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AutoTag is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AutoTag. If not, see <https://www.gnu.org/licenses/>.
 */
package me.whizvox.autotag;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.IOException;

public class AutoTagSettingsGui extends JPanel {

  private JLabel labelOsuDirectory;
  private JTextField textOsuDirectory;
  private JFileChooser fcOsuDirectory;
  private JButton btnOsuDirectory;
  private JLabel labelCheckUpdates;
  private JCheckBox boxCheckUpdates;
  private JButton btnApply;
  private JButton btnBack;

  private AutoTagGui parent;

  public AutoTagSettingsGui(AutoTagGui parent) {
    this.parent = parent;
    setLayout(null);
    setSize(400, 400);

    labelOsuDirectory = new JLabel("osu! directory");
    labelOsuDirectory.setBounds(10, 10, 380, 30);

    textOsuDirectory = new JTextField(parent.getSettings().getOsuDirectory());
    textOsuDirectory.setBounds(10, 40, 270, 30);
    textOsuDirectory.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        checkChanges();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        checkChanges();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        checkChanges();
      }
    });

    fcOsuDirectory = new JFileChooser();
    fcOsuDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    btnOsuDirectory = new JButton("Browse");
    btnOsuDirectory.setBounds(290, 40, 90, 30);
    btnOsuDirectory.addActionListener(e -> {
      int res = fcOsuDirectory.showOpenDialog(this);
      if (res == JFileChooser.APPROVE_OPTION) {
        textOsuDirectory.setText(fcOsuDirectory.getSelectedFile().getAbsolutePath());
      }
    });

    labelCheckUpdates = new JLabel("Check for updates");
    labelCheckUpdates.setBounds(10, 90, 380, 30);

    boxCheckUpdates = new JCheckBox("Enabled", parent.getSettings().canCheckUpdates());
    boxCheckUpdates.setBounds(10, 120, 380, 30);
    boxCheckUpdates.addActionListener(e -> {
      checkChanges();
    });

    btnApply = new JButton("Apply");
    btnApply.setBounds(10, 320, 100, 30);
    btnApply.addActionListener(e -> {
      parent.getSettings().setOsuDirectory(getOsuDirectory());
      parent.getSettings().setCheckUpdates(checkUpdates());
      try {
        parent.getSettings().save();
      } catch (IOException ex) {
        parent.error("Could not save settings: " + ex.getMessage());
      }
      checkChanges();
    });

    btnBack = new JButton("Go Back");
    btnBack.setBounds(120, 320, 100, 30);
    btnBack.addActionListener(e -> {
      parent.closeSettingsDialog();
    });

    add(labelOsuDirectory);
    add(textOsuDirectory);
    add(btnOsuDirectory);
    add(labelCheckUpdates);
    add(boxCheckUpdates);
    add(btnApply);
    add(btnBack);

    checkChanges();
  }

  public String getOsuDirectory() {
    return textOsuDirectory.getText();
  }

  public boolean checkUpdates() {
    return boxCheckUpdates.isSelected();
  }

  private void checkChanges() {
    if (getOsuDirectory().equals(parent.getSettings().getOsuDirectory()) && checkUpdates() == parent.getSettings().canCheckUpdates()) {
      btnApply.setEnabled(false);
    } else {
      btnApply.setEnabled(true);
    }
  }

}
