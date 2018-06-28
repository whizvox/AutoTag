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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

public class UpdateChecker {

  private String url;
  private String fetchedVersion;
  private int fetchedBuild;
  private Date fetchedDate;
  private String fetchedDownloadUrl;

  public UpdateChecker(String url) {
    this.url = url;
    fetchedVersion = null;
    fetchedBuild = 0;
    fetchedDownloadUrl = null;
    fetchedDate = null;
  }

  public String getLatestVersion() {
    return fetchedVersion;
  }

  public int getLatestBuild() {
    return fetchedBuild;
  }

  public Date getDate() {
    return fetchedDate;
  }

  public String getLatestDownloadUrl() {
    return fetchedDownloadUrl;
  }

  public void refresh() throws IOException {
    URL u = new URL(url);
    HttpURLConnection conn;
    try {
      conn = (HttpURLConnection)u.openConnection();
    } catch (ClassCastException e) {
      throw new IOException("Not an HTTP URL: " + url);
    }
    conn.setRequestMethod("GET");
    conn.connect();
    int response = conn.getResponseCode();
    if (response >= 400) {
      throw new IOException("Response code: " + response);
    }
    Properties props = new Properties();
    try (InputStream in = conn.getInputStream()) {
      props.load(in);
    }
    if (props.containsKey("build")) {
      String fetchedBuildStr = props.getProperty("build");
      try {
        fetchedBuild = Integer.parseInt(fetchedBuildStr);
      } catch (NumberFormatException e) {
        throw new IOException("Malformed build number: " + fetchedBuildStr);
      }
    } else {
      throw new IOException("Could not retrieve build number");
    }
    if (props.containsKey("version")) {
      fetchedVersion = props.getProperty("version");
    } else {
      throw new IOException("Could not retrieve version");
    }
    if (props.containsKey("date")) {
      String fetchedDateStr = props.getProperty("date");
      try {
        fetchedDate = new Date(Long.parseLong(fetchedDateStr));
      } catch (NumberFormatException e) {
        throw new IOException("Malformed epoch for date: " + fetchedDateStr);
      }
    } else {
      throw new IOException("Could not retrieve date");
    }
    if (props.containsKey("download")) {
      fetchedDownloadUrl = props.getProperty("download");
      if (fetchedDownloadUrl.equals("NOTREADY")) {
        fetchedDownloadUrl = null;
      }
    } else {
      throw new IOException("Could not retrieve download url");
    }
  }

}
