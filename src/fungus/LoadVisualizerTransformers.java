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
import java.text.DecimalFormat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;

import edu.uci.ics.jung.graph.util.Pair;

import org.apache.commons.collections15.Transformer;

public class LoadVisualizerTransformers extends
                                          ClusteringVisualizerTransformers
{

  // FIXME: Need to replace getMetric() from old mycoload code with something
  // more up-to-date; so, for the moment, this visualization transformer is
  // the same as ClusteringVisualizerTransformer

  final static DecimalFormat df = new DecimalFormat("#.##");

  public Transformer<MycoNode,String> getNodeLabeller() {
    return new Transformer<MycoNode,String>() {
      DecimalFormat df = new DecimalFormat("#.##");
      public String transform(MycoNode n) {
        try {
          //                    return df.format(TypeObserver.getMetric(n));
          return Long.toString(n.getID()) + "(" +
              Long.toString(n.getHyphaData().getMaxCapacity()) +
              "," + n.getHyphaData().getQueueLength() + ") : " +
              df.format(((double) n.getHyphaData().getQueueLength()) /
                        ((double) n.getHyphaData().getMaxCapacity())) + " M " +
              df.format(TypeObserver.getMetric(n));
        } catch (NullPointerException ex) {
          return "?";
        }
      }
    };
  }

  Transformer<MycoNode,Paint> queueColorRenderer =
      new Transformer<MycoNode,Paint>() {
        public Paint transform(MycoNode n) {
          try {
            float m = (float) TypeObserver.getMetric(n);
            if (m > 0) {
              //return new Color(m, m, m);
              //return new Color(m, 0.0f, 0.0f);
              return new Color(1.0f, 1.0f - m, 1.0f - m);
            } else if (m < 0) {
              ///return new Color(-m, -m, -m);
              //return new Color(0.0f, 0.0f, -m);
              return new Color(1.0f + m, 1.0f + m, 1.0f);
            } else {
              //return Color.BLACK;
              return Color.WHITE;
            }
          } catch (NullPointerException ex) {
            return Color.BLACK;
          }
        }
      };

  public Transformer<MycoNode,Paint> getNodeFillRenderer() {
    return queueColorRenderer;
  }

  public Transformer<MycoNode,Paint> getVertexDrawPaintTransformer() {
    return typeColorRenderer;
  }


  final Stroke fatStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                                           BasicStroke.JOIN_MITER);
  final Transformer<MycoNode,Stroke>  queueStrokeRenderer =
      new Transformer<MycoNode,Stroke>() {
        public Stroke transform(MycoNode n) {
          return fatStroke;
        }
      };

  public Transformer<MycoNode,Stroke> getVertexStrokeTransformer() {
    return queueStrokeRenderer;
  }


  // final Transformer<MycoNode,Paint> blackNodeTransformer =
  //     new Transformer<MycoNode,Paint>() {
  //   public Color transform(MycoNode n) {
  //     return Color.BLACK;
  //   }
  // };

}
