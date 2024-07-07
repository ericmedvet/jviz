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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot.Data;
import io.github.ericmedvet.jviz.core.plot.PlotUtils.GMetrics;
import io.github.ericmedvet.jviz.core.plot.image.Axis;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.BoxPlot;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.IntStream;

public class BoxPlotDrawer extends AbstractXYPlotDrawer<DistributionPlot, List<Data>> {

  private final Configuration.BoxPlot c;
  private final List<Color> colors;

  public BoxPlotDrawer(Configuration configuration, BoxPlot c, List<Color> colors) {
    super(configuration, 1, c.yExtensionRate());
    this.c = c;
    this.colors = colors;
  }

  @Override
  protected DoubleRange computeRange(List<Data> data, boolean isXAxis, DistributionPlot p) {
    if (isXAxis) {
      return p.xRange();
    }
    return data.stream()
        .map(DistributionPlot.Data::range)
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }

  @Override
  public double computeLegendH(Graphics2D g, DistributionPlot p) {
    GMetrics gm = new GMetrics(g);
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    return PlotUtils.computeItemsLegendSize(
            g, configuration(), dataColors, c.legendImageWRate() * gm.w(), c.legendImageHRate() * gm.h())
        .getY();
  }

  private SortedMap<String, Color> getComputeSeriesDataColors(DistributionPlot p) {
    return PlotUtils.computeSeriesDataColors(
        p.dataGrid().values().stream()
            .map(XYPlot.TitledData::data)
            .flatMap(List::stream)
            .map(Data::name)
            .toList(),
        colors);
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, DistributionPlot p) {
    return 0;
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, DistributionPlot p) {
    GMetrics gm = new GMetrics(g);
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    PlotUtils.drawItemsLegend(
        g,
        configuration(),
        r,
        dataColors,
        c.legendImageWRate() * gm.w(),
        c.legendImageHRate() * gm.h(),
        (gg, ir, color) -> PlotUtils.drawBoxAndWhiskers(
            gg,
            configuration(),
            gm,
            new Rectangle2D.Double(
                ir.getX() + ir.getWidth() * 0.2, ir.getY(), ir.getWidth() * 0.6, ir.getHeight()),
            color,
            ir.getY() + ir.getHeight() * 0.2,
            ir.getCenterY(),
            ir.getMaxY() - ir.getHeight() * 0.2,
            c.alpha(),
            c.whiskersWRate(),
            c.strokeSizeRate()));
  }

  @Override
  public void drawPlot(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, Axis xA, Axis yA, DistributionPlot p) {
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    g.setColor(configuration().colors().gridColor());
    g.setStroke(new BasicStroke((float) (configuration().general().gridStrokeSizeRate() * gm.refL())));
    xA.ticks()
        .forEach(x -> g.draw(new Line2D.Double(
            xA.xIn(x, r), yA.yIn(yA.range().min(), r),
            xA.xIn(x, r), yA.yIn(yA.range().max(), r))));
    yA.ticks()
        .forEach(y -> g.draw(new Line2D.Double(
            xA.xIn(xA.range().min(), r), yA.yIn(y, r),
            xA.xIn(xA.range().max(), r), yA.yIn(y, r))));
    // draw data
    List<String> names = dataColors.keySet().stream().toList();
    double w = r.getWidth() / ((double) names.size()) * c.boxWRate();
    IntStream.range(0, names.size())
        .filter(i -> p.dataGrid().get(k).data().stream()
            .map(DistributionPlot.Data::name)
            .anyMatch(n -> names.get(i).equals(n)))
        .forEach(x -> p.dataGrid().get(k).data().stream()
            .filter(d -> d.name().equals(names.get(x)))
            .findFirst()
            .ifPresent(d -> {
              double topY = yA.yIn(
                  switch (c.extremeType()) {
                    case MIN_MAX -> d.stats().min();
                    case IQR_1_5 -> d.stats().q1minus15IQR();
                  },
                  r);
              double bottomY = yA.yIn(
                  switch (c.extremeType()) {
                    case MIN_MAX -> d.stats().max();
                    case IQR_1_5 -> d.stats().q3plus15IQR();
                  },
                  r);
              double innerTopY = yA.yIn(d.stats().q1(), r);
              double innerBottomY = yA.yIn(d.stats().q3(), r);
              double centerY = yA.yIn(
                  switch (c.midType()) {
                    case MEAN -> d.stats().mean();
                    case MEDIAN -> d.stats().median();
                  },
                  r);
              Rectangle2D bR = new Rectangle2D.Double(xA.xIn(x, r) - w / 2d, bottomY, w, topY - bottomY);
              PlotUtils.drawBoxAndWhiskers(
                  g,
                  configuration(),
                  gm,
                  bR,
                  dataColors.get(names.get(x)),
                  innerBottomY,
                  centerY,
                  innerTopY,
                  c.alpha(),
                  c.boxWRate(),
                  c.strokeSizeRate());
            }));
  }

  @Override
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, DistributionPlot p) {
    // do nothing
  }
}
