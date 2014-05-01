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

import java.util.logging.Logger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;

import java.text.DecimalFormat;

import edu.uci.ics.jung.graph.util.Pair;

import org.apache.commons.collections15.Transformer;

import peersim.config.Configuration;

public class HITAPVisualizerTransformers extends VisualizerTransformers {
  private static final String PAR_FAILUREALERTER_PROTO =
      "network.node.failurealerter_proto";
  private static final String PAR_CHEMICALMANAGER_PROTO =
      "network.node.chemicalmanager_proto";

  private static Logger log =
      Logger.getLogger(HITAPVisualizerTransformers.class.getName());

  private static int failureAlerterPid;
  private static int chemicalManagerPid;

  protected final Shape bulwarkShape = createRegularPolygon(8,10);

  final static DecimalFormat df = new DecimalFormat("#.##");


  public Transformer<MycoNode,String> alertNodeLabeller =
      new Transformer<MycoNode,String>() {
        public String transform(MycoNode n) {
          return Long.toString(n.getID()) + "(" +
          Long.toString(n.getHyphaData().getMaxCapacity()) + "," +
          df.format(((ChemicalManager) n.getProtocol(chemicalManagerPid))
                    .getConcentration(AlertHormone.class)) + ")";
        }
      };


  public Transformer<MycoNode,String> getNodeLabeller() {
    return alertNodeLabeller;
  }


  public Transformer<MycoNode,Shape> hitapShapeTransformer =
      new Transformer<MycoNode,Shape>() {
        public Shape transform(MycoNode n) {
          HyphaData data = n.getHyphaData();
          if (data.isExtending()) {
            return extendingShape;
          } else if (data.isBranching()) {
            return branchingShape;
          } else if (data.isImmobile()) {
            return immobileShape;
          } else if (data.isBulwark()) {
            return bulwarkShape;
          } else {
            return biomassShape;
          }
        }
      };

  public Transformer<MycoNode,Shape> getShapeTransformer(boolean scaledShapes)
  {
    // FIXME: Currently does NOT scale sizes (ignores scale setting)
    return hitapShapeTransformer;
  }

  public Transformer<MycoNode,Paint> concentrationPaintTransformer =
      new Transformer<MycoNode,Paint>() {
        // FIXME: Hard-coded config strings
        float concMax = (float)
            Configuration.getDouble("config.alert.max_concentration");
        float threshold = (float)
            Configuration.getDouble("config.thresholdbulwark.threshold");

        float upperRange = concMax - threshold;
        float lowerRange = threshold;

        public Paint transform(MycoNode n) {
          HyphaData data = n.getHyphaData();
          ChemicalManager cm =
              (ChemicalManager) n.getProtocol(chemicalManagerPid);
          FailureAlerter fa = (FailureAlerter) n.getProtocol(failureAlerterPid);

          float conc = (float) cm.getConcentration(AlertHormone.class);
          if (conc > concMax) conc = concMax;

          if (!n.isUp()) {
            return Color.BLACK;
          } else if (conc >= threshold) {
            float val = (conc - threshold) / upperRange;
            return new Color(1.0f, 0.0f, val);
          } else {
            float val = conc / lowerRange;
            return new Color(val, 1.0f, 0.0f);
          }
        }
      };


  public Transformer<MycoNode,Paint> getNodeFillRenderer() {
    return concentrationPaintTransformer;
  }

  public HITAPVisualizerTransformers() {
    super();
    failureAlerterPid = Configuration.getPid(PAR_FAILUREALERTER_PROTO);
    chemicalManagerPid = Configuration.getPid(PAR_CHEMICALMANAGER_PROTO);
  }
}
