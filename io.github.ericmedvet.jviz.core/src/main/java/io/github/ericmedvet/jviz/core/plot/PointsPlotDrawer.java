/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 Eric Medvet
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
package io.github.ericmedvet.jviz.core.plot;

import io.github.ericmedvet.jviz.core.plot.PlotUtils.GMetrics;
import io.github.ericmedvet.jviz.core.plot.image.Axis;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.PointsPlot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class PointsPlotDrawer extends AbstractXYDataSeriesPlotDrawer {

  private final Configuration.PointsPlot c;

  public PointsPlotDrawer(Configuration configuration, PointsPlot c, List<Color> colors) {
    super(configuration, c.xExtensionRate(), c.yExtensionRate(), colors);
    this.c = c;
  }

  @Override
  protected Point2D computeLegendImageSize(Graphics2D g) {
    GMetrics gm = new GMetrics(g);
    return new Point2D.Double(c.legendImageSizeRate() * gm.refL(), c.legendImageSizeRate() * gm.refL());
  }

  @Override
  protected void drawData(Graphics2D g, GMetrics gm, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color) {
    double l = c.markerSizeRate() * gm.refL();
    double strokeSize = c.strokeSizeRate() * gm.refL();
    ds.points()
        .forEach(p -> PlotUtils.drawMarker(
            g,
            new Point2D.Double(xA.xIn(p.x().v(), r), yA.yIn(p.y().v(), r)),
            l,
            c.marker(),
            color,
            c.alpha(),
            strokeSize));
  }

  @Override
  protected void drawLegendImage(Graphics2D g, Rectangle2D r, Color color) {
    GMetrics gm = new GMetrics(g);
    double l = c.markerSizeRate() * gm.refL();
    PlotUtils.drawMarker(
        g,
        new Point2D.Double(r.getCenterX(), r.getCenterY()),
        l,
        c.marker(),
        color,
        c.alpha(),
        c.strokeSizeRate() * gm.refL());
  }
}
