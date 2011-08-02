package racefix.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Set;

import sabazios.domains.ConcurrentFieldAccess;

public class PrintUtil {

  public static void writeLCDs(String accessesInLCDTestString, String fileName) {
    try {
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(accessesInLCDTestString);
      out.close();
    } catch (Exception e) {// Catch exception if any
    }

  }

  public static void writeRacesToFile(Collection<Set<ConcurrentFieldAccess>> collection, String fileName) {
    try {
      FileWriter fstream = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(fstream);
      for (Set<ConcurrentFieldAccess> set : collection) {
        for (ConcurrentFieldAccess fieldAccess : set) {
          out.write(fieldAccess.toString() + "\n");
        }
      }
      out.close();
    } catch (Exception e) {// Catch exception if any
    }

  }
}
