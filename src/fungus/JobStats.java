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

import peersim.cdsim.CDState;

public class JobStats {

    public int type;
    public int completedJobs = 0;
    public int createdJobs = 0;

    public long totalWait = 0;
    public long totalTransfers = 0;
    public long totalRequeues = 0;

    private double consistentValue = Double.MAX_VALUE;

    public JobStats(int type) {
        this.type = type;
    }

    public double getAvgResponse() {

        if (CDState.getCycle() % JobGenerator.period == 0) {
            consistentValue = ((double) totalWait) / ((double) completedJobs);
        }

        return consistentValue;
    }

    public double getAvgTransfers() {
        return ((double) totalTransfers) / ((double) completedJobs);
    }

    public double getAvgRequeues() {
        return ((double) totalRequeues) / ((double) completedJobs);
    }

    public void complete(Job j) {

        completedJobs += 1;
        totalWait += ((j.completionTime - j.creationTime) + 1);
        totalTransfers += j.timesTransferred;
        totalRequeues += j.timesRequeued;
    }

    private void reset() {
        completedJobs = 0;
        totalWait = 0;
        totalTransfers = 0;
        createdJobs = 0;
    }

    public void incrementCreatedJobs(int jobCount) {
        createdJobs += jobCount;
    }

    public String toString() {
        return "[JobStats for type " + type + "]->(" + completedJobs
            + " completed, " + getAvgResponse() + " mean response time, "
            + getAvgTransfers() + " mean transfers)";
    }

}