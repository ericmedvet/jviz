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

import io.github.ericmedvet.jviz.core.plot.RangedValue;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
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

  public LinesPlotDrawer() {
    this(Configuration.DEFAULT);
  }

  public LinesPlotDrawer(Configuration configuration) {
    super(configuration, configuration.linesPlot().xExtensionRate(), configuration.linesPlot().yExtensionRate());
    this.c = configuration.linesPlot();
  }

  private static <T> List<T> reverse(List<T> ts) {
    return IntStream.range(0, ts.size())
        .mapToObj(i -> ts.get(ts.size() - 1 - i))
        .toList();
  }

  @Override
  protected List<Color> colors() {
    return c.colors();
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
        ds.points()
            .stream()
            .sorted(Comparator.comparingDouble(p -> p.x().v()))
            .toList()
    );
    if (ds.points().getFirst().y() instanceof RangedValue) {
      // draw shaded area
      Path2D sPath = new Path2D.Double();
      sPath.moveTo(
          xA.xIn(ds.points().getFirst().x().v(), r),
          yA.yIn(ds.points().getFirst().y().v(), r)
      );
      ds.points()
          .stream()
          .skip(1)
          .forEach(
              p -> sPath.lineTo(
                  xA.xIn(p.x().v(), r),
                  yA.yIn(RangedValue.range(p.y()).min(), r)
              )
          );
      reverse(ds.points())
          .forEach(
              p -> sPath.lineTo(
                  xA.xIn(p.x().v(), r),
                  yA.yIn(RangedValue.range(p.y()).max(), r)
              )
          );
      sPath.closePath();
      g.setColor(GraphicsUtils.alphaed(color, c.alpha()));
      g.fill(sPath);
    }
    // draw line
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(
        xA.xIn(ds.points().getFirst().x().v(), r),
        yA.yIn(ds.points().getFirst().y().v(), r)
    );
    ds.points().stream().skip(1).forEach(p -> path.lineTo(xA.xIn(p.x().v(), r), yA.yIn(p.y().v(), r)));
    g.draw(path);
    if (c.markers()) {
      double l = c.markerSizeRate() * gm.refL();
      double strokeSize = c.strokeSizeRate() * gm.refL();
      ds.points()
          .forEach(
              p -> PlotUtils.drawMarker(
                  g,
                  new Point2D.Double(xA.xIn(p.x().v(), r), yA.yIn(p.y().v(), r)),
                  l,
                  c.marker(),
                  color,
                  c.alpha(),
                  strokeSize
              )
          );
    }
  }

  @Override
  protected void drawLegendImage(Graphics2D g, Rectangle2D r, Color color) {
    GMetrics gm = new GMetrics(g);
    g.setColor(GraphicsUtils.alphaed(color, c.alpha()));
    g.fill(
        new Rectangle2D.Double(
            r.getX() + r.getWidth() * 0.1,
            r.getCenterY() - r.getHeight() * 0.25,
            r.getWidth() * 0.8,
            r.getHeight() * 0.5
        )
    );
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
    g.draw(
        new Line2D.Double(
            r.getX() + r.getWidth() * 0.1,
            r.getCenterY(),
            r.getMaxX() - r.getWidth() * 0.1,
            r.getCenterY()
        )
    );
    if (c.markers()) {
      double l = c.markerSizeRate() * gm.refL();
      PlotUtils.drawMarker(
          g,
          new Point2D.Double(r.getCenterX(), r.getCenterY()),
          l,
          c.marker(),
          color,
          c.alpha(),
          c.strokeSizeRate() * gm.refL()
      );
    }
  }
}
