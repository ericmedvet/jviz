/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 - 2025 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jviz.core.plot.image;

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public interface XYPlotDrawer<P extends XYPlot<D>, D> extends Drawer<P> {

  int DEFAULT_AXIS_W = 300;
  int DEFAULT_AXIS_H = 300;

  Configuration configuration();

  enum AnchorH {
    L, C, R
  }

  @Override
  default ImageInfo imageInfo(P p) {
    int axisW = ImageBuilder.DEFAULT_W / p.dataGrid().w();
    int axisH = ImageBuilder.DEFAULT_H / p.dataGrid().h();
    if (axisW < DEFAULT_AXIS_W || axisH < DEFAULT_AXIS_H) {
      axisW = DEFAULT_AXIS_W;
      axisH = DEFAULT_AXIS_H;
    }
    return new ImageInfo(axisW * p.dataGrid().w(), axisH * p.dataGrid().h());
  }

  double computeLegendH(Graphics2D g, P p);

  double computeNoteH(Graphics2D g, Grid.Key k, P p);

  Grid<Axis> computeXAxes(Graphics2D g, Layout l, P p);

  Grid<Axis> computeYAxes(Graphics2D g, Layout l, P p);

  void drawLegend(Graphics2D g, Rectangle2D r, P p);

  void drawPlot(Graphics2D g, GMetrics gm, Rectangle2D r, Grid.Key k, Axis xA, Axis yA, P p);

  void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Grid.Key k, P p);

  enum AnchorV {
    T, C, B
  }

  enum Marker {
    CIRCLE, PLUS, SQUARE, TIMES
  }

  @Override
  default void draw(Graphics2D g, P p) {
    GMetrics gm = new GMetrics(g);
    // compute layout and axes
    Layout l = PlotUtils.computeLayout(g, configuration(), p, this);
    Grid<Axis> xAxesGrid = computeXAxes(g, l, p);
    Grid<Axis> yAxesGrid = computeYAxes(g, l, p);
    Shape clip = g.getClip();
    // clean
    Drawer.clean(g);
    // draw title
    PlotUtils.markRectangle(g, configuration(), l.mainTitle());
    PlotUtils.drawString(
        g,
        configuration(),
        PlotUtils.center(l.mainTitle()),
        p.title(),
        AnchorH.C,
        AnchorV.C,
        Configuration.Text.Use.TITLE,
        Configuration.Text.Direction.H,
        configuration().colors().titleColor()
    );
    // draw legend
    PlotUtils.markRectangle(g, configuration(), l.legend());
    drawLegend(g, l.legend(), p);
    g.setClip(clip);
    g.setStroke(new BasicStroke());
    // draw plots
    for (int px = 0; px < p.dataGrid().w(); px = px + 1) {
      for (int py = 0; py < p.dataGrid().h(); py = py + 1) {
        if (px == 0 && configuration().plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common y-axis
          PlotUtils.markRectangle(g, configuration(), l.commonYAxis(py));
          PlotUtils.drawYAxis(g, configuration(), l.commonYAxis(py), p.yName(), yAxesGrid.get(0, py));
        }
        if (px == p.dataGrid().w() - 1 && configuration().plotMatrix()
            .titlesShow()
            .equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common row title
          PlotUtils.markRectangle(g, configuration(), l.commonRowTitle(py));
          PlotUtils.drawString(
              g,
              configuration(),
              PlotUtils.center(l.commonRowTitle(py)),
              p.dataGrid().get(px, py).yTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.V,
              configuration().colors().titleColor()
          );
        }
        if (py == p.dataGrid().h() - 1 && configuration().plotMatrix()
            .axesShow()
            .equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common x-axis
          PlotUtils.markRectangle(g, configuration(), l.commonXAxis(px));
          PlotUtils.drawXAxis(g, configuration(), l.commonXAxis(px), p.xName(), xAxesGrid.get(px, 0));
        }
        if (py == 0 && configuration().plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common col title
          PlotUtils.markRectangle(g, configuration(), l.commonColTitle(px));
          PlotUtils.drawString(
              g,
              configuration(),
              PlotUtils.center(l.commonColTitle(px)),
              p.dataGrid().get(px, py).xTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.H,
              configuration().colors().titleColor()
          );
        }
        // draw plot titles
        if (configuration().plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.ALL)) {
          PlotUtils.markRectangle(g, configuration(), l.colTitle(px, py));
          PlotUtils.drawString(
              g,
              configuration(),
              PlotUtils.center(l.colTitle(px, py)),
              p.dataGrid().get(px, py).xTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.H,
              configuration().colors().titleColor()
          );
          PlotUtils.markRectangle(g, configuration(), l.rowTitle(px, py));
          PlotUtils.drawString(
              g,
              configuration(),
              PlotUtils.center(l.rowTitle(px, py)),
              p.dataGrid().get(px, py).yTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.V,
              configuration().colors().titleColor()
          );
        }
        // draw axes
        if (configuration().plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.ALL)) {
          PlotUtils.markRectangle(g, configuration(), l.xAxis(px, py));
          PlotUtils.drawXAxis(g, configuration(), l.xAxis(px, py), p.xName(), xAxesGrid.get(px, py));
          PlotUtils.markRectangle(g, configuration(), l.yAxis(px, py));
          PlotUtils.drawYAxis(g, configuration(), l.yAxis(px, py), p.yName(), yAxesGrid.get(px, py));
        }
        // draw notes
        PlotUtils.markRectangle(g, configuration(), l.note(px, py));
        drawNote(g, gm, l.note(px, py), new Grid.Key(px, py), p);
        g.setClip(clip);
        g.setStroke(new BasicStroke());
        // draw background
        g.setColor(configuration().colors().plotBgColor());
        g.fill(l.innerPlot(px, py));
        // draw border
        g.setStroke(new BasicStroke((float) (configuration().general().borderStrokeSizeRate() * gm.refL())));
        g.setColor(configuration().colors().plotBorderColor());
        g.draw(l.innerPlot(px, py));
        // draw plot
        PlotUtils.markRectangle(g, configuration(), l.innerPlot(px, py));
        g.setClip(l.innerPlot(px, py));
        drawPlot(
            g,
            gm,
            l.innerPlot(px, py),
            new Grid.Key(px, py),
            xAxesGrid.get(px, py),
            yAxesGrid.get(px, py),
            p
        );
        g.setClip(clip);
        g.setStroke(new BasicStroke());
      }
    }
  }
}
