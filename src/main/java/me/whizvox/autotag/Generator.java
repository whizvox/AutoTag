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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Generator {

  protected File in;
  protected List<File> out;

  public Generator(File in, List<File> out) {
    this.in = in;
    this.out = out;
  }

  public Generator() {
    this(null, null);
  }

  public void setIn(File in) {
    this.in = in;
  }

  public File getIn() {
    return in;
  }

  public void setOut(List<File> out) {
    this.out = out;
  }

  public List<File> getOut() {
    return out;
  }

  public abstract void generate() throws IOException;

  public abstract Pattern getFilePattern();

  public final File[] getMatchingFiles(File dir) {
    final Pattern ptrn = getFilePattern();
    return dir.listFiles((d, name) -> ptrn.matcher(name).matches());
  }

  private static final Pattern PATTERN_OSU_FILE = Pattern.compile(".* - .* \\(.*\\) \\[.*].osu");

  public static boolean isValidOsuFile(String fileName) {
    Matcher m = PATTERN_OSU_FILE.matcher(fileName);
    return m.matches();
  }

  public static final int MAX_DIFFICULTIES = 5000;

}
