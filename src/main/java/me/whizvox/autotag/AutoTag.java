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
import java.nio.file.Paths;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoTag {

  public static final int BUILD = 2;
  public static final Date RELEASE_DATE = new Date(1530169126000L);
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

  private static final Pattern PATTERN_OSU_FILE = Pattern.compile(".* - .* \\(.*\\) \\[.*].osu");

  public static boolean isValidOsuFile(String fileName) {
    Matcher m = PATTERN_OSU_FILE.matcher(fileName);
    return m.matches();
  }

}
