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
import io.github.ericmedvet.jviz.core.plot.image.Configuration.LinesPlot;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class LinesPlotDrawer extends AbstractXYDataSeriesPlotDrawer {

  private final Configuration.LinesPlot c;

  public LinesPlotDrawer(Configuration configuration, LinesPlot c, List<Color> colors) {
    super(configuration, c.xExtensionRate(), c.yExtensionRate(), colors);
    this.c = c;
  }

  private static <T> List<T> reverse(List<T> ts) {
    return IntStream.range(0, ts.size())
        .mapToObj(i -> ts.get(ts.size() - 1 - i))
        .toList();
  }

  @Override
  protected Point2D computeLegendImageSize(Graphics2D g) {
    GMetrics gm = new GMetrics(g);
    return new Point2D.Double(c.legendImageWRate() * gm.w(), +c.legendImageHRate() * gm.h());
  }

  @Override
  protected void drawData(Graphics2D g, GMetrics gm, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color) {
    ds = XYDataSeries.of(
        ds.name(),
        ds.points().stream()
            .sorted(Comparator.comparingDouble(p -> p.x().v()))
            .toList());
    if (ds.points().get(0).y() instanceof RangedValue) {
      // draw shaded area
      Path2D sPath = new Path2D.Double();
      sPath.moveTo(
          xA.xIn(ds.points().get(0).x().v(), r),
          yA.yIn(ds.points().get(0).y().v(), r));
      ds.points().stream()
          .skip(1)
          .forEach(p -> sPath.lineTo(
              xA.xIn(p.x().v(), r),
              yA.yIn(RangedValue.range(p.y()).min(), r)));
      reverse(ds.points())
          .forEach(p -> sPath.lineTo(
              xA.xIn(p.x().v(), r),
              yA.yIn(RangedValue.range(p.y()).max(), r)));
      sPath.closePath();
      g.setColor(GraphicsUtils.alphaed(color, c.alpha()));
      g.fill(sPath);
    }
    // draw line
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(
        xA.xIn(ds.points().get(0).x().v(), r),
        yA.yIn(ds.points().get(0).y().v(), r));
    ds.points().stream().skip(1).forEach(p -> path.lineTo(xA.xIn(p.x().v(), r), yA.yIn(p.y().v(), r)));
    g.draw(path);
  }

  @Override
  protected void drawLegendImage(Graphics2D g, Rectangle2D r, Color color) {
    GMetrics gm = new GMetrics(g);
    g.setColor(GraphicsUtils.alphaed(color, c.alpha()));
    g.fill(new Rectangle2D.Double(
        r.getX() + r.getWidth() * 0.1,
        r.getCenterY() - r.getHeight() * 0.25,
        r.getWidth() * 0.8,
        r.getHeight() * 0.5));
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
    g.draw(new Line2D.Double(
        r.getX() + r.getWidth() * 0.1, r.getCenterY(), r.getMaxX() - r.getWidth() * 0.1, r.getCenterY()));
  }
}
