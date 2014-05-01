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

import peersim.core.Node;
import peersim.core.GeneralNode;
import peersim.config.Configuration;

public class MycoNode extends GeneralNode implements Node, Comparable {

  private static final String PAR_MYCOCAST_PROTO = "mycocast_proto";
  private static final String PAR_HYPHADATA_PROTO = "hyphadata_proto";
  private static final String PAR_HYPHALINK_PROTO = "hyphalink_proto";
  private static final String PAR_FUNGALGROWTH_PROTO = "fungalgrowth_proto";
  //private static final String PAR_FAILUREALERTER_PROTO =
  //      "failurealerter_proto";
  //private static final String PAR_CHEMICALMANAGER_PROTO =
  //    "chemicalmanager_proto";

  private static int mycoCastPid;
  private static int hyphaDataPid;
  private static int hyphaLinkPid;
  private static int fungalGrowthPid;
  //private static int failureAlerterPid;
  //private static int chemicalManagerPid;

  public MycoNode(String prefix) {
    super(prefix);
    mycoCastPid = Configuration.getPid(prefix + "." + PAR_MYCOCAST_PROTO);
    hyphaDataPid = Configuration.getPid(prefix + "." + PAR_HYPHADATA_PROTO);
    hyphaLinkPid = Configuration.getPid(prefix + "." + PAR_HYPHALINK_PROTO);
    fungalGrowthPid = Configuration.getPid(prefix + "."
                                           + PAR_FUNGALGROWTH_PROTO);
    //failureAlerterPid = Configuration.getPid(prefix + "."
    //                                         + PAR_FAILUREALERTER_PROTO);
    //chemicalManagerPid =
    //    Configuration.getPid(prefix + "." + PAR_CHEMICALMANAGER_PROTO);
  }

  public MycoCast getMycoCast() {
    return (MycoCast) this.getProtocol(mycoCastPid);
  }

  public HyphaData getHyphaData() {
    return (HyphaData) this.getProtocol(hyphaDataPid);
  }

  public HyphaLink getHyphaLink() {
    return (HyphaLink) this.getProtocol(hyphaLinkPid);
  }

  public FungalGrowth getFungalGrowth() {
    return (FungalGrowth) this.getProtocol(fungalGrowthPid);
  }

  // public FailureAlerter getFailureAlerter() {
  //   return (FailureAlerter) this.getProtocol(failureAlerterPid);
  // }

  // public ChemicalManager getChemicalManager() {
  //   return (ChemicalManager) this.getProtocol(chemicalManagerPid);
  // }

  // Conveniently access HyphaData info

  public boolean isBiomass() {
    return getHyphaData().isBiomass();
  }

  public boolean isHypha() {
    return !isBiomass();
  }

  public boolean isExtending() {
    return getHyphaData().isExtending();
  }

  public boolean isBranching() {
    return getHyphaData().isBranching();
  }

  public boolean isImmobile() {
    return getHyphaData().isImmobile();
  }

  public boolean isBulwark() {
    return getHyphaData().isBulwark();
  }

  public int getMaxCapacity() {
    return getHyphaData().getMax();
  }

  public int getIdealHyphae() {
    return getHyphaData().getIdealHyphae();
  }

  // Conveniently access HyphaLink info

  public boolean contains(MycoNode neighbor) {
    return getHyphaLink().contains(neighbor);
  }

  public boolean isDisconnected(MycoNode neighbor) {
    return getHyphaLink().isDisconnected();
  }

  public String toString() {
    return new String(getID() + " (type-" + getHyphaData().getType() + "; "
                      + getHyphaData().getState() + "; "
                      + getHyphaLink().degree() + "/"
                      + getHyphaData().getMaxCapacity() + ")");
  }

  public int compareTo(Object o) {
    if (!(o instanceof MycoNode)) {
      throw new ClassCastException();
    }
    MycoNode other = (MycoNode) o;
    HyphaData od = other.getHyphaData();
    HyphaData md = getHyphaData();
    int comp = md.getState().compareTo(od.getState());
    if (comp != 0) {
      return comp;
    }

    if (getID() < other.getID()) {
      return -1;
    } else if (getID() > other.getID()) {
      return 1;
    } else {
      return 0;
    }
  }
}
