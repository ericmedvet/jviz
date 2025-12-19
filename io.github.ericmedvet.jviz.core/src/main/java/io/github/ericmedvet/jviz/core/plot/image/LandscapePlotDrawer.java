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
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.plot.LandscapePlot;
import io.github.ericmedvet.jviz.core.plot.LandscapePlot.Data;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.SortedMap;
import java.util.function.DoubleBinaryOperator;

public class LandscapePlotDrawer extends AbstractXYPlotDrawer<LandscapePlot, LandscapePlot.Data> {

  private final Configuration.LandscapePlot c;

  public LandscapePlotDrawer() {
    this(Configuration.DEFAULT);
  }

  public LandscapePlotDrawer(Configuration configuration) {
    super(
        configuration,
        configuration.landscapePlot().xExtensionRate(),
        configuration.landscapePlot().yExtensionRate()
    );
    this.c = configuration.landscapePlot();
  }

  @Override
  public double computeLegendH(Graphics2D g, LandscapePlot p) {
    GMetrics gm = new GMetrics(g);
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    double itemsLegendH = PlotUtils.computeItemsLegendSize(
        g,
        configuration(),
        dataColors,
        configuration().layout().legendInnerMarginWRate() * gm.w(),
        configuration().layout().legendInnerMarginHRate() * gm.h()
    )
        .getY();
    double colorBarLegendH = c.colorBarLegendImageHRate() * gm.h() + PlotUtils.computeStringH(
        g,
        configuration(),
        Configuration.Text.Use.LEGEND_LABEL
    ) + configuration().layout().legendInnerMarginHRate() * gm.h();
    return itemsLegendH + configuration().layout().legendInnerMarginHRate() * gm.h() + colorBarLegendH;
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, LandscapePlot p) {
    GMetrics gm = new GMetrics(g);
    return c.showRanges() ? (c.colorBarLegendImageHRate() * gm.h() + PlotUtils.computeStringH(
        g,
        configuration(),
        Configuration.Text.Use.TICK_LABEL
    ) + configuration().layout().legendInnerMarginHRate() * gm.h()) : 0;
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, LandscapePlot p) {
    // prepare colors
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    GMetrics gm = new GMetrics(g);
    double l = c.markerSizeRate() * gm.refL();
    double itemsLegendH = PlotUtils.computeItemsLegendSize(
        g,
        configuration(),
        dataColors,
        configuration().layout().legendInnerMarginWRate() * gm.w(),
        configuration().layout().legendInnerMarginHRate() * gm.h()
    )
        .getY();
    Point2D legendImageSize = new Point2D.Double(
        c.markerLegendImageSizeRate() * gm.refL(),
        c.markerLegendImageSizeRate() * gm.refL()
    );
    PlotUtils.drawItemsLegend(
        g,
        configuration(),
        r,
        dataColors,
        legendImageSize.getX(),
        legendImageSize.getY(),
        (g1, ir, color) -> PlotUtils.drawMarker(
            g1,
            new Point2D.Double(ir.getCenterX(), ir.getCenterY()),
            l,
            c.marker(),
            color,
            c.alpha(),
            c.dataStrokeSizeRate() * gm.refL()
        )
    );
    r = new Rectangle2D.Double(
        r.getX(),
        r.getY() + configuration().layout().legendInnerMarginHRate() * gm.h() + itemsLegendH,
        r.getWidth(),
        r.getHeight() - configuration().layout().legendInnerMarginHRate() * gm.h() - itemsLegendH
    );
    DoubleRange valueRange = computeValueRange(p, gm);
    PlotUtils.drawColorBar(
        g,
        configuration(),
        gm,
        new Rectangle2D.Double(
            r.getCenterX() - c.colorBarLegendImageWRate() * gm.w() / 2d,
            r.getY(),
            c.colorBarLegendImageWRate() * gm.w(),
            r.getHeight()
        ),
        valueRange,
        valueRange,
        c.colorRange(),
        c.colorBarLegendImageHRate() * gm.h(),
        c.legendSteps(),
        Configuration.Text.Use.LEGEND_LABEL,
        configuration().colors().legendLabelColor(),
        AnchorV.B,
        null
    );
  }

  @Override
  public void drawPlot(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, Axis xA, Axis yA, LandscapePlot p) {
    Grid<DoubleRange> xRanges = computeRanges(true, c.xExtensionRate(), p);
    Grid<DoubleRange> yRanges = computeRanges(false, c.xExtensionRate(), p);
    DoubleRange valueRange = computeValueRange(p, gm);
    SortedMap<String, Color> dataColors = getComputeSeriesDataColors(p);
    // draw function
    double w = r.getWidth() * c.fDensity();
    double h = r.getHeight() * c.fDensity();
    double cellS = 1d / c.fDensity() + 1;
    xRanges.get(k)
        .points((int) w)
        .forEach(x -> yRanges.get(k).points((int) h).forEach(y -> {
          double v = p.dataGrid().get(k).data().f().applyAsDouble(x, y);
          g.setColor(c.colorRange().interpolate(valueRange.normalize(v)));
          g.fill(new Rectangle2D.Double(xA.xIn(x, r), yA.yIn(y, r) - cellS, cellS, cellS));
        }));
    // draw points
    double strokeSize = c.dataStrokeSizeRate() * gm.refL();
    double l = c.markerSizeRate() * gm.refL();
    p.dataGrid().get(k).data().xyDataSeries().forEach(ds -> {
      Color color = dataColors.get(ds.name());
      ds.points()
          .forEach(
              point -> PlotUtils.drawMarker(
                  g,
                  new Point2D.Double(
                      xA.xIn(point.x().v(), r),
                      yA.yIn(point.y().v(), r)
                  ),
                  l,
                  c.marker(),
                  color,
                  c.alpha(),
                  strokeSize
              )
          );
    });
  }

  @Override
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, LandscapePlot p) {
    if (!c.showRanges()) {
      return;
    }
    DoubleRange valueRange = computeValueRange(p, gm);
    Grid<DoubleRange> valueRanges = computeValueRanges(p, gm);
    PlotUtils.drawColorBar(
        g,
        configuration(),
        gm,
        new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()),
        valueRange,
        valueRanges.get(k),
        c.colorRange(),
        c.colorBarLegendImageHRate() * gm.h(),
        c.legendSteps(),
        Configuration.Text.Use.TICK_LABEL,
        configuration().colors().tickLabelColor(),
        AnchorV.T,
        null
    );
  }

  @Override
  protected DoubleRange computeRange(Data data, boolean isXAxis, LandscapePlot p) {
    return data.xyDataSeries()
        .stream()
        .map(d -> isXAxis ? d.xRange() : d.yRange())
        .reduce(DoubleRange::unionWith)
        .orElseThrow();
  }

  private DoubleRange computeValueRange(LandscapePlot p, GMetrics gm) {
    DoubleRange valueRange;
    if (p.valueRange().equals(DoubleRange.UNBOUNDED)) {
      Grid<DoubleRange> valueRanges = computeValueRanges(p, gm);
      valueRange = DoubleRange.union(valueRanges.values().stream().toList());
    } else {
      valueRange = p.valueRange();
    }
    return valueRange;
  }

  private DoubleRange computeValueRange(Grid.Key k, double w, double h, LandscapePlot p) {
    Grid<DoubleRange> xRanges = computeRanges(true, c.xExtensionRate(), p);
    Grid<DoubleRange> yRanges = computeRanges(false, c.xExtensionRate(), p);
    DoubleRange xRange = xRanges.get(k);
    DoubleRange yRange = yRanges.get(k);
    DoubleBinaryOperator f = p.dataGrid().get(k).data().f();
    List<Double> vs = xRange.points((int) w)
        .mapToObj(
            x -> yRange.points((int) h)
                .map(y -> f.applyAsDouble(x, y))
                .boxed()
                .toList()
        )
        .flatMap(List::stream)
        .toList();
    return new DoubleRange(
        vs.stream().min(Double::compareTo).orElse(0d),
        vs.stream().max(Double::compareTo).orElse(1d)
    );
  }

  private Grid<DoubleRange> computeValueRanges(LandscapePlot p, GMetrics gm) {
    return p.dataGrid()
        .map(
            (k, td) -> computeValueRange(
                k,
                gm.w() / p.dataGrid().w() * c.fDensity(),
                gm.h() / p.dataGrid().h() * c.fDensity(),
                p
            )
        );
  }

  private SortedMap<String, Color> getComputeSeriesDataColors(LandscapePlot p) {
    return PlotUtils.computeSeriesDataColors(
        p.dataGrid()
            .values()
            .stream()
            .map(td -> td.data().xyDataSeries())
            .flatMap(List::stream)
            .map(XYDataSeries::name)
            .toList(),
        c.colors()
    );
  }
}
