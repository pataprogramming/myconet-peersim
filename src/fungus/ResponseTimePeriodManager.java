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

import java.util.logging.Logger;

import peersim.cdsim.CDSimulator;
import peersim.config.Configuration;

public class ResponseTimePeriodManager {

  private int[] numberOfJobsInPeriod;
  private long[] jobsInPeriod;
  private int numberOfJobs;
  private double theoreticResponseTime;

  private static Logger log = Logger.getLogger(ResponseTimePeriodManager.class
                                               .getName());

  public ResponseTimePeriodManager() {

    int numberOfCycles = Configuration.getInt(CDSimulator.PAR_CYCLES);
    numberOfJobsInPeriod = new int[numberOfCycles];
    jobsInPeriod = new long[numberOfCycles];

    numberOfJobs = (int) (BurstJobGenerator.jobRatio * TypeObserver
                          .getTotalCapacity());

    for (int i = 0; i < numberOfCycles; i++) {
      numberOfJobsInPeriod[i] = numberOfJobs;
      jobsInPeriod[i] = 0;
    }

    theoreticResponseTime = computeTheoreticResponseTime();

  }

  public void completeJob(Job j) {
    if (numberOfJobs > 0) {
      int jobPeriod = j.id / numberOfJobs;

      numberOfJobsInPeriod[jobPeriod]--;
      jobsInPeriod[jobPeriod] += j.completionTime - j.creationTime + 1;
    }
  }

  public double getResponseTimeOptimality() {
    return theoreticResponseTime / getMeasuredResponseTime();
  }

  public double getMeasuredResponseTime() {

    int period = 0;
    double responseTimeSum = 0;
    int partialJobs=0;


    while (numberOfJobsInPeriod[period] == 0
           && period < numberOfJobsInPeriod.length - 1) {
      responseTimeSum = jobsInPeriod[period];
      period++;
    }

    //if (period < numberOfJobsInPeriod.length - 1) {
    //      responseTimeSum += jobsInPeriod[period];
    //      partialJobs = numberOfJobs - numberOfJobsInPeriod[period];
    //}

    //log.severe(responseTimeSum + " " + numberOfJobs);
    //return responseTimeSum / (partialJobs + period * numberOfJobs);
    return responseTimeSum / numberOfJobs;
  }

  private double computeTheoreticResponseTime() {

    double C = TypeObserver.getTotalCapacity();
    double J = numberOfJobs;
    double N = Math.floor(J / C);

    return ((N + 1) / J) * (((C * N) / 2) + (J / C - N));

  }

}
