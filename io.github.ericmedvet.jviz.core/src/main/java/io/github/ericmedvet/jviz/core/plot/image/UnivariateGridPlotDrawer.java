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
package io.github.ericmedvet.jviz.core.plot.image;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.plot.RangedGrid;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleFunction;

public class UnivariateGridPlotDrawer extends AbstractXYPlotDrawer<UnivariateGridPlot, Grid<Double>> {

  private final Configuration.UnivariateGridPlot c;

  public UnivariateGridPlotDrawer() {
    this(Configuration.DEFAULT, Configuration.UnivariateGridPlot.DEFAULT);
  }

  public UnivariateGridPlotDrawer(Configuration configuration, Configuration.UnivariateGridPlot c) {
    super(configuration, 1, 1);
    this.c = c;
  }

  private static DoubleRange computeValueRange(UnivariateGridPlot p) {
    DoubleRange valueRange;
    Grid<DoubleRange> valueRanges = p.dataGrid().map(td -> computeValueRange(td.data()));
    if (p.valueRange().equals(DoubleRange.UNBOUNDED)) {
      valueRange = DoubleRange.largest(valueRanges.values().stream().toList());
    } else {
      valueRange = p.valueRange();
    }
    return valueRange;
  }

  private static DoubleRange computeValueRange(Grid<Double> grid) {
    double[] values = grid.values()
        .stream()
        .filter(Objects::nonNull)
        .filter(Double::isFinite)
        .mapToDouble(v -> v)
        .toArray();
    return new DoubleRange(
        Arrays.stream(values).min().orElse(0),
        Arrays.stream(values).max().orElse(1)
    );
  }

  @Override
  protected DoubleRange computeRange(Grid<Double> data, boolean isXAxis, UnivariateGridPlot p) {
    if (data instanceof RangedGrid<Double> rg) {
      return isXAxis ? rg.xRange() : rg.yRange();
    }
    return isXAxis ? new DoubleRange(0, data.w()) : new DoubleRange(0, data.h());
  }

  @Override
  public double computeLegendH(Graphics2D g, UnivariateGridPlot p) {
    GMetrics gm = new GMetrics(g);
    return c.legendImageHRate() * gm.h() + PlotUtils.computeStringH(
        g,
        configuration(),
        Configuration.Text.Use.LEGEND_LABEL
    ) + configuration().layout().legendInnerMarginHRate() * gm.h();
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, UnivariateGridPlot univariateGridPlot) {
    GMetrics gm = new GMetrics(g);
    return c.showRanges() ? (c.legendImageHRate() * gm.h() + PlotUtils.computeStringH(
        g,
        configuration(),
        Configuration.Text.Use.TICK_LABEL
    ) + configuration().layout().legendInnerMarginHRate() * gm.h()) : 0;
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, UnivariateGridPlot p) {
    GMetrics gm = new GMetrics(g);
    DoubleRange valueRange = computeValueRange(p);
    PlotUtils.drawColorBar(
        g,
        configuration(),
        gm,
        new Rectangle2D.Double(
            r.getCenterX() - c.legendImageWRate() * gm.w() / 2d,
            r.getY(),
            c.legendImageWRate() * gm.w(),
            r.getHeight()
        ),
        valueRange,
        valueRange,
        c.colorRange(),
        c.legendImageHRate() * gm.h(),
        c.legendSteps(),
        Configuration.Text.Use.LEGEND_LABEL,
        configuration().colors().legendLabelColor(),
        AnchorV.B,
        null
    );
  }

  @Override
  public void drawPlot(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, Axis xA, Axis yA, UnivariateGridPlot p) {
    Grid<Double> data = p.dataGrid().get(k).data();
    DoubleFunction<Color> colorF = v -> c.colorRange().interpolate(computeValueRange(p).normalize(v));
    double cellW = r.getWidth() / (double) data.w() * c.cellSideRate();
    double cellH = r.getHeight() / (double) data.h() * c.cellSideRate();
    double cellMarginW = r.getWidth() / (double) data.w() * (1 - c.cellSideRate()) / 2d;
    double cellMarginH = r.getHeight() / (double) data.h() * (1 - c.cellSideRate()) / 2d;
    if (data instanceof RangedGrid<Double> rg) {
      data.entries()
          .stream()
          .filter(e -> e.value() != null)
          .filter(e -> Double.isFinite(e.value()))
          .forEach(e -> {
            g.setColor(colorF.apply(e.value()));
            Rectangle2D.Double cellR = new Rectangle2D.Double(
                xA.xIn(rg.xRange(e.key().x()).min(), r) + cellMarginW,
                yA.yIn(rg.yRange(e.key().y()).max(), r) + cellMarginH,
                cellW,
                cellH
            );
            g.fill(cellR);
          });
    } else {
      data.entries()
          .stream()
          .filter(e -> e.value() != null)
          .filter(e -> Double.isFinite(e.value()))
          .forEach(e -> {
            g.setColor(colorF.apply(e.value()));
            Rectangle2D.Double cellR = new Rectangle2D.Double(
                xA.xIn(e.key().x(), r) + cellMarginW,
                yA.yIn(e.key().y() + 1, r) + cellMarginH,
                cellW,
                cellH
            );
            g.fill(cellR);
          });
    }
  }

  @Override
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, UnivariateGridPlot p) {
    if (!c.showRanges()) {
      return;
    }
    DoubleRange valueRange = computeValueRange(p);
    Grid<DoubleRange> valueRanges = p.dataGrid().map(td -> computeValueRange(td.data()));
    List<Double> allLabels = p.dataGrid()
        .values()
        .stream()
        .map(td -> computeValueRange(td.data()))
        .map(dr -> List.of(dr.min(), dr.max()))
        .flatMap(List::stream)
        .toList();
    String rangeLabelFormat = PlotUtils.computeTicksFormat(configuration(), allLabels);
    PlotUtils.drawColorBar(
        g,
        configuration(),
        gm,
        new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()),
        valueRange,
        valueRanges.get(k),
        c.colorRange(),
        c.legendImageHRate() * gm.h(),
        c.legendSteps(),
        Configuration.Text.Use.TICK_LABEL,
        configuration().colors().tickLabelColor(),
        AnchorV.T,
        rangeLabelFormat
    );
  }
}
