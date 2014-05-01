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
import java.util.logging.*;
import cern.jet.random.engine.*;
import cern.jet.random.Distributions.*;

public class UniformInitializer implements Control {
  //private static final String PAR_CONSOLE_LEVEL = "console_level";
  // private static final String PAR_LOG_LEVEL = "log_level";
  private static final String PAR_MAX = "max";

  private static LogManager manager = LogManager.getLogManager();
  private static Logger log = Logger.getLogger("fungus");

  private final String name;
  private final double max;
  private int intmax;
  //private final Level console_level;
  //private final Level log_level;
  //private final int pid;

  private cern.jet.random.engine.RandomEngine generator;
  private cern.jet.random.Uniform distribution;

  public UniformInitializer(String name) {
    this.name = name;
    max = Configuration.getDouble(name + "." + PAR_MAX);
    intmax = (new Double(max)).intValue();
    this.generator =
        new cern.jet.random.engine.MersenneTwister(new java.util.Date());
    this.distribution = new cern.jet.random.Uniform(1.0,max,generator);
    //            new cern.jet.random.engine.MersenneTwister(new java.util.Date());
  }

  public int nextInt() {
    return distribution.nextInt();
  }

  public boolean execute() {
    MycoNode n;
    HyphaData d;
    for (int i = 1; i < Network.size(); i++) {
      n = (MycoNode) Network.get(i);
      d = n.getHyphaData();
      d.setMax(nextInt());
    }
    return false;
  }


}
