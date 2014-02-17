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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;
import java.io.FileOutputStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.graph.Hypergraph;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.cdsim.CDState;

public class JungGraphWriter implements Control {
  private static final String PAR_BASENAME = "config.basename";
  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static int chemicalManagerPid;

  private static Logger log =
      Logger.getLogger(GraphWriter.class.getName());

  private final String name;
  private final String basename;

  private final String dirname = "graphml";
  private String filename;

  private final String seed;

  private File dir;
  private static GraphMLWriter<MycoNode, MycoEdge> writer;

  public Transformer<Hypergraph<MycoNode,MycoEdge>,String>
      experimentTransformer =
      new Transformer<Hypergraph<MycoNode,MycoEdge>,String>() {
    public String transform(Hypergraph<MycoNode,MycoEdge> g) {
      return basename;
    }
  };

  public Transformer<Hypergraph<MycoNode,MycoEdge>,String> seedTransformer
      = new Transformer<Hypergraph<MycoNode,MycoEdge>,String>() {
    public String transform(Hypergraph<MycoNode,MycoEdge> g) {
      return seed;
    }
  };

  public Transformer<Hypergraph<MycoNode,MycoEdge>,String> cycleTransformer
      = new Transformer<Hypergraph<MycoNode,MycoEdge>,String>() {
    public String transform(Hypergraph<MycoNode,MycoEdge> g) {
      return new Integer(CDState.getCycle()).toString();
    }
  };

  public Transformer<MycoNode,String> capacityTransformer =
      new Transformer<MycoNode,String>() {
    public String transform(MycoNode n) {
      return new Integer(n.getHyphaData().getCapacity()).toString();
    }
  };

  public Transformer<MycoNode,String> hormoneTransformer =
      new Transformer<MycoNode,String>() {
    public String transform(MycoNode n) {
      return new Double(((ChemicalManager) n.getProtocol(chemicalManagerPid))
                        .getConcentration(AlertHormone.class)).toString();
    }
  };

  public Transformer<MycoNode,String> stateTransformer =
      new Transformer<MycoNode,String>() {
    public String transform(MycoNode n) {
      return n.getHyphaData().getState().toString();
    }
  };

  public Transformer<MycoNode,String> serviceTransformer =
      new Transformer<MycoNode,String>() {
    public String transform(MycoNode n) {
      return new Integer(n.getHyphaData().getType()).toString();
    }
  };

  public Transformer<MycoNode,String> idTransformer =
      new Transformer<MycoNode,String>() {
    public String transform(MycoNode n) {
      return new Long(n.getID()).toString();
    }
  };

  public JungGraphWriter(String name) {
    this.name = name;
    this.basename = Configuration.getString(PAR_BASENAME);
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);

    this.writer = new GraphMLWriter<MycoNode, MycoEdge>();
    this.seed = new Long(CommonState.seed).toString();

    writer.addGraphData("experiment", "Experiment Name", "experiment",
                        experimentTransformer);
    writer.addGraphData("seed", "Random Seed", "0", seedTransformer);
    writer.addGraphData("cycle", "Cycle Number", "0", cycleTransformer);
    writer.addVertexData("c", "Max Capacity", "1", capacityTransformer);
    writer.addVertexData("h", "Hormone Concentration", "0.0",
                         hormoneTransformer);
    writer.addVertexData("s", "Myconet Protocol State", "BIOMASS",
                         stateTransformer);
    writer.addVertexData("t", "Service Type", "0", serviceTransformer);
    writer.setVertexIDs(idTransformer);
    dir = new File(dirname);
    if (!dir.exists()) {
      dir.mkdir();
    }

  }

  public boolean execute() {
    try {
      StringBuffer fn = new StringBuffer();
      Formatter f = new Formatter(fn, Locale.US);
      f.format("%s/graphml-%s-%d-%03d.xml.gz", dir.getPath(), basename,
               CommonState.seed, CDState.getCycle());
      filename = fn.toString();
      FileOutputStream fos = new FileOutputStream(filename, true);
      GZIPOutputStream gzos = new GZIPOutputStream(fos);

      PrintWriter out =
          new PrintWriter(new
                          BufferedWriter(new OutputStreamWriter(gzos)));



      writer.save(JungGraphObserver.getGraph(), out);
    } catch (IOException e) {
      log.severe("Couldn't open file " + filename + " for writing");
    }
    return false;
  }
}
