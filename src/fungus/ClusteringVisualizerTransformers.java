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

import edu.uci.ics.jung.graph.util.Pair;

import org.apache.commons.collections15.Transformer;

public class ClusteringVisualizerTransformers extends VisualizerTransformers {

  private static Logger log =
      Logger.getLogger(ClusteringVisualizerTransformers.class.getName());

  public abstract class CachingTransformer<I,O> implements Transformer<I,O> {
    public abstract void clear();
  };

  CachingTransformer<MycoNode,Shape> shapeTransformer;

  private int numTypes = 1;
  protected java.util.List <Color> typeColors;

  float[] dash = { 10.0f };


  public final Transformer<MycoEdge,Integer> clusteringEdgeLengthTransformer =
    new Transformer<MycoEdge,Integer>() {
    public Integer transform(MycoEdge e) {
      Pair<MycoNode> vertices = graph.getEndpoints(e);
      HyphaData firstData = vertices.getFirst().getHyphaData();
      HyphaData secondData = vertices.getSecond().getHyphaData();
      if (firstData.getType() == secondData.getType()) {
        if (firstData.isBiomass() || secondData.isBiomass()) {
          return 50;
        } else {
          return 75;
        }
      } else {
        //return 75;
        return 150;
      }
    }
  };


  public Transformer<MycoEdge,Integer> getEdgeLengthTransformer() {
    return clusteringEdgeLengthTransformer;
  }


  public final Transformer<MycoEdge,Paint> clusteringEdgeDrawPaintTransformer =
      new Transformer<MycoEdge,Paint>() {
    public Paint transform(MycoEdge e) {
      try {
        Pair<MycoNode> vertices = graph.getEndpoints(e);
        HyphaData firstData = vertices.getFirst().getHyphaData();
        HyphaData secondData = vertices.getSecond().getHyphaData();
        if (secondData.isBiomass()) {
          return transparent;
        } else {
          if (firstData.getType() == secondData.getType()) {
            return Color.BLACK;
          } else {
            return Color.BLUE;
          }
        }
      } catch (NullPointerException ex) {
        log.fine("Null pointer caught");
        return Color.RED;
      }
    }
  };

  public Transformer<MycoEdge,Paint> getEdgeDrawPaintTransformer() {
    return clusteringEdgeDrawPaintTransformer;
  }

  public final Transformer<MycoNode,Paint> typeColorRenderer =
      new Transformer<MycoNode,Paint>() {
    public Paint transform(MycoNode n) {
      HyphaData data = n.getHyphaData();
      if (!n.isUp()) {
        return Color.BLACK;
      } else {
        return typeColors.get(data.getType());
      }
    }
  };

  public Transformer<MycoNode,Paint> getNodeFillRenderer() {
    return typeColorRenderer;
  }


  final Stroke sameBiomassStroke = biomassStroke;
  final Stroke sameHyphalStroke = hyphalStroke;
  final Stroke differentBiomassStroke =
      new BasicStroke(0.25f, BasicStroke.CAP_BUTT,
                      BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
  final Stroke differentHyphalStroke =
      new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
                      BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
//final Stroke noStroke = new BasicStroke(0.0f, BasicStroke.CAP_BUTT,
  //                                        BasicStroke.JOIN_MITER, 1.0f);


  public final Transformer<MycoEdge,Stroke> clusteringEdgeStrokeTransformer =
      new Transformer<MycoEdge,Stroke>() {
    public Stroke transform(MycoEdge e) {
      Pair<MycoNode> vertices = graph.getEndpoints(e);
      HyphaData firstData = vertices.getFirst().getHyphaData();
      HyphaData secondData = vertices.getSecond().getHyphaData();
      if (firstData.getType() == secondData.getType()) {
        if (firstData.isBiomass()) {
          return sameBiomassStroke;
        } else {
          return sameHyphalStroke;
        }
      } else {
        if (firstData.isBiomass()) {
          return sameBiomassStroke;
        } else {
          return sameHyphalStroke;
        }
      }
    }
  };

  public Transformer<MycoEdge,Stroke> getEdgeStrokeTransformer() {
    return clusteringEdgeStrokeTransformer;
  }


  public boolean execute() {
    // FIXME: Hacky workaround slow initialization
    if (HyphaData.numTypes != numTypes) {
      numTypes = HyphaData.numTypes;
      typeColors = ColorWheel.getColorMap(numTypes, 1.0f, 1.0f);
    }
    return false;
  }

  public ClusteringVisualizerTransformers() {
    // FIXME: Work around slow initialization
    typeColors = ColorWheel.getColorMap(HyphaData.numTypes, 1.0f, 1.0f);
  }
}
