package io;

import java.io.*;

public abstract class LineReader {
  public abstract void add(String line);

  public static <T extends LineReader> T handle(boolean skipfirst, T lineReader, String filename) throws IOException {
    try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
      String line;
      if (skipfirst) {
        in.readLine(); // skip first line
      }
      while (null != (line = in.readLine())) {
        lineReader.add(line);
     }
    }
    return lineReader;
  }
}