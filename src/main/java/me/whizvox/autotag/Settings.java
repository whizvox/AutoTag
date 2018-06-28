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

import java.io.*;
import java.util.Properties;

public class Settings {

  private File file;
  private String osuDirectory;
  private boolean checkUpdates;

  public Settings(File file) {
    this.file = file;
  }

  public Settings load() throws IOException {
    Properties props = new Properties();
    if (!file.exists()) {
      try {
        new FileOutputStream(file).close();
      } catch (IOException ignored) {}
    } else {
      props.load(new FileReader(file));
    }
    if (props.containsKey("osuDirectory")) {
      setOsuDirectory(props.getProperty("osuDirectory"));
    } else {
      setOsuDirectory(AutoTag.getDefaultOsuPath());
    }
    if (props.containsKey("checkUpdates")) {
      try {
        setCheckUpdates(StringUtils.parseBoolean(props.getProperty("checkUpdates")));
      } catch (IllegalArgumentException e) {
        setCheckUpdates(true);
      }
    } else {
      setCheckUpdates(true);
    }
    return this;
  }

  public Settings save() throws IOException {
    Properties props = new Properties();
    props.setProperty("osuDirectory", osuDirectory);
    props.setProperty("checkUpdates", String.valueOf(checkUpdates));
    props.store(new FileWriter(file), "Settings for the AutoTag Java application");
    return this;
  }

  public String getOsuDirectory() {
    return osuDirectory;
  }

  public void setOsuDirectory(String osuDirectory) {
    this.osuDirectory = osuDirectory;
  }

  public boolean canCheckUpdates() {
    return checkUpdates;
  }

  public void setCheckUpdates(boolean checkUpdates) {
    this.checkUpdates = checkUpdates;
  }

}
