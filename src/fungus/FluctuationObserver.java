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

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
//import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
import edu.uci.ics.jung.algorithms.util.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.picking.*;
import edu.uci.ics.jung.visualization.renderers.*;
import org.apache.commons.collections15.*;
import org.apache.commons.collections15.functors.*;

public class FluctuationObserver implements Control {
  private Graph<MycoNode,MycoEdge> graph;

  private static final String PAR_TESTNAME = "config.basename";

  private static String basename;
  private static String filename;

  private static final String PAR_PERIOD = "period";
  private static int period;

  private static Logger log =
      Logger.getLogger(FluctuationObserver.class.getName());

  private static Set<ChangeListener> changeListeners =
      new HashSet<ChangeListener>();

  // ah: (key, val) = (times fluctuated, number of nodes)
  public static HashMap<Integer,Integer> fluctuations;

  public FluctuationObserver(String prefix) {
    basename = Configuration.getString(PAR_TESTNAME);
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);
    graph = JungGraphObserver.getGraph();
    clearStats();

    File resultsDir = new File("results");
    if (!resultsDir.exists()) {
      resultsDir.mkdir();
    }
    StringBuffer fn = new StringBuffer();
    java.util.Formatter f = new java.util.Formatter(fn, Locale.US);
    f.format("%s/fluc-%s-%d.txt", resultsDir.getPath(), basename,
             CommonState.seed);
    filename = fn.toString();
  }

  private static void clearStats() {
    fluctuations = new HashMap<Integer,Integer>();
  }

  public static void updateStats() {

    clearStats();
    MycoList all = MycoCast.getAllNodes();

    for (MycoNode n : all) {
      int flucs = n.getHyphaData().getBulwarkEntries();
      // ah: it's not a fluctuation until second time in bulwark state
      if (flucs > 0) {
        flucs = flucs -1;
      }
      Integer key = new Integer(flucs);
      Integer val = fluctuations.get(key);
      if (val == null) {
        fluctuations.put(key, new Integer(1));
      }
      else {
        fluctuations.put(key, val + 1);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public boolean execute() {

    if (CDState.getCycle() % period != 0)
        return false;

    updateStats();
    if (fluctuations.isEmpty())
        return false;

    StringBuilder sb = new StringBuilder();
    java.util.Formatter f = new java.util.Formatter(sb, Locale.US);

    Integer i = 0;

    HashMap<Integer,Integer> map =
        (HashMap<Integer,Integer>) fluctuations.clone();
    while (!map.isEmpty()) {
      Integer val = map.remove(i);
      if (val == null) {
        f.format("%1d:%1d, ", i, 0);
      }
      else {
        f.format("%1d:%1d, ", i, val);
      }
      i++;
    }

    // ah: remove last commaspace and add a newline
    sb.delete(sb.length()-2, sb.length());
    sb.append("\n");

    // ah: TODO: copy experiment writer to get this writing to
    // a file, too, so i can make a graph out of it.
    try {
      FileOutputStream out = new FileOutputStream(filename, true);
      PrintStream p = new PrintStream(out);
      p.print(sb.toString());
      out.close();
    } catch (IOException e) {
      log.severe("Error writing fluctuation data to file");
      debug(e.getMessage());
    }

    log.info("fluctuation counts " + sb.toString());
    return false;
  }

  public static void debug(String s) {
    System.out.println("DEBUG: " + s);
  }

}
