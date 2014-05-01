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

import java.util.logging.*;

import peersim.cdsim.CDState;

public class Job implements Comparable<Job> {
  public static int idCounter = 0;

  public int id;
  public int type;
  public int workUnits;
  public int remaining;
  public int creationTime;

  public int timesTransferred = 0;
  public int timesRequeued = 0;
  private boolean started = false;
  private boolean completed = false;
  public int completionTime = 0;

  private static Logger log = Logger.getLogger(Job.class.getName());

  public Job(int type, int workUnits) {
    id = idCounter++;
    creationTime = CDState.getCycle();

    this.type = type;
    this.workUnits = workUnits;

    remaining = workUnits;
  }

  public void requeue(MycoNode n) {
    timesRequeued++;
    transfer(n);
  }

  public void transfer(MycoNode n) {
    timesTransferred += 1;
  }

  public void complete() {
    remaining = 0;
    completed = true;
    completionTime = CDState.getCycle();

    log.finest(this + " completed at cycle " + completionTime
               + " (created at cycle " + creationTime + ")");

    JobController.complete(this);
  }

  // Takes number of available work units as an argument
  // Returns number of work units remaining after processing
  public int process(int work) {
    started = true;
    if (remaining > work) {
      remaining -= work;
      return 0;
    } else {
      int ret = work - remaining;
      complete();
      return ret;
    }
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isCompleted() {
    return completed;
  }

  public int compareTo(Job that) {
    // return that.completionTime - this.completionTime;
    return this.creationTime - that.creationTime;
  }

  public String toString() {
    return "<Job#" + id + "(" + type + "):" + (workUnits - remaining) + "/"
        + workUnits + " units>";
  }
}
