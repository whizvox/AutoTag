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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StringUtils {

  public static void splitByChar(String in, char separator, List<String> out) {
    out.clear();
    int last = 0;
    final int L = in.length();
    char c;
    for (int i = 0; i < L; i++) {
      c = in.charAt(i);
      if (c == separator) {
        out.add(in.substring(last, i));
        last = i + 1;
      }
    }
    out.add(in.substring(last));
  }

  public static boolean containsIgnoreCase(String str, String a) {
    return str.toLowerCase().contains(a.toLowerCase());
  }

  public static boolean parseBoolean(String s) throws IllegalArgumentException {
    if ("0".equals(s) || "false".equals(s)) {
      return false;
    } else if ("1".equals(s) || "true".equals(s)) {
      return true;
    }
    throw new IllegalArgumentException("Not a valid boolean <" + s + ">");
  }

  private static final SimpleDateFormat FORMAT_HUMAN_FRIENDLY = new SimpleDateFormat("dd MMM, yyyy, kk:mm:ss");

  public static String getHumanFriendlyDate(Date date) {
    return FORMAT_HUMAN_FRIENDLY.format(date);
  }

  public static Date parseHumanFriendlyDate(String str) {
    return FORMAT_HUMAN_FRIENDLY.parse(str, new ParsePosition(0));
  }

}
