/* Copyright (c) 2014, Paul L. Snyder <paul@pataprogramming.com>,
 * Daniel Dubois, Nicolo Calcavecchia.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * Any later version. It may also be redistributed and/or modified under the
 * terms of the BSD 3-Clause License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package fungus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Logger;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;

public class ExperimentWriter implements Control {
  private static final String PAR_TESTNAME = "config.basename";

  private static String basename;
  private static String filename;

  private static Logger log = Logger.getLogger(ExperimentWriter.class.getName());

  private static List<Metric> metrics = new LinkedList<Metric>();

  private boolean headerPrinted = false;

  public ExperimentWriter(String prefix) {
    basename = Configuration.getString(PAR_TESTNAME);

    File resultsDir = new File("results");
    if (!resultsDir.exists()) {
      resultsDir.mkdir();
    }

    Date ts = (new Date());
    StringBuffer fn = new StringBuffer();
    java.util.Formatter f = new java.util.Formatter(fn, Locale.US);
    /*
     * f.format("%s/%s-%02d%02d%02d-%02d%02d%02d.csv",
     * resultsDir.getPath(), basename, ts.getYear(), ts.getMonth(),
     * ts.getDay(), ts.getHours(), ts.getMinutes(), ts.getSeconds())
     */;
    //f.format("%s/%s-%d.txt", resultsDir.getPath(), basename,
    //              System.currentTimeMillis());
    f.format("%s/%s-%d.txt", resultsDir.getPath(), basename,
             CommonState.seed);
    filename = fn.toString();

  }

  public static void addMetric(Metric m) {
    if (metrics.isEmpty()) {
      metrics.add(new Metric<Long>("cycle") {public Long fetch() { return CommonState.getTime(); }});
    }
    if (! metrics.contains(m)) {
      metrics.add(m);
    }
  }

  public boolean execute() {

    try {
      FileOutputStream out = new FileOutputStream(filename, true);
      PrintStream p = new PrintStream(out);

      if (!headerPrinted) {
        StringBuffer h = new StringBuffer();
        for (Metric m : metrics) {
          h.append(m.name);
          h.append(",");
        }
        h.setCharAt(h.length() - 1, '\n');

        p.print(h.toString());
        out.close();

        headerPrinted = true;
      }


      StringBuffer l = new StringBuffer();

      for (Metric m : metrics) {
        l.append(m);
        l.append(",");
      }
      l.setCharAt(l.length() - 1, '\n');

      p.print(l.toString());
      out.close();
    } catch (IOException e) {
      log.severe("Error writing experimental data to file");
    }
    return false;
  }
}
