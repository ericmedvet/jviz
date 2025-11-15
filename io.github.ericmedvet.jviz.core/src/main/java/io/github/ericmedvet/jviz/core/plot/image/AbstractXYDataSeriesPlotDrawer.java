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
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.SortedMap;

public abstract class AbstractXYDataSeriesPlotDrawer extends AbstractXYPlotDrawer<XYDataSeriesPlot, List<XYDataSeries>> {

  public AbstractXYDataSeriesPlotDrawer(Configuration configuration, double xExtensionRate, double yExtensionRate) {
    super(configuration, xExtensionRate, yExtensionRate);
  }

  protected abstract Point2D computeLegendImageSize(Graphics2D g);

  protected abstract void drawData(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Axis xA,
      Axis yA,
      XYDataSeries ds,
      Color color
  );

  protected abstract void drawLegendImage(Graphics2D g, Rectangle2D r, Color color);

  protected abstract List<Color> colors();

  @Override
  public double computeLegendH(Graphics2D g, XYDataSeriesPlot p) {
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

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, XYDataSeriesPlot p) {
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

  @Override
  public void drawPlot(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, Axis xA, Axis yA, XYDataSeriesPlot p) {
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
    // draw data
    p.dataGrid().get(k).data().forEach(ds -> drawData(g, gm, r, xA, yA, ds, dataColors.get(ds.name())));
  }

  @Override
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, XYDataSeriesPlot p) {
    // do nothing
  }

  @Override
  protected DoubleRange computeRange(List<XYDataSeries> data, boolean isXAxis, XYDataSeriesPlot p) {
    return data.stream()
        .filter(d -> !d.points().isEmpty())
        .map(d -> isXAxis ? d.xRange() : d.yRange())
        .reduce(DoubleRange::largest)
        .orElse(new DoubleRange(0d, 0d));
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, XYDataSeriesPlot xyDataSeriesPlot) {
    return 0;
  }

  private SortedMap<String, Color> getComputeSeriesDataColors(XYDataSeriesPlot p) {
    return PlotUtils.computeSeriesDataColors(
        p.dataGrid()
            .values()
            .stream()
            .map(XYPlot.TitledData::data)
            .flatMap(List::stream)
            .map(XYDataSeries::name)
            .toList(),
        colors()
    );
  }
}
