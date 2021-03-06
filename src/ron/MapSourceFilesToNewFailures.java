package ron;

import io.LineReader;
import io.ProgressTracker;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import data.*;

public class MapSourceFilesToNewFailures {
  public static void main(String[]args)throws Exception {
    String basedir = "/Users/ry23/Dropbox/cmu-sfdc/data/";
    String changelists_tsv = basedir+"changelists.txt";
    String runs_tsv = basedir+"runs.txt";
    String test_failures_tsv = basedir+"test_failures.txt";

    ChangelistSourceFiles cls = ChangelistSourceFiles.readChangelistToFileMapping(changelists_tsv);

    Runs runs = LineReader.handle(true, new Runs(), runs_tsv);

    TestFailuresByRun test_failures_by_run_id = TestFailuresByRun.readTestFailures(test_failures_tsv);

    Map<String, Map<String, Integer>> changelist_to_failures = Utils.get_new_failures_by_changelist(cls, runs,
        test_failures_by_run_id);

    Map<String, Map<String, Integer>> source_file_to_failures = new HashMap<>();
    // additional xform step required to accumulate test failure counts by source file
    for (Map.Entry<String, Map<String, Integer>> e : changelist_to_failures.entrySet()) {
      for (String source_file : cls.getSourceFiles(e.getKey())) {
        Utils.increment(source_file_to_failures, source_file, e.getValue());
      }
    }

    try (BufferedWriter out = new BufferedWriter(new FileWriter("docs.txt"));
        ProgressTracker pt = new ProgressTracker(null, "write", -1, "documents", "words", "bytes")) {
      for (Map.Entry<String, Map<String, Integer>> e_failure_count : source_file_to_failures.entrySet()) {
        Map<String, Integer> failure_count = e_failure_count.getValue();
        String src_file_name = e_failure_count.getKey();
        if (src_file_name.contains("\t")) throw new RuntimeException();
        out.write(src_file_name);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : failure_count.entrySet()) {
          for (int i=0; i<e.getValue(); i++) {
            sb.append('\t');
            sb.append(e.getKey());
            pt.advise(0, 1, 0);
          }
        }
        String s = sb.toString();
        out.write(s);
        out.write('\n');
        pt.advise(1, 0, s.length());
      }
    }
  }
}
