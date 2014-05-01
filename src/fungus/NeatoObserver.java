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
import java.util.*;
import java.util.logging.*;

import java.io.*;

public class NeatoObserver implements Control {
  private static final String PAR_HYPHADATA_PROTO =
      "network.node.hyphadata_proto";
  private static final String PAR_HYPHALINK_PROTO =
      "network.node.hyphalink_proto";
  private static final String PAR_MYCOCAST_PROTO =
      "network.node.mycocast_proto";
  private static final String PAR_BASENAME = "basename";

  private static Logger log = Logger.getLogger(NeatoObserver.class.getName());

  private final String name;
  private final int hyphadataPid;
  private final int hyphalinkPid;
  private final int mycocastPid;
  private final String basename;
  private final String dirname = "log";

  public NeatoObserver(String name) {
    this.name = name;
    this.hyphadataPid = Configuration.getPid(PAR_HYPHADATA_PROTO);
    this.hyphalinkPid = Configuration.getPid(PAR_HYPHALINK_PROTO);
    this.mycocastPid = Configuration.getPid(PAR_MYCOCAST_PROTO);
    this.basename = Configuration.getString(PAR_BASENAME);

    boolean success = (new File(dirname)).mkdir();
    if (success) {
      System.out.println("Directory: " + dirname + " created");
    }
  }

  public boolean execute() {
    MycoCast mycocast = (MycoCast) Network.get(0).getProtocol(mycocastPid);

    int bio = mycocast.countBiomass();
    int ext = mycocast.countExtending();
    int bra = mycocast.countBranching();
    int imm = mycocast.countImmobile();

    try {

      String filename = String.format("%s/%s-%03d.dot", dirname,
                                      basename,
                                      CommonState.getTime());
      FileOutputStream fos = new FileOutputStream(filename);
      PrintStream ps = new PrintStream(fos);

      ps.println("graph G {");

      // Add vertices
      for (int i = 0; i < Network.size(); i++) {
        MycoNode n = (MycoNode) Network.get(i);
        HyphaData data = (HyphaData) n.getProtocol(hyphadataPid);
        HyphaLink link = (HyphaLink) n.getProtocol(hyphalinkPid);
        if (data.isBiomass()) { continue; }
        String nodeString = "  " + n.getID() + " [label = " +
            link.sameBiomassDegree() + ", fontcolor=black";
        //n.getID() + ", fontcolor=white"; //link.biomassDegree();
        if (data.isExtending()) nodeString += ", shape = ellipse";
        if (data.isBranching()) nodeString += ", shape = diamond";
        if (data.isImmobile()) nodeString += ", shape = box";
        nodeString += "];";
        ps.println(nodeString);
      }

      // Add edges
      for (int i = 0; i < Network.size(); i++) {
        MycoNode n = (MycoNode) Network.get(i);
        HyphaLink link = (HyphaLink) n.getProtocol(hyphalinkPid);
        HyphaData ndata = (HyphaData) n.getProtocol(hyphadataPid);
        if (ndata.isBiomass()) { continue; }

        MycoList neighbors = link.getHyphae();
        for (MycoNode o : neighbors) {
          HyphaData odata = (HyphaData) o.getProtocol(hyphadataPid);
          if (odata.isBiomass()) { continue; }
          ps.println("  " + n.getID() + " -- " + o.getID() + ";");
        }
      }
      ps.println("}");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return false;
  }
}
