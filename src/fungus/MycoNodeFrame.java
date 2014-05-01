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

public class MycoNodeFrame extends JFrame implements ChangeListener {
  public MycoNode node;

  private JLabel stateLabel;
  private JLabel maxCapacityLabel;
  private JLabel typeLabel;
  private JLabel queueLengthLabel;
  private JLabel sameLabel;
  private JLabel differentLabel;
  private JLabel idealImmobileLabel;
  private JLabel idealHyphaeLabel;
  private JLabel idealBiomassLabel;
  private JLabel degreeLabel;
  private JLabel hyphaDegreeLabel;
  private JLabel biomassDegreeLabel;
  private JLabel capacityUtilizationLabel;
  private JLabel hyphaUtilizationLabel;
  private JLabel biomassUtilizationLabel;
  private JScrollPane neighborListScroller;
  private JList neighborListControl;
  private JTextArea loggingTextArea;

  private HyphaData data;
  private HyphaLink link;
  private MycoCast mycocast;

  private MycoNodeLogHandler handler;

  private Graph<MycoNode,MycoEdge> graph;

  public MycoNodeFrame(MycoNode node) {
    this.node = node;
    this.setTitle("Node " + node.getID());

    graph = JungGraphObserver.getGraph();

    Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
    JPanel labelPane = new JPanel();
    labelPane.setLayout(new GridLayout(7,2));
    JPanel neighborPane = new JPanel();
    neighborPane.setLayout(new BoxLayout(neighborPane,BoxLayout.PAGE_AXIS));
    JPanel logPane = new JPanel();
    logPane.setLayout(new BoxLayout(logPane,BoxLayout.PAGE_AXIS));
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

    loggingTextArea = new JTextArea("",25,100);
    loggingTextArea.setLineWrap(true);
    loggingTextArea.setEditable(false);
    handler = new MycoNodeLogHandler(node, loggingTextArea);
    handler.addChangeListener(this);
    JScrollPane logScrollPane = new JScrollPane(loggingTextArea);
    logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    logPane.add(logScrollPane);

    contentPane.add(labelPane);
    //contentPane.add(Box.createRigidArea(new Dimension(0,5)));
    contentPane.add(neighborPane);
    //contentPane.add(Box.createRigidArea(new Dimension(0,5)));
    contentPane.add(logPane);
    contentPane.add(buttonPane);

    data = node.getHyphaData();
    link = node.getHyphaLink();
    mycocast = node.getMycoCast();

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    stateLabel = new JLabel();
    typeLabel = new JLabel();
    queueLengthLabel = new JLabel();
    sameLabel = new JLabel();
    differentLabel = new JLabel();
    maxCapacityLabel = new JLabel();
    idealImmobileLabel = new JLabel();
    idealHyphaeLabel = new JLabel();
    idealBiomassLabel = new JLabel();
    degreeLabel = new JLabel();
    hyphaDegreeLabel = new JLabel();
    biomassDegreeLabel = new JLabel();
    hyphaUtilizationLabel = new JLabel();
    biomassUtilizationLabel = new JLabel();
    capacityUtilizationLabel = new JLabel();

    labelPane.add(new JLabel("state"));
    labelPane.add(stateLabel);
    labelPane.add(new JLabel("type"));
    labelPane.add(typeLabel);
    labelPane.add(new JLabel("queue"));
    labelPane.add(queueLengthLabel);
    labelPane.add(new JLabel(""));
    labelPane.add(new JLabel(""));
    labelPane.add(new JLabel("same"));
    labelPane.add(sameLabel);
    labelPane.add(new JLabel("different"));
    labelPane.add(differentLabel);
    //labelPane.add(new JLabel("immobile"));
    //labelPane.add(idealImmobileLabel);
    labelPane.add(new JLabel(""));
    labelPane.add(new JLabel("actual"));
    labelPane.add(new JLabel("ideal"));
    labelPane.add(new JLabel("utilization"));
    labelPane.add(new JLabel("hyphae"));
    labelPane.add(hyphaDegreeLabel);
    labelPane.add(idealHyphaeLabel);
    labelPane.add(hyphaUtilizationLabel);
    labelPane.add(new JLabel("biomass"));
    labelPane.add(biomassDegreeLabel);
    labelPane.add(idealBiomassLabel);
    labelPane.add(biomassUtilizationLabel);
    labelPane.add(new JLabel("capacity"));
    labelPane.add(degreeLabel);
    labelPane.add(maxCapacityLabel);
    labelPane.add(capacityUtilizationLabel);

    neighborListControl = new JList();
    neighborListControl.setLayoutOrientation(JList.VERTICAL_WRAP);
    neighborListControl.setVisibleRowCount(-1);

    neighborListScroller = new JScrollPane(neighborListControl);
    neighborListScroller.setPreferredSize(new Dimension(250, 150));
    neighborListScroller.setMinimumSize(new Dimension(250, 150));

    neighborPane.add(neighborListScroller);

    JButton updateButton = new JButton("Refresh");
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

    JungGraphObserver.addChangeListener(this);

    this.pack();
    this.setVisible(true);
  }

  public void closeFrame() {
    JungGraphObserver.removeChangeListener(this);
    dispose();
  }

  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == handler) {
      getContentPane().validate();
    } else {
      refreshData();
    }
  }

  @SuppressWarnings("unchecked")
  public void refreshData() {
    stateLabel.setText(data.getState().toString());
    typeLabel.setText(new Integer(data.getType()).toString());
    queueLengthLabel.setText(new Integer(data.getQueueLength()).toString());
    maxCapacityLabel.setText(Integer.toString(data.getMaxCapacity()));
    sameLabel.setText(Integer.toString(link.getSameNeighbors().size()));

    int different = link.getDifferentNeighbors().size();
    differentLabel.setText(Integer.toString(different));

    // idealImmobileLabel.setText(Integer.toString(data.getIdealImmobile()));
    idealHyphaeLabel.setText(Integer.toString(data.getIdealHyphae()));
    idealBiomassLabel.setText(Integer.toString(data.getIdealBiomass()));
    degreeLabel.setText(Integer.toString(link.degree()));
    hyphaDegreeLabel.setText(Integer.toString(link.sameHyphaDegree()));
    biomassDegreeLabel.setText(Integer.toString(link.sameBiomassDegree()));

    double capacityUtilization =
        (new Integer(link.degree())).doubleValue() /
        (new Integer(data.getMaxCapacity())).doubleValue();
    capacityUtilizationLabel.setText(Double.toString(capacityUtilization));

    double hyphaUtilization =
        (new Integer(link.sameHyphaDegree())).doubleValue() /
        (new Integer(data.getIdealHyphae())).doubleValue();
    hyphaUtilizationLabel.setText(Double.toString(hyphaUtilization));

    double biomassUtilization =
        (new Integer(link.sameBiomassDegree())).doubleValue() /
        (new Integer(data.getIdealBiomass())).doubleValue();
    biomassUtilizationLabel.setText(Double.toString(biomassUtilization));

    Object[] array = link.getNeighbors().toArray();
    Arrays.sort(array);

    // This generates an unchecked warning. TODO: eliminate suppression
    neighborListControl.setListData(array);
    validate();
  }

}
