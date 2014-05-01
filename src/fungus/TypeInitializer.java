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

public class TypeInitializer implements Control {
  public static final String PAR_NUM_TYPES = "num_types";
  public static final String PAR_UNIFORM_TYPES = "uniform_types";
  // private static final String PAR_CONSOLE_LEVEL = "console_level";
  // private static final String PAR_LOG_LEVEL = "log_level";

  private static LogManager manager = LogManager.getLogManager();
  private static Logger log = Logger.getLogger("fungus");

  private final String name;

  private static int numTypes;
  private static boolean uniformTypes;

  protected static Random generator;


  public TypeInitializer(String name) {
    this.name = name;
    numTypes = Configuration.getInt(name + "." + PAR_NUM_TYPES);
    uniformTypes = Configuration.getBoolean(name + "." + PAR_UNIFORM_TYPES);

    HyphaData.numTypes = numTypes;

    if (generator == null) {
      TypeInitializer.generator = CommonState.r;
    }
  }


  public static int getNumTypes() {
    return numTypes;
  }

  public static int typeSeed;


  public static void initialize(MycoNode n, int type) {
    n.getHyphaData().setType(type);
  }

  public static void initialize(MycoNode n) {
    initialize(n,generator.nextInt(numTypes));
  }

  public boolean execute() {
    MycoNode n;
    typeSeed = generator.nextInt(numTypes);
    for (int i = 0; i < Network.size(); i++) {
      n = (MycoNode) Network.get(i);
      if (uniformTypes) {
        initialize(n, (typeSeed + i) % numTypes);
      } else {
        initialize(n);
      }
    }
    return false;
  }
}
