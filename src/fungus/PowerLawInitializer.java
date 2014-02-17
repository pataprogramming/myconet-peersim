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
import peersim.config.*;
import peersim.core.*;
import peersim.util.*;
import peersim.dynamics.NodeInitializer;
import java.util.logging.*;
import cern.jet.random.engine.*;
import cern.jet.random.Distributions.*;

public class PowerLawInitializer implements Control, NodeInitializer {
  private static final String PAR_ALPHA = "alpha";
  private static final String PAR_MAX = "max";

  private static LogManager manager = LogManager.getLogManager();
  private static Logger log = Logger.getLogger("fungus");

  private final String name;
  private static double alpha;
  private static double max;
  private static int intmax;
  //private final int pid;

  private static cern.jet.random.engine.RandomEngine generator;

  public PowerLawInitializer(String name) {
    this.name = name;
    alpha = Configuration.getDouble(name + "." + PAR_ALPHA);
    max = Configuration.getDouble(name + "." + PAR_MAX);
    intmax = (new Double(max).intValue());
    this.generator =
        new cern.jet.random.engine.MersenneTwister(CommonState.r.nextInt(Integer.MAX_VALUE));
  }

  public static double nextPowLaw(double alpha, double max) {
    return cern.jet.random.Distributions.nextPowLaw(alpha,max,generator);
  }

  public static int nextPowInt() {
    return intmax - (new Double(nextPowLaw(alpha,max))).intValue();
  }


  public static void initialize(MycoNode n) {
    n.getHyphaData().setMax(nextPowInt());
  }

  public void initialize(Node n) {
    initialize((MycoNode) n);
  }

  public boolean execute() {
    MycoNode n;
    HyphaData d;

    for (int i = 1; i < Network.size(); i++) {
      n = (MycoNode) Network.get(i);
      initialize(n);
    }

    return false;
  }

}
