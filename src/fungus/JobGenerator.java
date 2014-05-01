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

import java.util.*;
import java.util.logging.*;

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;

public class JobGenerator implements Control {
  private static final String PAR_HYPHADATA_PROTO =
      "network.node.hyphadata_proto";

  // Generate this proportion of jobs, relative to the total capacity of
  // each type
  private static final String PAR_JOB_RATIO = "jobratio";

  // Size of a generated job
  private static final String PAR_JOB_SIZE = "jobsize";

  // Interval (in cycles) between bursts of generated jobs
  private static final String PAR_PERIOD = "period";

  // Use single random entry points for jobs (for each burst)
  private static final String PAR_SINGLE_ENTRY_POINT = "single_entry_point";

  private static final String PAR_VARIED = "varied";
  private static final String PAR_NUM_TYPES = "num_types";
  private static final String PAR_TYPE_RATIO = "typeratio";

  private static final String PAR_START = "start";
  private static final String PAR_END = "end";

  private static int hyphadataPid;
  public static double jobRatio; // Number of jobs of each type added each cycle
  //private static double jobRatio; // Number of jobs of each type added each cycle
  private static int jobSize;
  public static int period;
  private static boolean single_entry_point;

  private static boolean varied; // Specify variable ratio of jobs per type
  // Must specify a proto.typeratio.n for each!
  private static int numTypes;   // The number of types...must agree w/
  // the value set for TypeInitializer

  private static int start;
  private static int end;

  private static List<Double> ratios;
  public static int jobsThisPeriod;

  private static Logger log = Logger.getLogger(MycoCast.class.getName());

  public JobGenerator(String prefix) {
    hyphadataPid = Configuration.getPid(PAR_HYPHADATA_PROTO);
    jobSize = Configuration.getInt(prefix + "." + PAR_JOB_SIZE);
    period = Configuration.getInt(prefix + "." + PAR_PERIOD);
    single_entry_point = Configuration.getBoolean(prefix + "." +
                                                  PAR_SINGLE_ENTRY_POINT);

    start = Configuration.getInt(prefix + "." + PAR_START);
    end = Configuration.getInt(prefix + "." + PAR_END);

    varied = Configuration.getBoolean(prefix + "." + PAR_VARIED);
    if (varied) {
      numTypes = TypeInitializer.getNumTypes();
      ratios = new ArrayList<Double>();
      for (int i=0; i < numTypes; i++) {
        ratios.add(Configuration.getDouble(prefix + "." +
                                           PAR_TYPE_RATIO + "." + i));
      }
      System.out.println("RATIOS: " + ratios);
      throw new RuntimeException("FIXME: Varied ratios incompatible with current ResponseTimePeriodManager implementation");
    } else {
      jobRatio = Configuration.getDouble(prefix + "." + PAR_JOB_RATIO);
    }
    jobsThisPeriod = 0;
  }


  public boolean execute() {
    if ((CDState.getCycle() % period != 0) || CDState.getCycle() < start
        || CDState.getCycle() > end) {
      return false;
    }

    jobsThisPeriod = 0;

    int numTypes = TypeInitializer.getNumTypes();

    if (varied) {
      StringBuilder sb = new StringBuilder();
      java.util.Formatter f = new java.util.Formatter(sb, Locale.US);
      for (int i=0; i < numTypes; i++) {
        f.format("%d: %f - ", i, ratios.get(i));
      }
      log.info("Adding jobs by ratio: " + sb.toString());
    } else {
      log.info("Adding " + jobRatio +
               " times total capacity for each of " + numTypes +
               " types");
    }

    MycoNode n;
    HyphaData d;

    List<JobStats> jobStats = JobController.getJobStats();

    int totalJobs = 0;

    // HyphaData.numTypes = numTypes;

    for (int type = 0; type < numTypes; type++) {

      int totalCapacity = TypeObserver.getTypeCapacity(type);

      if (totalCapacity == 0) {
        log.severe("No nodes of type " + type + "; not generating jobs");
        continue;
      }

      double typeRatio;
      if (varied) {
        typeRatio = ratios.get(type);
      } else {
        typeRatio = jobRatio;
      }

      if (JobController.responseTimeManager == null) {
        JobController.responseTimeManager = new ResponseTimePeriodManager();
      }

      int jobCount = (new Double(totalCapacity * typeRatio)).intValue();

      jobsThisPeriod += jobCount;
      totalJobs += jobCount;

      for (JobStats js : jobStats) {
        if (js.type == type) {
          js.incrementCreatedJobs(jobCount);
        }
      }

      log.fine("Generating " + jobCount + " jobs of type " + type +
               " (for type capacity " + totalCapacity + ")");
      n = TypeObserver.getRandomNodeOfType(type);

      for (int i = 0; i < jobCount; i++) {
        if (!single_entry_point) {
          n = TypeObserver.getRandomNodeOfType(type);
        }
        // n = (MycoNode) nl.get(generator.nextInt(nl.size()));
        d = n.getHyphaData();
        log.log(Level.FINER, "Sending job of type" + type + " to node "
                + n.getID(), n);
        d.enqueue(new Job(type, jobSize));
      }
    }
    return false;
  }
}
