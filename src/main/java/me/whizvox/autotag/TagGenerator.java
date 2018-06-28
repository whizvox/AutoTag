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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TagGenerator extends Generator {

  public TagGenerator(File in, List<File> out) {
    super(in, out);
  }

  public TagGenerator() {
    super();
  }

  public void setOut(int numDiffs) {
    out.clear();
    final String origName = in.getAbsolutePath();
    for (int i = 0; i < numDiffs; i++) {
      out.add(i, new File(getAlteredFilePath(origName, i + 1)));
    }
  }

  @Override
  public void generate() throws IOException {
    List<BufferedWriter> writers = new ArrayList<>(out.size());
    try {
      for (int i = 0; i < out.size(); i++) {
        writers.add(i, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out.get(i)), "UTF-8")));
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in), "UTF-8"))) {
        boolean readingMetadata = false;
        boolean readingHitObjects = false;
        // the first object has a new combo, so this will be set to 0
        // EDIT: Apparently this doesn't happen with some maps
        int currentIndex = -1;
        String line;
        int lineNumber = 1;
        List<String> tokens = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
          if (!readingHitObjects) {
            if (readingMetadata) {
              StringUtils.splitByChar(line, ':', tokens);
              if (tokens.size() > 1 && tokens.get(0).equals("Version")) {
                for (int i = 0; i < writers.size(); i++) {
                  writers.get(i).write("Version:" + tokens.get(1) + " - PLAYER" + (i + 1));
                  writers.get(i).newLine();
                }
                continue;
              }
            }
            for (BufferedWriter w : writers) {
              w.write(line);
              w.newLine();
            }
            if (!line.isEmpty() && line.charAt(0) == '[') {
              if (line.equals("[HitObjects]")) {
                readingHitObjects = true;
              } else {
                readingMetadata = line.equals("[Metadata]");
              }
            }
          } else {
            StringUtils.splitByChar(line, ',', tokens);
            if (tokens.size() > 3) {
              String objectTypeStr = tokens.get(3);
              int objectType;
              try {
                objectType = Integer.parseInt(objectTypeStr);
              } catch (NumberFormatException e) {
                throw new IOException("Not a valid object type <" + objectTypeStr + "> at line " + lineNumber);
              }
              // if it's a new combo and not a spinner
              if ((objectType & 0b0100) != 0 && (objectType & 0b1000) == 0) {
                currentIndex++;
                if (currentIndex >= out.size() || currentIndex < 0) {
                  currentIndex = 0;
                }
              }
              // if the object is a spinner, write the line to all files (a.k.a. all players need to spin)
              if ((objectType & 0b1000) != 0) {
                for (BufferedWriter w : writers) {
                  w.write(line);
                  w.newLine();
                }
              } else {
                if (currentIndex < 0) {
                  currentIndex = 0;
                }
                writers.get(currentIndex).write(line);
                writers.get(currentIndex).newLine();
              }
            }
          }
          lineNumber++;
        }
      }
    } finally {
      for (BufferedWriter w : writers) {
        if (w != null) {
          try {
            w.close();
          } catch (IOException e) {}
        }
      }
    }
  }

  @Override
  public Pattern getFilePattern() {
    return PATTERN_TAG_DIFF;
  }

  private static Pattern PATTERN_TAG_DIFF = Pattern.compile(".*\\[(.* - PLAYER[0-9]+)].osu$");

  private static String getAlteredFilePath(String filePath, int index) {
    return filePath.substring(0, filePath.lastIndexOf(']')) + " - PLAYER" + index + "].osu";
  }

}
