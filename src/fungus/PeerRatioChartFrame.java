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

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.util.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.picking.*;
import edu.uci.ics.jung.visualization.renderers.*;
import org.apache.commons.collections15.*;
import org.apache.commons.collections15.functors.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.*;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
//import org.jfree.ui.Spacer;

public class PeerRatioChartFrame extends JFrame implements Control {
  private static final String PAR_SIMULATION_CYCLES = "simulation.cycles";

  private static double simulationCycles;

  private MycoCast mycocast;
  //private XYSeries averageUtilizationData;
  //private XYSeries averageStableUtilizationData;
  private XYSeries bulwarkRatioData;
  private XYSeries biomassRatioData;
  private XYSeries hyphaRatioData;
  //private XYSeries stableHyphaRatioData;
  JLabel chartLabel;

  private Graph<MycoNode,MycoEdge> graph;

  public PeerRatioChartFrame(String prefix) {
    simulationCycles = Configuration.getDouble(PAR_SIMULATION_CYCLES);
    this.setTitle("MycoNet Statistics Chart");
    graph = JungGraphObserver.getGraph();

    //data.add(-1,0);
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.setAutoWidth(false);
    dataset.setIntervalWidth(simulationCycles);

    //averageUtilizationData = new XYSeries("Average Utilization");
    //dataset.addSeries(averageUtilizationData);
    //averageStableUtilizationData = new XYSeries("Avg Stable Util");
    //dataset.addSeries(averageStableUtilizationData);
    bulwarkRatioData = new XYSeries("Bulwark Ratio");
    dataset.addSeries(bulwarkRatioData);
    biomassRatioData = new XYSeries("Biomass Ratio");
    dataset.addSeries(biomassRatioData);
    hyphaRatioData = new XYSeries("Hypha Ratio");
    dataset.addSeries(hyphaRatioData);
    // stableHyphaRatioData = new XYSeries("Stable Hypha Ratio");
    // dataset.addSeries(stableHyphaRatioData);

    //XYSeriesCollection dataset;

    JFreeChart peerRatioChart =
        ChartFactory.createXYLineChart("Bulwark Metrics",
                                       "Cycle",
                                       "Peer State Ratio",
                                       dataset,
                                       PlotOrientation.VERTICAL,
                                       true, false, false);
    ChartPanel peerRatioChartPanel = new ChartPanel(peerRatioChart);

    XYPlot xyplot = peerRatioChart.getXYPlot();

    NumberAxis domainAxis = (NumberAxis) xyplot.getDomainAxis();
    NumberAxis rangeAxis = (NumberAxis) xyplot.getRangeAxis();
    domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setRange(0,1);



    //chart.setBackgroundPaint(Color.white);
    //XYPlot plot = chart.getXYPlot();


    //        BufferedImage chartImage = chart.createBufferedImage(500,300);
    //        chartLabel = new JLabel();
    //chartLabel.setIcon(new ImageIcon(chartImage));


    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
    JPanel labelPane = new JPanel();
    labelPane.setLayout(new GridLayout(1,1));
    //chartPane.setPreferredSize(new java.awt.Dimension(500, 300));
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

    ////contentPane.add(labelPane,BorderLayout.PAGE_START);
    //contentPane.add(Box.createRigidArea(new Dimension(0,5)));
    contentPane.add(peerRatioChartPanel, BorderLayout.CENTER);
    //contentPane.add(Box.createRigidArea(new Dimension(0,5)));
    ////contentPane.add(buttonPane, BorderLayout.PAGE_END);

    //data = node.getHyphaData();
    //link = node.getHyphaLink();
    //mycocast = node.getMycoCast();

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    //chartPanel.add(chartLabel);

    /*JButton updateButton = new JButton("Refresh");
      ActionListener updater = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      refreshData();
      }
      };
      updateButton.addActionListener(updater);

      JButton closeButton = new JButton("Close");
      ActionListener closer = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      closeFrame();
      }
      };
      closeButton.addActionListener(closer);

      buttonPane.add(Box.createHorizontalGlue());
      buttonPane.add(updateButton);
      buttonPane.add(Box.createRigidArea(new Dimension(5,0)));
      buttonPane.add(closeButton);
      refreshData();
    */

    //JungGraphObserver.addChangeListener(this);

    this.pack();
    this.setVisible(true);
  }


  public void closeFrame() {
    //JungGraphObserver.removeChangeListener(this);
    dispose();
  }

  //public void stateChanged(ChangeEvent e) {
  //    refreshData();
  //}


  public boolean execute() {
    //averageUtilizationData.add(CommonState.getTime(),UtilizationObserver.hyphaUtilization);
    //averageStableUtilizationData.add(CommonState.getTime(),UtilizationObserver.stableUtilization);
    bulwarkRatioData.add(CommonState.getTime(),
                         StateObserver.bulwarkRatio);
    biomassRatioData.add(CommonState.getTime(),
                         StateObserver.biomassRatio);
    hyphaRatioData.add(CommonState.getTime(),
                       StateObserver.hyphaRatio);

    //stableHyphaRatioData.add(CommonState.getTime(),UtilizationObserver.stableHyphaRatio);


    //BufferedImage chartImage = chart.createBufferedImage(500,300);
    //chartLabel = new JLabel();        chartLabel.setIcon(new ImageIcon(chartImage));
    this.pack();
    return false;
  }

}
