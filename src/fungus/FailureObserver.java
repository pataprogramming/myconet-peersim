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

import cern.jet.stat.Descriptive;
import cern.colt.list.DoubleArrayList;

public class FailureObserver implements Control {
  //private static final String PAR_PROTO = "protocol";

  private static Logger log =
      Logger.getLogger(FailureObserver.class.getName());

  private final String name;
  //private final int pid;

  public static int failures = 0;
  public static int disconnections = 0;

  public FailureObserver(String name) {
    this.name = name;
    //this.pid = Configuration.getPid(name + "." + PAR_PROTO);
  }

  public boolean execute() {
    MycoGraph g = JungGraphObserver.getGraph();

    return false;
  }
}
