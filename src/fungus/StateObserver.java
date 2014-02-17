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

import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import java.util.logging.*;

public class StateObserver implements Control {
  private static final String PAR_PROTO = "protocol";

  private static Logger log =
      Logger.getLogger(StateObserver.class.getName());

  private final String name;
  private final int pid;

  public static int bio = 0;
  public static int bul = 0;
  public static int ext = 0;
  public static int bra = 0;
  public static int imm = 0;
  public static int tot = 0;
  public static int hyp = 0;

  public static double bulwarkRatio = 0.0;
  public static double biomassRatio = 0.0;
  public static double hyphaRatio = 0.0;

  public static int getMinCapacity() {
    int min = Integer.MAX_VALUE;
    for (MycoNode n : MycoCast.getAllNodes()) {
      int i = n.getHyphaData().getCapacity();
      if (i < min) {
        min = i;
      }
    }
    if (min == Integer.MAX_VALUE) {
      min = 0;
    }
    return min;
  }

  public static int getMaxCapacity() {
    int max = 0;
    for (MycoNode n : MycoCast.getAllNodes()) {
      int i = n.getHyphaData().getCapacity();
      if (i > max) {
        max = i;
      }
    }
    return max;
  }


  public StateObserver(String name) {
    this.name = name;
    this.pid = Configuration.getPid(name + "." + PAR_PROTO);

    ExperimentWriter.addMetric(new Metric<Integer>("nodeCount") {
        public Integer fetch() { return StateObserver.tot; }});
    ExperimentWriter.addMetric(new Metric<Integer>("biomassCount") {
        public Integer fetch() { return StateObserver.bio; }});
    ExperimentWriter.addMetric(new Metric<Integer>("hyphaCount") {
        public Integer fetch() { return StateObserver.hyp; }});
    ExperimentWriter.addMetric(new Metric<Integer>("extendingCount") {
        public Integer fetch() { return StateObserver.ext; }});
    ExperimentWriter.addMetric(new Metric<Integer>("branchingCount") {
        public Integer fetch() { return StateObserver.bra; }});
    ExperimentWriter.addMetric(new Metric<Integer>("immobileCount") {
        public Integer fetch() { return StateObserver.imm; }});
    ExperimentWriter.addMetric(new Metric<Integer>("bulwarkCount") {
        public Integer fetch() { return StateObserver.bul; }});

  }

  public boolean execute() {
    MycoCast mycocast = (MycoCast) Network.get(0).getProtocol(pid);

    bio = mycocast.countBiomass();
    bul = mycocast.countBulwark();
    ext = mycocast.countExtending();
    bra = mycocast.countBranching();
    imm = mycocast.countImmobile();
    hyp = ext + bra + imm;
    tot = bio + bul + ext + bra + imm;

    bulwarkRatio = ((double) bul) / ((double) tot);
    biomassRatio = ((double) bio) / ((double) tot);
    hyphaRatio = ((double) (ext + bra + imm)) / ((double) tot);

    log.info("MYCOCAST STATS: " + bio + " biomass, " +
             ext + " extending, " + bra + " branching, " +
             imm + " immobile, " + bul + " bulwark.");
    return false;
  }
}
