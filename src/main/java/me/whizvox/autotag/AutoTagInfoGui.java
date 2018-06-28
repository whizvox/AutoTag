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
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class AutoTagInfoGui extends JPanel {

  private JLabel labelVersion;
  private JLabel labelVersionData;
  private JLabel labelBuild;
  private JLabel labelBuildData;
  private JLabel labelReleased;
  private JLabel labelReleasedData;
  private JLabel labelNewUpdates;
  private JLabel labelNewUpdatesData;
  private JButton btnNewUpdates;
  private JButton btnBack;
  private JButton btnGotoDownload;
  private Font dataFont = new Font("monospaced", Font.BOLD, 18);

  private AutoTagGui parent;

  public AutoTagInfoGui(AutoTagGui parent) {
    this.parent = parent;

    setLayout(null);

    labelBuild = new JLabel("Build");
    labelBuild.setBounds(10, 10, 380, 30);
    labelBuildData = new JLabel(String.valueOf(AutoTag.BUILD));
    labelBuildData.setBounds(10, 40, 380, 30);
    labelBuildData.setFont(dataFont);
    labelBuildData.setForeground(Color.RED);

    labelVersion = new JLabel("Version");
    labelVersion.setBounds(10, 80, 380, 30);
    labelVersionData = new JLabel(AutoTag.VERSION);
    labelVersionData.setBounds(10, 110, 380, 30);
    labelVersionData.setFont(dataFont);
    labelVersionData.setForeground(Color.RED);

    labelReleased = new JLabel("Released");
    labelReleased.setBounds(10, 150, 380, 30);
    labelReleasedData = new JLabel(StringUtils.getHumanFriendlyDate(AutoTag.RELEASE_DATE));
    labelReleasedData.setBounds(10, 180, 380, 30);
    labelReleasedData.setFont(dataFont);
    labelReleasedData.setForeground(Color.RED);

    labelNewUpdates = new JLabel("Updates available");
    labelNewUpdates.setBounds(10, 220, 180, 30);
    labelNewUpdatesData = new JLabel();
    labelNewUpdatesData.setBounds(10, 250, 380, 30);
    labelNewUpdatesData.setFont(dataFont);
    labelNewUpdatesData.setForeground(Color.RED);

    btnNewUpdates = new JButton("Check");
    btnNewUpdates.setBounds(200, 225, 100, 20);
    btnNewUpdates.addActionListener(e -> checkNewUpdates(true));

    btnBack = new JButton("Go back");
    btnBack.setBounds(10, 310, 100, 30);
    btnBack.addActionListener(e -> parent.closeInformationDialog());

    btnGotoDownload = new JButton("Go to download");
    btnGotoDownload.setBounds(120, 310, 150, 30);
    btnGotoDownload.addActionListener(e -> {
      try {
        final String url = parent.getUpdateChecker().getLatestDownloadUrl();
        if (url != null) {
          Desktop.getDesktop().browse(URI.create(parent.getUpdateChecker().getLatestDownloadUrl()));
        }
      } catch (IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    checkNewUpdates(parent.getUpdateChecker().getLatestVersion() == null);
    if (!parent.getSettings().canCheckUpdates()) {
      btnNewUpdates.setEnabled(false);
    }

    add(labelVersion);
    add(labelVersionData);
    add(labelBuild);
    add(labelBuildData);
    add(labelReleased);
    add(labelReleasedData);
    add(labelNewUpdates);
    add(labelNewUpdatesData);
    add(btnNewUpdates);
    add(btnBack);
    add(btnGotoDownload);
  }

  private void checkNewUpdates(boolean refresh) {
    if (parent.getSettings().canCheckUpdates()) {
      UpdateChecker c = parent.getUpdateChecker();
      labelNewUpdatesData.setText("Checking...");
      Thread t = new Thread(() -> {
        try {
          if (refresh) {
            c.refresh();
          }
        } catch (IOException e) {
          e.printStackTrace();
          labelNewUpdatesData.setText("ERROR: " + e.getMessage());
          parent.error("Could not check updates: " + e.getMessage());
          btnGotoDownload.setEnabled(false);
          return;
        }
        final int build = c.getLatestBuild();
        String data;
        if (build == AutoTag.BUILD) {
          data = "No new updates available";
          btnGotoDownload.setEnabled(true);
        } else if (build > AutoTag.BUILD) {
          data = c.getLatestVersion() + "-" + build + " (" + StringUtils.getHumanFriendlyDate(c.getDate()) + ")";
          btnGotoDownload.setEnabled(true);
        } else {
          data = "You are using a dev build";
          btnGotoDownload.setEnabled(true);
        }
        labelNewUpdatesData.setText(data);
      });
      t.start();
    } else {
      labelNewUpdatesData.setText("Update checking disabled");
      btnGotoDownload.setEnabled(false);
    }
  }

}
