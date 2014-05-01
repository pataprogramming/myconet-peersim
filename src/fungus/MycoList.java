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
import com.google.common.base.Predicate;

import peersim.core.*;

public class MycoList extends ArrayList<MycoNode> {

  protected static Random generator;

  public MycoList() {
    super();
  }
  public MycoList(int initialCapacity) {
    super(initialCapacity);
  }
  public MycoList(Iterable<MycoNode> nodes) {
    super();
    for (MycoNode n : nodes) {
      this.add(n);
    }
  }

  public MycoNode getRandom() {
    if (this.size() == 0) {
      return null;
    }
    return get(CommonState.r.nextInt(this.size()));
  }

  public MycoNode getRandomOfType(int t) {
    MycoList ts = getType(t);
    if (ts.isEmpty()) {
      return null;
    }
    return ts.get(CommonState.r.nextInt(ts.size()));
  }

  public MycoList duplicate() {
    return new MycoList(this);
  }

  public List<MycoList> partition() {
    List<MycoList> ret = new ArrayList<MycoList>();
    for (int t = 0; t < HyphaData.numTypes; t++) {
      ret.add(new MycoList());
    }
    for (MycoNode n : this) {
      ret.get(n.getHyphaData().getType()).add(n);
    }
    return ret;
  }

  private static List<Predicate<MycoNode>> typePredicates = new ArrayList<Predicate<MycoNode>>();
  private static List<Predicate<MycoNode>> typeNotPredicates = new ArrayList<Predicate<MycoNode>>();

  public static Predicate<MycoNode> getTypePredicate(final int t) {
    while (typePredicates.size() <= t) {
      typePredicates.add(new Predicate<MycoNode>() {
          private int type = typePredicates.size();
          public boolean apply(MycoNode n) {
            return n.getHyphaData().getType() == type;
          }
        });
    }
    return typePredicates.get(t);
  }

  public static Predicate<MycoNode> getTypeNotPredicate(final int t) {
    while (typeNotPredicates.size() <= t) {
      typeNotPredicates.add(new Predicate<MycoNode>() {
          private int type = typeNotPredicates.size();
          public boolean apply(MycoNode n) {
            return n.getHyphaData().getType() != type;
          }
        });
    }
    return typeNotPredicates.get(t);
  }


  public static Predicate<MycoNode> hyphaPredicate = new Predicate<MycoNode>() {
      public boolean apply(MycoNode n) {
        return n.getHyphaData().isHypha();
      }
    };
  public static Predicate<MycoNode> biomassPredicate = new Predicate<MycoNode>() {
      public boolean apply(MycoNode n) {
        return n.getHyphaData().isBiomass();
      }
    };
  public static Predicate<MycoNode> branchingPredicate = new Predicate<MycoNode>() {
      public boolean apply(MycoNode n) {
        return n.getHyphaData().isBranching();
      }
    };
  public static Predicate<MycoNode> bulwarkPredicate =
      new Predicate<MycoNode>() {
        public boolean apply(MycoNode n) {
          return n.getHyphaData().isBulwark();
        }
      };
  public static Predicate<MycoNode> extendingPredicate =
      new Predicate<MycoNode>() {
        public boolean apply(MycoNode n) {
          return n.getHyphaData().isExtending();
        }
      };
  public static Predicate<MycoNode> immobilePredicate = new Predicate<MycoNode>() {
      public boolean apply(MycoNode n) {
        return n.getHyphaData().isImmobile();
      }
    };
  public static Predicate<MycoNode> stablePredicate = new Predicate<MycoNode>() {
      public boolean apply(MycoNode n) {
        return immobilePredicate.apply(n) || branchingPredicate.apply(n);
      }
    };
  public static Predicate<MycoNode> deadPredicate = new Predicate<MycoNode>() {
      public boolean apply(MycoNode n) {
        return n.getHyphaData().isDead();
      }
    };

  public MycoList getBiomass() {
    return new MycoList(Iterables.filter(this, biomassPredicate));
  }

  public MycoList getHyphae() {
    return new MycoList(Iterables.filter(this, hyphaPredicate));
  }
  public MycoList getBulwark() {
    return new MycoList(Iterables.filter(this, bulwarkPredicate));
  }
  public MycoList getBranching() {
    return new MycoList(Iterables.filter(this, branchingPredicate));
  }

  public MycoList getExtending() {
    return new MycoList(Iterables.filter(this, extendingPredicate));
  }

  public MycoList getImmobile() {
    return new MycoList(Iterables.filter(this, immobilePredicate));
  }

  public MycoList getStable() {
    return new MycoList(Iterables.filter(this, stablePredicate));
  }

  public MycoList getDead() {
    return new MycoList(Iterables.filter(this, deadPredicate));
  }

  public MycoList getType(int t) {
    return new MycoList(Iterables.filter(this, getTypePredicate(t)));
  }

  public MycoList getTypeNot(int t) {
    return new MycoList(Iterables.filter(this, getTypeNotPredicate(t)));
  }

}
