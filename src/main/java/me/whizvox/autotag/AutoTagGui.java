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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class AutoTagGui extends JFrame {

  private JLabel labelFilePath;
  private JTextField textFilePath;
  private JButton btnFilePathBrowse;
  private JFileChooser fcOsuMap;
  private JLabel labelSplitCriteria;
  private JLabel labelNumDiffs;
  private JTextField textNumDiffs;
  private JLabel labelObjectCount;
  private JTextField textObjectCount;
  private JButton btnGenerate;
  private JButton btnClean;
  private JButton btnGoto;
  private JLabel labelOut;
  private JMenuBar menuBar;
  private JComboBox<String> comboSplitCriteria;

  private JLabel labelSearchMaps;
  private JTextField textSearchMaps;
  private JButton btnSearchMaps;
  private JList<String> listSearchMaps;
  private Map<String, String> searchedMapsCache;
  private Settings settings;
  private JDialog dialogSettings;
  private JDialog dialogInfo;

  private Font monoFont;

  private boolean searching;
  private Generator generator;
  private UpdateChecker updateChecker;

  public AutoTagGui(Settings settings) {
    this.settings = settings;
    searching = false;
    updateChecker = new UpdateChecker("https://raw.githubusercontent.com/whizvox/AutoTag/master/latestVersion.properties");

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setLayout(null);
    setSize(800, 400);
    setLocationRelativeTo(null);
    setResizable(false);

    monoFont = new Font("monospaced", Font.PLAIN, 12);

    labelFilePath = new JLabel("File Path");
    labelFilePath.setBounds(10, 30, 780, 30);

    textFilePath = new JTextField();
    textFilePath.setBounds(10, 60, 650, 30);

    fcOsuMap = new JFileChooser();

    btnFilePathBrowse = new JButton("Browse");
    btnFilePathBrowse.setBounds(670, 60, 100, 30);
    btnFilePathBrowse.addActionListener(e -> {
      if (e.getSource() == btnFilePathBrowse) {
        int res = fcOsuMap.showOpenDialog(AutoTagGui.this);
        if (res == JFileChooser.APPROVE_OPTION) {
          File file = fcOsuMap.getSelectedFile();
          textFilePath.setText(file.getAbsolutePath());
        }
      }
    });

    labelSplitCriteria = new JLabel("Split difficulties by");
    labelSplitCriteria.setBounds(10, 90, 780, 30);

    comboSplitCriteria = new JComboBox<>();
    comboSplitCriteria.addItem("New Combos");
    comboSplitCriteria.addItem("Object Count");
    comboSplitCriteria.setSelectedItem("New Combos");
    comboSplitCriteria.setBounds(10, 120, 200, 30);
    comboSplitCriteria.addActionListener(e -> {
      if (e.getSource() == comboSplitCriteria) {
        if ("New Combos".equals(comboSplitCriteria.getSelectedItem())) {
          setObjectCountVisible(false);
          generator = new TagGenerator();
        } else if ("Object Count".equals(comboSplitCriteria.getSelectedItem())) {
          setObjectCountVisible(true);
          generator = new ComboGenerator(getObjectCount());
        } else {
          error("Invalid or implemented split criteria chosen <" + String.valueOf(comboSplitCriteria.getSelectedItem()) + ">. Please report this immediately.");
        }
      }
    });
    generator = new TagGenerator();

    labelNumDiffs = new JLabel("Number of Difficulties");
    labelNumDiffs.setBounds(10, 160, 650, 30);

    textNumDiffs = new JTextField("4");
    textNumDiffs.setBounds(10, 190, 50, 30);
    textNumDiffs.setFont(monoFont);

    labelObjectCount = new JLabel("Object Count");
    labelObjectCount.setBounds(200, 160, 650, 30);

    textObjectCount = new JTextField("1");
    textObjectCount.setBounds(200, 190, 50, 30);
    textObjectCount.setFont(monoFont);

    btnGenerate = new JButton("Generate");
    btnGenerate.setBounds(10, 280, 150, 40);
    btnGenerate.addActionListener(GENERATE_LISTENER);

    labelOut = new JLabel();
    labelOut.setBounds(10, 320, 780, 30);

    btnClean = new JButton("Clean");
    btnClean.setBounds(165, 280, 100, 40);
    btnClean.addActionListener(e -> {
      if (generator == null) {
        return;
      }
      if (getFilePath().isEmpty()) {
        return;
      }
      File dir = new File(getFilePath()).getParentFile();
      if (dir == null) {
        error("Could not open parent directory of " + getFilePath());
        return;
      }
      File[] lf = dir.listFiles((dir1, name) -> generator.getFilePattern().matcher(name).matches());
      for (File f : lf) {
        if (!f.delete()) {
          System.err.println("Could not delete " + f.getAbsolutePath());
        }
      }
      output("Deleted " + lf.length + " files");
    });

    btnGoto = new JButton("Open");
    btnGoto.setBounds(270, 280, 100, 40);
    btnGoto.addActionListener(e -> {
      if (getFilePath().isEmpty()) {
        return;
      }
      try {
        Desktop.getDesktop().open(new File(getFilePath()).getParentFile());
      } catch (Exception ex) {
        error("Could not open parent directory");
      }
    });

    searchedMapsCache = new HashMap<>();
    labelSearchMaps = new JLabel("Search Beatmaps");
    labelSearchMaps.setBounds(420, 90, 300, 30);
    textSearchMaps = new JTextField();
    textSearchMaps.setBounds(420, 120, 240, 30);
    btnSearchMaps = new JButton("Search");
    btnSearchMaps.setBounds(670, 120, 100, 30);
    btnSearchMaps.addActionListener(e -> {
      if (e.getSource() == btnSearchMaps) {
        String query = getSearchMapsQuery();
        if (!query.isEmpty() && settings.getOsuDirectory() != null && !searching) {
          search(query);
        }
      }
    });
    listSearchMaps = new JList<>();
    listSearchMaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listSearchMaps.setBounds(420, 160, 350, 200);
    listSearchMaps.addListSelectionListener(e -> {
      textFilePath.setText(searchedMapsCache.get(listSearchMaps.getSelectedValue()));
    });

    menuBar = new JMenuBar();
    menuBar.setBounds(0, 0, getWidth(), 25);
    JMenu menuFile = new JMenu("File");
    menuFile.setMnemonic(KeyEvent.VK_F);
    JMenuItem itemInfo = new JMenuItem("Information");
    menuFile.getAccessibleContext().setAccessibleDescription("The file menu. Can access information and change properties related to the application.");
    itemInfo.addActionListener(e -> {
      dialogInfo = new JDialog(AutoTagGui.this, getTitle() + " - Information", Dialog.ModalityType.APPLICATION_MODAL);
      dialogInfo.setSize(400, 400);
      dialogInfo.setContentPane(new AutoTagInfoGui(this));
      dialogInfo.setLocationRelativeTo(AutoTagGui.this);
      dialogInfo.setVisible(true);
    });
    menuFile.add(itemInfo);
    JMenuItem itemSettings = new JMenuItem("Settings");
    itemSettings.addActionListener(e -> {
      dialogSettings = new JDialog(AutoTagGui.this, getTitle() + " - Settings", Dialog.ModalityType.APPLICATION_MODAL);
      dialogSettings.setSize(400, 400);
      dialogSettings.setContentPane(new AutoTagSettingsGui(this));
      dialogSettings.setLocationRelativeTo(AutoTagGui.this);
      dialogSettings.setVisible(true);
    });
    menuFile.add(itemSettings);
    JMenuItem itemExit = new JMenuItem("Exit");
    itemExit.addActionListener(e -> AutoTagGui.this.dispatchEvent(new WindowEvent(AutoTagGui.this, WindowEvent.WINDOW_CLOSING)));
    menuFile.add(itemExit);
    menuBar.add(menuFile);
    JMenu menuHelp = new JMenu("Help");
    menuHelp.setMnemonic(KeyEvent.VK_H);
    menuHelp.getAccessibleContext().setAccessibleDescription("The help menu. If you are having trouble using this software, this will provide some more information.");
    JMenuItem itemRepo = new JMenuItem("Github repository");
    itemRepo.addActionListener(e -> {
      try {
        Desktop.getDesktop().browse(URI.create("https://github.com/whizvox/AutoTag"));
      } catch (IOException ex) {
        error(ex.getMessage());
      }
    });
    menuHelp.add(itemRepo);
    JMenuItem itemWiki = new JMenuItem("Help wiki");
    itemWiki.addActionListener(e -> {
      try {
        Desktop.getDesktop().browse(URI.create("https://github.com/whizvox/AutoTag/wiki"));
      } catch (IOException ex) {
        error(ex.getMessage());
      }
    });
    menuHelp.add(itemWiki);
    JMenuItem itemBugs = new JMenuItem("Report a bug");
    itemBugs.addActionListener(e -> {
      try {
        Desktop.getDesktop().browse(URI.create("https://github.com/whizvox/AutoTag/issues"));
      } catch (IOException ex) {
        error(ex.getMessage());
      }
    });
    menuHelp.add(itemBugs);
    menuBar.add(menuHelp);

    getContentPane().add(menuBar);
    getContentPane().add(labelFilePath);
    getContentPane().add(textFilePath);
    getContentPane().add(btnFilePathBrowse);
    getContentPane().add(labelSplitCriteria);
    getContentPane().add(comboSplitCriteria);
    getContentPane().add(labelNumDiffs);
    getContentPane().add(textNumDiffs);
    getContentPane().add(btnGenerate);
    getContentPane().add(labelOut);
    getContentPane().add(btnClean);
    getContentPane().add(btnGoto);
    getContentPane().add(labelSearchMaps);
    getContentPane().add(textSearchMaps);
    getContentPane().add(btnSearchMaps);
    JScrollPane scrollPane = new JScrollPane(listSearchMaps);
    scrollPane.setBounds(listSearchMaps.getBounds());
    getContentPane().add(scrollPane);

    getContentPane().add(labelObjectCount);
    getContentPane().add(textObjectCount);
    setObjectCountVisible(false);
  }

  public Settings getSettings() {
    return settings;
  }

  public String getFilePath() {
    return textFilePath.getText();
  }

  public String getSearchMapsQuery() {
    return textSearchMaps.getText();
  }

  public String getNumDiffsStr() {
    return textNumDiffs.getText();
  }

  public int getNumDiffs() {
    try {
      return Integer.parseInt(getNumDiffsStr());
    } catch (NumberFormatException e) {}
    return 0;
  }

  private String getObjectCountStr() {
    return textObjectCount.getText();
  }

  public int getObjectCount() {
    try {
      return Integer.parseInt(getObjectCountStr());
    } catch (NumberFormatException e) {}
    return 0;
  }

  public String getSplitCriteria() {
    return (String) comboSplitCriteria.getSelectedItem();
  }

  public UpdateChecker getUpdateChecker() {
    return updateChecker;
  }

  public void closeSettingsDialog() {
    dialogSettings.dispatchEvent(new WindowEvent(dialogSettings, WindowEvent.WINDOW_CLOSING));
  }

  public void closeInformationDialog() {
    dialogInfo.dispatchEvent(new WindowEvent(dialogInfo, WindowEvent.WINDOW_CLOSING));
  }

  private void setObjectCountVisible(boolean visible) {
    labelObjectCount.setVisible(visible);
    textObjectCount.setVisible(visible);
    repaint();
  }

  public void error(String msg) {
    System.err.println(msg);
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  public void output(String msg) {
    System.out.println(msg);
    JOptionPane.showMessageDialog(this, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
  }

  private void search(String query) {
    final Path songsDir = Paths.get(settings.getOsuDirectory(), "Songs");
    if (!Files.exists(songsDir)) {
      error("No songs directory found at " + songsDir.toString());
    }
    Thread t = new Thread(() -> {
      btnSearchMaps.setText("Searching...");
      btnSearchMaps.setEnabled(false);
      searching = true;
      List<File> res = new ArrayList<>();
      File[] lf = songsDir.toFile().listFiles();
      File[] lfd;
      if (lf != null) {
        for (File dir : lf) {
          lfd = dir.listFiles((d, name) -> Generator.isValidOsuFile(name) && StringUtils.containsIgnoreCase(name, query));
          if (lfd != null) {
            Collections.addAll(res, lfd);
          }
        }
      }
      searchedMapsCache.clear();
      String[] listData = new String[res.size()];
      for (int i = 0; i < res.size(); i++) {
        final File f = res.get(i);
        searchedMapsCache.put(f.getName(), f.getAbsolutePath());
        listData[i] = f.getName();
      }
      listSearchMaps.setListData(listData);
      btnSearchMaps.setText("Search");
      btnSearchMaps.setEnabled(true);
      searching = false;
    });
    t.start();
  }

  private final ActionListener GENERATE_LISTENER = e -> {
    if (getFilePath().isEmpty()) {
      error("An input file must be specified");
      return;
    }
    File file = new File(getFilePath());
    if (!file.exists()) {
      error("File <" + file.getAbsolutePath() + "> does not exist");
      return;
    }
    if (file.isDirectory()) {
      error("File <" + file.getAbsolutePath() + "> cannot be a directory");
      return;
    }
    int numDiffs = getNumDiffs();
    if (numDiffs < 2 || numDiffs > 1000) {
      error("Invalid number of difficulties <" + numDiffs + "> [2,1000]");
      return;
    }
    int objectCount = getObjectCount();
    if (objectCount < 1 || objectCount > 10000) {
      error("Invalid number of objects <" + objectCount + "> [1,10000]");
      return;
    }
    try {
      if (generator != null) {
        if (generator.getOut() == null) {
          generator.setOut(new ArrayList<>());
        }
        generator.setIn(new File(getFilePath()));
        if (generator instanceof TagGenerator) {
          TagGenerator g = (TagGenerator)generator;
          g.setOut(getNumDiffs());
        } else if (generator instanceof ComboGenerator) {
          ComboGenerator g = (ComboGenerator)generator;
          g.setObjectsPerCombo(getObjectCount());
          g.setOut(getNumDiffs());
        }
        generator.generate();
      }
    } catch (IOException ex) {
      error("Could not generate difficulties: " + ex.getMessage());
      ex.printStackTrace();
      return;
    }
    output("Generated successfully!");
  };

}
