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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.logging.Logger;

import peersim.core.Control;
import edu.uci.ics.jung.graph.Graph;

public class JobController implements Control {

  private static List<Graph<MycoNode, MycoEdge>> typeGraphs;

  private static List<JobStats> stats = new ArrayList<JobStats>();
  private static Queue<Job> queue = new LinkedList<Job>();
  public static ResponseTimePeriodManager responseTimeManager = null;
  private static ThroughputMeter tm = new ThroughputMeter();

  private static Logger log = Logger.getLogger(JobController.class.getName());

  public JobController(String prefix) {
    if (responseTimeManager == null) {
      responseTimeManager = new ResponseTimePeriodManager();
    }
  }

  public static void complete(Job j) {
    // FIXME: rationalize logic for completing jobs
    stats.get(j.type).complete(j);
    responseTimeManager.completeJob(j);
    tm.completeJob(j);

  }

  public static void rescue(Job j) {
    queue.add(j);
  }

  public boolean execute() {
    while (stats.size() < HyphaData.numTypes) {
      stats.add(new JobStats(stats.size()));
    }
    log.fine(queue.size() + " jobs from dead nodes need to be requeued.");
    while (!queue.isEmpty()) {
      Job j = queue.remove();
      MycoNode n = TypeObserver.getRandomNodeOfType(j.type);
      log.finer("Requeueing job " + j + " at node " + n);
      j.requeue(n);
    }

    StringBuilder sb = new StringBuilder();
    java.util.Formatter f = new java.util.Formatter(sb, Locale.US);

    f.format("Job statistics: ");
    for (JobStats js : stats) {
      f.format("%s ", js.toString());
    }

    log.fine(sb.toString());

    sb = new StringBuilder();
    f = new java.util.Formatter(sb, Locale.US);

    f.format("Active jobs: ");
    for (int i = 0; i < TypeObserver.getNumTypes(); i++) {
      f.format("%d:%d/%d ", i, TypeObserver.getActiveJobsOfType(i),
               TypeObserver.getTypeCapacity(i));
    }

    log.fine(sb.toString());

    return false;
  }

  public static List<JobStats> getJobStats() {
    return stats;
  }

  public static double getThroughput() {
    return tm.getThroughput();
  }

  public static double getThroughputOptimality() {
    return tm.getThroughputOptimality();
  }

}
