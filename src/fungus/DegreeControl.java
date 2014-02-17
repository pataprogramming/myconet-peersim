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
import java.lang.Math;
import java.util.logging.*;

import cern.jet.stat.Descriptive;
import cern.colt.list.DoubleArrayList;
import org.apache.commons.math3.stat.Frequency;

public class DegreeControl implements Control {
  //private static final String PAR_PROTO = "protocol";

  private static Logger log =
      Logger.getLogger(DegreeControl.class.getName());

  private final String name;
  //private final int pid;

  public static double degreeMean = 0.0;
  public static double degreeStdDev = 0.0;
  public static double biomassMean = 0.0;
  public static double biomassStdDev = 0.0;
  public static double hyphaMean = 0.0;
  public static double hyphaStdDev = 0.0;
  public static Frequency frequencies = new Frequency();
  public static Frequency parentTargets = new Frequency();
  public static Frequency biomassFrequencies = new Frequency();
  public static Frequency detectedFailures = new Frequency();
  //public static int disconnectCount = 0;

  public DegreeControl(String name) {
    this.name = name;
    //this.pid = Configuration.getPid(name + "." + PAR_PROTO);

    ExperimentWriter.addMetric(new Metric<Double>("degreeMean") {public Double fetch() { return DegreeControl.degreeMean; }});
    ExperimentWriter.addMetric(new Metric<Double>("degreeStdDev") {public Double fetch() { return DegreeControl.degreeStdDev; }});
    ExperimentWriter.addMetric(new Metric<Double>("biomassMean") {public Double fetch() { return DegreeControl.biomassMean; }});
    ExperimentWriter.addMetric(new Metric<Double>("biomassStdDev") {public Double fetch() { return DegreeControl.biomassStdDev; }});
    ExperimentWriter.addMetric(new Metric<Double>("hyphaMean") {public Double fetch() { return DegreeControl.hyphaMean; }});
    ExperimentWriter.addMetric(new Metric<Double>("hyphaStdDev") {public Double fetch() { return DegreeControl.hyphaStdDev; }});
  }

  public boolean execute() {
    MycoGraph g = JungGraphObserver.getGraph();

    DoubleArrayList ds = new DoubleArrayList(g.getVertexCount());
    DoubleArrayList bs = new DoubleArrayList(g.getVertexCount());
    DoubleArrayList hs = new DoubleArrayList(g.getVertexCount());

    // frequencies = new Frequency();
    // parentTargets = new Frequency();
    // biomassFrequencies = new Frequency();
    // detectedFailures = new Frequency();

    //disconnectCount = 0;

    for (MycoNode n : g.getVertices()) {
      HyphaData data = n.getHyphaData();
      int deg = n.getHyphaLink().neighborCount();
      //int deg = g.outDegree(n);
      ds.add(deg);
      //frequencies.addValue(deg);
      if (data.isBiomass()) {
        bs.add(deg);
        //biomassFrequencies.addValue(deg);
      } else {
        hs.add(deg);
      }

      // parentTargets.addValue(Math.ceil(data.getParentTarget()));
      // detectedFailures.addValue(data.getKnownDisconnect());

      // This is an action! (and why DegreeControl is a control not an observer)
      data.setObservedParents(n.getHyphaLink().getHyphae().size());

      // if (data.wasDisconnectDetected()) {
      //   disconnectCount += 1;
      //   data.clearDisconnectDetected();
      // }
      //data.clearKnownDisconnect();
    }

    degreeMean = Descriptive.mean(ds);
    double degreeVariance =
        Descriptive.variance(ds.size(),
                             Descriptive.sum(ds),
                             Descriptive.sumOfSquares(ds));
    degreeStdDev = Descriptive.standardDeviation(degreeVariance);

    biomassMean = Descriptive.mean(bs);
    double biomassVariance =
        Descriptive.variance(bs.size(),
                             Descriptive.sum(bs),
                             Descriptive.sumOfSquares(bs));
    biomassStdDev = Descriptive.standardDeviation(biomassVariance);

    hyphaMean = Descriptive.mean(hs);
    double hyphaVariance =
        Descriptive.variance(hs.size(),
                             Descriptive.sum(hs),
                             Descriptive.sumOfSquares(hs));
    hyphaStdDev = Descriptive.standardDeviation(hyphaVariance);

    System.out.println("DEGREE STATS: mean = " + degreeMean +
                       " stddev = " + degreeStdDev);
    System.out.println("BIOMASS STATS: mean = " + biomassMean +
                       " stddev = " + biomassStdDev);
    System.out.println("HYPHA STATS: mean = " + hyphaMean +
                       " stddev = " + hyphaStdDev);
    // System.out.println("BIOMASS DEGREE DISTRIB: \n" +
    //                    biomassFrequencies.toString());
    // System.out.println("PARENT TARGET DISTRIB: \n" + parentTargets.toString());
    // System.out.println("DETECTED FAILURE DISTRIB: \n" +
    //                    detectedFailures.toString());

    // log.info("DISCONNECT COUNT: " + disconnectCount);
    return false;
  }
}
