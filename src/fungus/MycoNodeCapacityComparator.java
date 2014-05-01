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

import peersim.cdsim.*;
import peersim.config.*;
import peersim.core.*;
import java.util.*;
import java.util.logging.*;


public class MycoNodeCapacityComparator implements Comparator<MycoNode> {
  public int compare(MycoNode m1, MycoNode m2) {
    /*MycoNode m1 = (MycoNode) o1;
      MycoNode m2 = (MycoNode) o2;*/
    Integer c1 = new Integer(m1.getHyphaData().getMaxCapacity());
    Integer c2 = new Integer(m2.getHyphaData().getMaxCapacity());
    return c1.compareTo(c2);
  }

  public boolean equals(MycoNode o1, MycoNode o2) {
    return compare(o1,o2) == 0;
  }
}
