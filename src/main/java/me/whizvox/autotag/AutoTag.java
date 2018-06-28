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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AutoTag {

  public static final int BUILD = 1;
  public static final Date RELEASE_DATE = new Date(1530150480000L);
  public static final String VERSION = "1.0";

  public static void main(String[] args) {
    Settings settings = new Settings(new File("autotag.properties"));
    try {
      settings.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    AutoTagGui gui = new AutoTagGui(settings);
    gui.setTitle("AutoTag " + VERSION);
    gui.setVisible(true);
    try {
      settings.save();
    } catch (IOException e) {
      System.err.println("Could not save settings.");
      e.printStackTrace();
    }
  }

  // osu! doesn't really have a default install directory for Mac or Linux
  public static String getDefaultOsuPath() {
    String userHome = System.getProperty("user.home");
    String osName = System.getProperty("os.name");
    if (osName.contains("Windows")) {
      return Paths.get(userHome, "AppData", "Local", "osu!").toString();
    } else if (osName.contains("Mac OS X")) {
      return Paths.get(userHome, "Application Support", "osu!").toString();
    }
    return null;
  }

  public static List<Path> searchBeatmaps(String osuDir, String query) throws IllegalArgumentException, IOException {
    Path songsDir = Paths.get(osuDir, "Songs");
    if (!Files.isDirectory(songsDir)) {
      throw new IllegalArgumentException("Songs directory must not be a file");
    }
    if (!Files.exists(songsDir)) {
      return new ArrayList<>(0);
    }
    return Files.walk(songsDir, 2)
        .filter(path -> {
          if (Files.isRegularFile(path)) {
            final String name = path.getFileName().toString();
            return Generator.isValidOsuFile(name) && StringUtils.containsIgnoreCase(name, query);
          }
          return false;
        })
        .collect(Collectors.toList());
  }

}
