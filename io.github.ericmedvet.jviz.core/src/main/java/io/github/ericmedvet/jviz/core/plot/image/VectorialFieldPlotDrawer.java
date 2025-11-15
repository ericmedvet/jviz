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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries.Point;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot.TitledData;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class VectorialFieldPlotDrawer extends AbstractXYPlotDrawer<VectorialFieldPlot, List<VectorialFieldDataSeries>> {

  private final Configuration.VectorialFieldPlot c;

  public VectorialFieldPlotDrawer(Configuration configuration) {
    super(
        configuration,
        configuration.vectorialFieldPlot().xExtensionRate(),
        configuration.vectorialFieldPlot().yExtensionRate()
    );
    this.c = configuration.vectorialFieldPlot();
  }

  public VectorialFieldPlotDrawer() {
    this(Configuration.DEFAULT);
  }

  @Override
  public double computeLegendH(Graphics2D g, VectorialFieldPlot p) {
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    GMetrics gm = new GMetrics(g);
    Point2D legendImageSize = new Point2D.Double(
        c.legendImageSizeRate() * gm.refL(),
        c.legendImageSizeRate() * gm.refL()
    );
    return PlotUtils.computeItemsLegendSize(
        g,
        configuration(),
        dataColors,
        legendImageSize.getX(),
        legendImageSize.getY()
    )
        .getY();
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, VectorialFieldPlot p) {
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    GMetrics gm = new GMetrics(g);
    Point2D legendImageSize = new Point2D.Double(
        c.legendImageSizeRate() * gm.refL(),
        c.legendImageSizeRate() * gm.refL()
    );
    PlotUtils.drawItemsLegend(
        g,
        configuration(),
        r,
        dataColors,
        legendImageSize.getX(),
        legendImageSize.getY(),
        this::drawLegendImage
    );
  }

  @Override
  public void drawPlot(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, Axis xA, Axis yA, VectorialFieldPlot p) {
    g.setColor(configuration().colors().gridColor());
    g.setStroke(new BasicStroke((float) (configuration().general().gridStrokeSizeRate() * gm.refL())));
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
    // compute range of magnitude
    Set<Entry<String, List<VectorialFieldDataSeries>>> dsEntries = p.dataGrid()
        .values()
        .stream()
        .map(TitledData::data)
        .flatMap(List::stream)
        .collect(Collectors.groupingBy(VectorialFieldDataSeries::name))
        .entrySet();
    Map<String, DoubleRange> dstXRanges = dsEntries.stream()
        .collect(
            Collectors.toMap(
                Entry::getKey,
                e -> e.getValue()
                    .stream()
                    .map(VectorialFieldDataSeries::destinationXRange)
                    .reduce(DoubleRange::largest)
                    .orElseThrow()
            )
        );
    Map<String, DoubleRange> dstYRanges = dsEntries.stream()
        .collect(
            Collectors.toMap(
                Entry::getKey,
                e -> e.getValue()
                    .stream()
                    .map(VectorialFieldDataSeries::destinationYRange)
                    .reduce(DoubleRange::largest)
                    .orElseThrow()
            )
        );
    List<Point> srcPoints = p.dataGrid()
        .values()
        .stream()
        .map(TitledData::data)
        .flatMap(List::stream)
        .map(ds -> ds.pointPairs().keySet())
        .flatMap(Set::stream)
        .toList();
    double minD = Math.min(
        minDistance(srcPoints.stream().map(Point::x).distinct().toList()),
        minDistance(srcPoints.stream().map(Point::y).distinct().toList())
    );
    DoubleRange dRange = new DoubleRange(-minD / 2d, minD / 2d);
    // draw data
    p.dataGrid().get(k).data().forEach(ds -> {
      DoubleRange dstXRange = dstXRanges.get(ds.name());
      DoubleRange dstYRange = dstYRanges.get(ds.name());
      ds.pointPairs()
          .forEach(
              (srcP, dstP) -> PlotUtils.drawArrow(
                  g,
                  new Point2D.Double(xA.xIn(srcP.x(), r), yA.yIn(srcP.y(), r)),
                  new Point2D.Double(
                      xA.xIn(srcP.x() + dRange.denormalize(dstXRange.normalize(dstP.x())), r),
                      yA.yIn(srcP.y() + dRange.denormalize(dstYRange.normalize(dstP.y())), r)
                  ),
                  c.srcSizeRate() * gm.refL(),
                  dataColors.get(ds.name()),
                  1d,
                  c.strokeSizeRate() * gm.refL()
              )
          );
    });
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, VectorialFieldPlot vectorialFieldPlot) {
    return 0;
  }

  @Override
  protected DoubleRange computeRange(
      List<VectorialFieldDataSeries> data,
      boolean isXAxis,
      VectorialFieldPlot vectorialFieldPlot
  ) {
    return data.stream()
        .map(d -> isXAxis ? d.originXRange() : d.originYRange())
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }

  private void drawLegendImage(Graphics2D g, Rectangle2D r, Color color) {
    GMetrics gm = new GMetrics(g);
    PlotUtils.drawArrow(
        g,
        new Point2D.Double(r.getMinX(), r.getY() / 2d + r.getMaxY() / 2d),
        new Point2D.Double(r.getMaxX(), r.getY() / 2d + r.getMaxY() / 2d),
        c.srcSizeRate() * gm.refL(),
        color,
        1d,
        c.strokeSizeRate() * gm.refL()
    );
  }

  private SortedMap<String, Color> getComputeSeriesDataColors(VectorialFieldPlot p) {
    return PlotUtils.computeSeriesDataColors(
        p.dataGrid()
            .values()
            .stream()
            .map(XYPlot.TitledData::data)
            .flatMap(List::stream)
            .map(VectorialFieldDataSeries::name)
            .toList(),
        c.colors()
    );
  }

  private double minDistance(List<Double> vs) {
    return vs.stream()
        .mapToDouble(
            v1 -> vs.stream()
                .filter(v2 -> (v2 - v1) != 0)
                .mapToDouble(v2 -> Math.abs(v1 - v2))
                .min()
                .orElseThrow()
        )
        .min()
        .orElseThrow();
  }

  @Override
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, VectorialFieldPlot vectorialFieldPlot) {
    // do none
  }
}
