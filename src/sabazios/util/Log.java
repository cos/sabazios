package sabazios.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Log {
	public static boolean active = true;
	private static long time;

	public static void start() {
		if (!active)
			return;
		time = System.currentTimeMillis();
	}

	public static void log(String s) {
		if (!active)
			return;
		System.out.println(s);
	}
	
	public static String timeDiffNow() {
		long t = System.currentTimeMillis() - time;
		String timeString = ""+ t / 1000 + "." + (t % 1000) / 10;
		time = t + time;
		return timeString;
	}

	private static Map<String, Map<String, String>> results = new LinkedHashMap<String, Map<String, String>>();
	private static String currentTestProject = null;
	public static void setCurrentTestProject(String name) {
		currentTestProject = name;
		getReportForProject(name);
	}

	public static void report(String key, String value) {
		if (!active)
			return;
		report(currentTestProject, key, value);
	}
	
	public static void report(String key, int value) {
		report(key, "" +value);
	}

	public static void report(String testProject, String key, String value) {
		if (!active)
			return;
		Map<String, String> m = getReportForProject(testProject);
		System.out.println(key+" -> "+value);
		m.put(key, value);
	}

	private static Map<String, String> getReportForProject(String testProject) {
		Map<String, String> map = results.get(testProject);
		if (map == null) {
			map = new LinkedHashMap<String, String>();
			results.put(testProject, map);
		}
		return map;
	}

	static String[] fields = new String[] {
			":size_LOC",
			":size_methods",
			":size_call_graph",
	    ":pointer_analysis_time" ,
	    ":map_vars_to_pointers_time",
	    ":alpha_beta_accesses_time",
	    ":locks_time",
	    ":potential_races_time",
	    ":races_time",
	    ":atomicity_violations_time",
	    ":alpha_accesses",
	    ":beta_accesses",
	    ":potential_races",
	    ":races",
	    ":atomicity_violations",
	    ":printed_atomicity_violations",
			":real_races",
			":beningn_races",
			":bugs",
			":notes"
	    };
	
	public static String report() {
		String s = "";
		for (String projectName : results.keySet()) {
	    s += projectName + " & ";
	    Map<String, String> project = results.get(projectName);
	    for (String field : fields) {
	      String fieldString = project.get(field);
	      if(fieldString == null)
	      	fieldString = "";
				s += fieldString + " & ";
      } 
	    s += "\\\\\n";
    }

		return "% !TEX root = main.tex\n"+s;
	}


	public static void outputReport(String file) throws IOException {
  	FileWriter fstream = new FileWriter(file);
    BufferedWriter out = new BufferedWriter(fstream);
    out.write(report());
    out.close();
	}

	public static void reportTime(String string) {
		report(string , timeDiffNow());
  }
}