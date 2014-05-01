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

public class PLTest {
  //private static final String PAR_CONSOLE_LEVEL = "console_level";
  // private static final String PAR_LOG_LEVEL = "log_level";

  //private final Level console_level;
  //private final Level log_level;
  //private final int pid;

  private static cern.jet.random.engine.RandomEngine generator;

  //public PowerLawInitializer(String name) {
  //   this.name = name;
  //   # FIXME: Use peersim random generator?
  //   this.generator =
  //       new cern.jet.random.engine.MersenneTwister(new java.util.Date());
  //}

  //public boolean execute() {
  //  MycoNode n;
  //  for (int i = 1; i < Network.size(); i++) {
  //      n = Network.get(i);
  //      HyphaData d = n.getHyphaData();
  //      d.setMax(Distributions.nextPowLaw()
  //  }
  //  return false;
  //}

  public static void main(String argv[]) {
    generator = new cern.jet.random.engine.MersenneTwister(new java.util.Date());
    double alpha = 1.5;
    double top = 500.0;
    double cut = top;
    for (int i=10000; --i >=0; ) {
      //int pl = (new Double(cern.jet.random.Distributions.nextPowLaw(alpha, cut, generator) * top)).intValue();
      int pl = (new Double(cern.jet.random.Distributions.nextPowLaw(alpha, cut, generator))).intValue();
      System.out.println(500- pl);
    }
  }
}
