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
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Ellipse2D;

import java.lang.Math;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap.SimpleImmutableEntry;

import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;

import org.apache.commons.collections15.Transformer;

public class VisualizerTransformers {

  protected MycoGraph graph;

  private static Logger log =
      Logger.getLogger(VisualizerTransformers.class.getName());

  public static Shape createRegularPolygon(int sides, int radius) {
    Polygon p = new Polygon();
    double a = 2 * Math.PI / sides;
    for(int i = 0; i < sides; i++)
        p.addPoint((int) (radius *  Math.sin(a * i)),
                   (int) (radius * -Math.cos(a * i)));
    return p;
  }


  public Transformer<MycoNode,String> idNodeLabeller =
      new Transformer<MycoNode,String>() {
    public String transform(MycoNode n) {
      return Long.toString(n.getID());
    }
  };

  public Transformer<MycoNode,String> getNodeLabeller() {
    return idNodeLabeller;
  }


  protected static final Shape biomassShape =
      new Ellipse2D.Float(-5.0f,-5.0f,10.0f,10.0f);
  //final Shape hyphaCircle = new Ellipse2D.Float(-5.0f,-5.0f,10.0f,10.0f);
  protected final Shape extendingShape =
      createRegularPolygon(3,10);
  protected final Shape branchingShape =
      createRegularPolygon(4,10);
  protected final Shape immobileShape  =
      createRegularPolygon(5,10);

  Transformer<MycoNode,Shape> fixedShapeTransformer =
      new Transformer<MycoNode,Shape>() {
    public Shape transform(MycoNode n) {
      HyphaData data = n.getHyphaData();
      if (data.isExtending()) {
        return extendingShape;
      } else if (data.isBranching()) {
        return branchingShape;
      } else if (data.isImmobile()) {
        return immobileShape;
      } else {
        return biomassShape;
      }
    }
  };



  private double scale;
  private double smallest;
  private double logMin;
  private boolean rangeScaled = false;

  private double logScale(int cap) {
    // FIXME: Doesn't update if node capacity distribution changes
    if (!rangeScaled) {
      double logMax = Math.log(StateObserver.getMaxCapacity());
      logMin = Math.log(StateObserver.getMinCapacity());
      if (logMax == 0.0) { logMax = 1.0; logMin = 0.0; } // Avoid / 0
      double preRange = logMax - logMin;
      double preScale = preRange / logMax;

      double largest = 15.0;
      smallest = 7.0;

      double rangeScale = largest - smallest;

      scale = preScale * rangeScale;
      rangeScaled = true;
    }

    double logCap = Math.log(cap);

    return ((logCap - logMin) * scale) + smallest;
  }


  public Shape makeShape(int sides, int cap) {
    return createRegularPolygon(sides, (int) logScale(cap));
  }
  public Shape makeBiomassShape(int cap) {
    float sc = (float) logScale(cap);
    //System.out.println((new Float(sc)).toString());
    return (new Ellipse2D.Float(-sc/2, -sc/2, sc, sc));
  }
  public Shape makeExtendingShape(int cap) {
    return makeShape(3, cap);
  }
  public Shape makeBranchingShape(int cap) {
    return makeShape(4, cap);
  }
  public Shape makeImmobileShape(int cap) {
    return makeShape(5, cap);
  }

  private class ShapeKey extends SimpleImmutableEntry<Integer,HyphaType> {
    public ShapeKey(Integer i, HyphaType t) {
      super(i,t);
    }
  };

  public abstract class CachingTransformer<I,O> implements Transformer<I,O> {
    public abstract void clear();
  };

  public final CachingTransformer<MycoNode,Shape> scaledShapeTransformer =
      new CachingTransformer<MycoNode,Shape>() {
    Map<ShapeKey,Shape> shapeMap = new HashMap<ShapeKey,Shape>();

    public void clear() {
      shapeMap = new HashMap<ShapeKey,Shape>();
    }

    public Shape transform(MycoNode n) {
      try {
        HyphaData data = n.getHyphaData();
        int cap = data.getCapacity();
        ShapeKey k = new ShapeKey(new Integer(cap), data.getState());
        if (shapeMap.containsKey(k)) {
          return shapeMap.get(k);
        }

        Shape ret;

        if (data.isExtending()) {
          ret = makeExtendingShape(cap);
        } else if (data.isBranching()) {
          ret = makeBranchingShape(cap);
        } else if (data.isImmobile()) {
          ret = makeImmobileShape(cap);
        } else {
          ret = makeBiomassShape(cap);
        }
        shapeMap.put(k, ret);
        return ret;
      } catch (NullPointerException ex) {
        return biomassShape;
      }
    }
  };



  protected boolean scaleShapes = false;

  public Transformer<MycoNode,Shape> getShapeTransformer(boolean scaleShapes)
  {
    if (scaleShapes) {
      this.scaleShapes = true;
      return scaledShapeTransformer;
    } else {
      return fixedShapeTransformer;
    }
  }

  // Default to not scaled
  public Transformer<MycoNode,Shape> getShapeTransformer() {
    return getShapeTransformer(false);
  }


  public final Transformer<MycoNode,Paint> nodeStatePaintTransformer =
      new Transformer<MycoNode,Paint>() {
    public Paint transform(MycoNode n) {
      HyphaData data = n.getHyphaData();
      if (data.isBiomass()) { return Color.BLUE; }
      else if (data.isExtending()) { return Color.RED; }
      else if (data.isBranching()) { return Color.YELLOW; }
      else { return Color.GREEN; }
    }
  };


  public Transformer<MycoNode,Paint> getNodeFillRenderer() {
    return nodeStatePaintTransformer;
  }


  final Stroke biomassStroke =
      new BasicStroke(0.25f, BasicStroke.CAP_BUTT,
                      BasicStroke.JOIN_MITER, 10.0f);
  final Stroke hyphalStroke =
      new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                      BasicStroke.JOIN_MITER, 10.0f);

  public final Transformer<MycoEdge,Stroke> basicEdgeStrokeTransformer =
      new Transformer<MycoEdge,Stroke>() {
    public Stroke transform(MycoEdge e) {
      Pair<MycoNode> vertices = graph.getEndpoints(e);
      HyphaData firstData = vertices.getFirst().getHyphaData();
      HyphaData secondData = vertices.getSecond().getHyphaData();
      if (firstData.isBiomass()) {
        return biomassStroke;
      } else {
        return hyphalStroke;
      }
    }
  };

  public final Transformer<MycoNode,Paint> blackPaintTransformer =
      new Transformer<MycoNode,Paint>() {
        public Paint transform(MycoNode n) {
          return Color.BLACK;
        }
      };

  public Transformer<MycoNode,Paint> getVertexDrawPaintTransformer() {
    return blackPaintTransformer;
  }

  public final Transformer<MycoNode,Stroke> thinStrokeTransformer =
      new Transformer<MycoNode,Stroke>() {
        Stroke thinStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
                                            BasicStroke.JOIN_MITER, 10.0f);

        public Stroke transform(MycoNode n) {
          return thinStroke;
        }
      };

  public Transformer<MycoNode,Stroke> getVertexStrokeTransformer() {
    return thinStrokeTransformer;
  }

  public Transformer<MycoEdge,Stroke> getEdgeStrokeTransformer() {
    return basicEdgeStrokeTransformer;
  }


  final Color transparent = new Color(0,0,0,0);

  public final Transformer<MycoEdge,Paint> basicEdgePaintTransformer =
      new Transformer<MycoEdge,Paint>() {
    public Paint transform(MycoEdge e) {
      try {
        Pair<MycoNode> vertices = graph.getEndpoints(e);
        HyphaData firstData = vertices.getFirst().getHyphaData();
        HyphaData secondData = vertices.getSecond().getHyphaData();
        if (secondData.isBiomass()) {
          return transparent;
        } else {
          return Color.BLACK;
        }
      } catch (NullPointerException ex) {
        log.fine("Null pointer caught");
        return Color.RED;
      }
    }
  };

  public Transformer<MycoEdge,Paint> getEdgeDrawPaintTransformer() {
    return basicEdgePaintTransformer;
  }


  // Edge length hints for SpringLayout
  public final Transformer<MycoEdge,Integer> basicEdgeLengthTransformer =
      new Transformer<MycoEdge,Integer>() {
    public Integer transform(MycoEdge e) {
      Pair<MycoNode> vertices = graph.getEndpoints(e);
      HyphaData firstData = vertices.getFirst().getHyphaData();
      HyphaData secondData = vertices.getSecond().getHyphaData();
      if (firstData.isBiomass() || secondData.isBiomass()) {
        return 50;
      } else {
        return 100;
      }
    }
  };


  public Transformer<MycoEdge,Integer> getEdgeLengthTransformer() {
    return basicEdgeLengthTransformer;
  }


  public void setGraph(MycoGraph g) {
    graph = g;
  }

  public Layout<MycoNode,MycoEdge> makeLayout(MycoGraph graph) {
    setGraph(graph);
    SpringLayout<MycoNode,MycoEdge> springLayout=
        new SpringLayout<MycoNode,MycoEdge>(graph, getEdgeLengthTransformer());
    springLayout.setSize(new Dimension(650,650));
    springLayout.setRepulsionRange(150);
    springLayout.setForceMultiplier(0.75);
    springLayout.setStretch(0.95);

    return springLayout;
  }

  public boolean execute() {
    rangeScaled = false; // FIXME: Ugly...breaks if sizes change,
                    // lots of unnecessary recalc otherwise
    if (scaledShapeTransformer != null) {
      scaledShapeTransformer.clear(); // FIXME: ICK!
    }

    return false;
  }

}
