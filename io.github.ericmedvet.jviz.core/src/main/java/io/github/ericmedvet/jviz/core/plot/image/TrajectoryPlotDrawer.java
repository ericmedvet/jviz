/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 - 2026 Eric Medvet
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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot.Data;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot.Data.ReducedPoint;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class TrajectoryPlotDrawer extends AbstractXYPlotDrawer<TrajectoryPlot, List<Data>> {

  private final Configuration.TrajectoryPlot c;

  public TrajectoryPlotDrawer() {
    this(Configuration.DEFAULT);
  }

  public TrajectoryPlotDrawer(Configuration configuration) {
    super(
        configuration,
        configuration.trajectoryPlot().xExtensionRate(),
        configuration.trajectoryPlot().yExtensionRate()
    );
    this.c = configuration.trajectoryPlot();
  }

  @Override
  public double computeLegendH(Graphics2D g, TrajectoryPlot p) {
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    Point2D legendImageSize = computeLegendImageSize(g);
    return PlotUtils.computeItemsLegendSize(
        g,
        configuration(),
        dataColors,
        legendImageSize.getX(),
        legendImageSize.getY()
    )
        .getY();
  }

  private Point2D computeLegendImageSize(Graphics2D g) {
    GMetrics gm = new GMetrics(g);
    return new Point2D.Double(
        c.legendImageXRate() * gm.refL(),
        c.legendImageYRate() * gm.refL()
    );
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, TrajectoryPlot trajectoryPlot) {
    return 0;
  }

  @Override
  protected DoubleRange computeRange(
      List<Data> data,
      boolean isXAxis,
      TrajectoryPlot trajectoryPlot
  ) {
    return data.stream()
        .filter(d -> !d.points().isEmpty())
        .map(
            d -> d.points()
                .values()
                .stream()
                .map(p -> new DoubleRange(isXAxis ? p.x() : p.y(), isXAxis ? p.x() : p.y()))
                .reduce(DoubleRange::unionWith)
                .orElseThrow()
        )
        .reduce(DoubleRange::unionWith)
        .orElse(new DoubleRange(0d, 0d));
  }

  private void drawData(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Axis xA,
      Axis yA,
      Data d,
      Color color
  ) {
    // draw path
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(
        xA.xIn(d.points().firstEntry().getValue().x(), r),
        yA.yIn(d.points().firstEntry().getValue().y(), r)
    );
    d.points()
        .values()
        .stream()
        .skip(1)
        .forEach(
            p -> path.lineTo(
                xA.xIn(p.x(), r),
                yA.yIn(p.y(), r)
            )
        );
    g.draw(path);
    // draw markers
    double l = c.markerSizeRate() * gm.refL();
    double strokeSize = c.strokeSizeRate() * gm.refL();
    PlotUtils.drawMarker(
        g,
        new Point2D.Double(
            xA.xIn(d.points().firstEntry().getValue().x(), r),
            yA.yIn(d.points().firstEntry().getValue().y(), r)
        ),
        l,
        c.startMarker(),
        color,
        1,
        strokeSize
    );
    PlotUtils.drawMarker(
        g,
        new Point2D.Double(
            xA.xIn(d.points().lastEntry().getValue().x(), r),
            yA.yIn(d.points().lastEntry().getValue().y(), r)
        ),
        l,
        c.endMarker(),
        color,
        1,
        strokeSize
    );
    if (c.nOfMidPoints() > 0) {
      double[] midTs = new DoubleRange(
          d.points().firstEntry().getKey(),
          d.points().lastEntry().getKey()
      ).points(c.nOfMidPoints() + 1).toArray();
      int lastIndex = 1;
      for (Map.Entry<Double, ReducedPoint> entry : d.points().entrySet()) {
        if (entry.getKey() >= midTs[lastIndex]) {
          lastIndex = lastIndex + 1;
          PlotUtils.drawMarker(
              g,
              new Point2D.Double(
                  xA.xIn(entry.getValue().x(), r),
                  yA.yIn(entry.getValue().y(), r)
              ),
              l,
              c.midMarker(),
              color,
              1,
              strokeSize
          );
        }
        if (lastIndex > midTs.length - 1) {
          break;
        }
      }
    }
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, TrajectoryPlot p) {
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    PlotUtils.drawItemsLegend(
        g,
        configuration(),
        r,
        dataColors,
        computeLegendImageSize(g).getX(),
        computeLegendImageSize(g).getY(),
        this::drawLegendImage
    );
  }

  protected void drawLegendImage(Graphics2D g, Rectangle2D r, Color color) {
    GMetrics gm = new GMetrics(g);
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
    double l = c.markerSizeRate() * gm.refL();
    g.draw(
        new Line2D.Double(
            r.getX() + r.getWidth() * 0.1 + l / 2d,
            r.getCenterY(),
            r.getMaxX() - r.getWidth() * 0.1 - l / 2d,
            r.getCenterY()
        )
    );
    PlotUtils.drawMarker(
        g,
        new Point2D.Double(r.getMinX() + r.getWidth() * 0.1 + l / 2d, r.getCenterY()),
        l,
        c.startMarker(),
        color,
        1,
        c.strokeSizeRate() * gm.refL()
    );
    if (c.nOfMidPoints() > 0) {
      PlotUtils.drawMarker(
          g,
          new Point2D.Double(r.getCenterX(), r.getCenterY()),
          l,
          c.midMarker(),
          color,
          1,
          c.strokeSizeRate() * gm.refL()
      );
    }
    PlotUtils.drawMarker(
        g,
        new Point2D.Double(r.getMaxX() - r.getWidth() * 0.1 - l / 2d, r.getCenterY()),
        l,
        c.endMarker(),
        color,
        1,
        c.strokeSizeRate() * gm.refL()
    );
  }

  @Override
  public void drawNote(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Key k,
      TrajectoryPlot trajectoryPlot
  ) {
    // do nothing
  }

  @Override
  public void drawPlot(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Key k,
      Axis xA,
      Axis yA,
      TrajectoryPlot p
  ) {
    g.setColor(configuration().colors().gridColor());
    g.setStroke(
        new BasicStroke((float) (configuration().general().gridStrokeSizeRate() * gm.refL()))
    );
    xA.ticks()
        .forEach(
            x -> g.draw(
                new Line2D.Double(
                    xA.xIn(x, r),
                    yA.yIn(yA.range().min(), r),
                    xA.xIn(x, r),
                    yA.yIn(yA.range().max(), r)
                )
            )
        );
    yA.ticks()
        .forEach(
            y -> g.draw(
                new Line2D.Double(
                    xA.xIn(xA.range().min(), r),
                    yA.yIn(y, r),
                    xA.xIn(xA.range().max(), r),
                    yA.yIn(y, r)
                )
            )
        );
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    // draw data
    p.dataGrid()
        .get(k)
        .data()
        .forEach(ds -> drawData(g, gm, r, xA, yA, ds, dataColors.get(ds.name())));

  }

  private SortedMap<String, Color> getComputeSeriesDataColors(TrajectoryPlot p) {
    return PlotUtils.computeSeriesDataColors(
        p.dataGrid()
            .values()
            .stream()
            .map(XYPlot.TitledData::data)
            .flatMap(List::stream)
            .map(Data::name)
            .toList(),
        c.colors()
    );
  }
}
