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
import com.google.common.collect.Iterables;

import peersim.core.*;

public class JobQueue extends PriorityQueue<Job> {
  public int getTotalWorkUnits() {
    int ret = 0;
    for (Job j : this) {
      ret += j.remaining;
    }
    return ret;
  }

  // // remove n elements, spaced out through
  // public JobQueue extract(int n) {
  // JobQueue ret = new JobQueue();
  //
  // LinkedList<Job> temp = new LinkedList<Job>();
  // Iterables.addAll(temp, this);
  // this.clear();
  //
  // // Select n random items from the list of jobs
  // Collections.shuffle(temp, CommonState.r);
  // while ((! temp.isEmpty()) && (ret.size() < n)) {
  // ret.add(temp.pop());
  // }
  //
  // this.addAll(temp);
  //
  // return ret;
  // }

  // remove n elements, spaced out through
  public JobQueue extract(int n) {
    JobQueue ret = new JobQueue();

    LinkedList<Job> temp = new LinkedList<Job>();
    Iterables.addAll(temp, this);
    this.clear();

    for (int i = 0; i < temp.size(); i++) {
      Job j = temp.get(i);

      if ((i < (n * 2.0)) && (i % 2 == 0))
          ret.add(j);
      else
          add(j);
    }

    return ret;
  }
}
