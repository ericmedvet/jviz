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
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import io.github.ericmedvet.jviz.core.plot.HeatPolyMapPlot;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleFunction;

public class HeatPolyMapPlotDrawer extends AbstractXYPlotDrawer<HeatPolyMapPlot, Map<Polygon, Double>> {

  private final Configuration.HeatPolyMapPlot c;

  public HeatPolyMapPlotDrawer() {
    this(Configuration.DEFAULT);
  }

  public HeatPolyMapPlotDrawer(Configuration configuration) {
    super(configuration, 1, 1);
    this.c = configuration.heatPolyMapPlot();
  }

  private static DoubleRange computeValueRange(HeatPolyMapPlot p) {
    DoubleRange valueRange;
    if (p.valueRange().equals(DoubleRange.UNBOUNDED)) {
      Grid<DoubleRange> valueRanges = p.dataGrid().map(td -> computeValueRange(td.data()));
      valueRange = DoubleRange.union(valueRanges.values().stream().toList());
    } else {
      valueRange = p.valueRange();
    }
    return valueRange;
  }

  private static DoubleRange computeValueRange(Map<Polygon, Double> map) {
    double[] values = map.values()
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

  private static Shape toShape(Rectangle2D r, Axis xA, Axis yA, Polygon p, double erodeL) {
    Path2D path = new Path2D.Double();
    Polygon rP = Polygon.of(
        p.vertexes()
            .stream()
            .map(point -> new Point(xA.xIn(point.x(), r), yA.yIn(point.y(), r)))
            .toList()
    );
    Point center = rP.center();
    rP = Polygon.of(
        rP.vertexes()
            .stream()
            .map(point -> moveTo(point, center, erodeL))
            .toList()
    );
    path.moveTo(rP.vertexes().getFirst().x(), rP.vertexes().getFirst().y());
    rP.vertexes()
        .stream()
        .skip(1)
        .forEach(point -> path.lineTo(point.x(), point.y()));
    path.lineTo(rP.vertexes().getFirst().x(), rP.vertexes().getFirst().y());
    return path;
  }

  private static Point moveTo(Point src, Point dst, double l) {
    return src.sum(new Point(dst.diff(src).direction()).scale(l));
  }

  @Override
  public double computeLegendH(Graphics2D g, HeatPolyMapPlot p) {
    GMetrics gm = new GMetrics(g);
    return c.legendImageHRate() * gm.h() + PlotUtils.computeStringH(
        g,
        configuration(),
        Configuration.Text.Use.LEGEND_LABEL
    ) + configuration().layout().legendInnerMarginHRate() * gm.h();
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, HeatPolyMapPlot p) {
    GMetrics gm = new GMetrics(g);
    return c.showRanges() ? (c.legendImageHRate() * gm.h() + PlotUtils.computeStringH(
        g,
        configuration(),
        Configuration.Text.Use.TICK_LABEL
    ) + configuration().layout().legendInnerMarginHRate() * gm.h()) : 0;
  }

  @Override
  protected DoubleRange computeRange(
      Map<Polygon, Double> data,
      boolean isXAxis,
      HeatPolyMapPlot p
  ) {
    Optional<Rectangle> oBoundingBox = data.keySet()
        .stream()
        .map(Polygon::boundingBox)
        .reduce(
            (r1, r2) -> Rectangle.of(
                new Point(
                    Math.min(r1.min().x(), r2.min().x()),
                    Math.min(r1.min().y(), r2.min().y())
                ),
                new Point(
                    Math.max(r1.max().x(), r2.max().x()),
                    Math.max(r1.max().y(), r2.max().y())
                )
            )
        );
    if (oBoundingBox.isEmpty()) {
      return DoubleRange.SYMMETRIC_UNIT;
    }
    Rectangle bb = oBoundingBox.get();
    return isXAxis ? new DoubleRange(bb.min().x(), bb.max().x()) : new DoubleRange(bb.min().y(), bb.max().y());
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, HeatPolyMapPlot p) {
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
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, HeatPolyMapPlot p) {
    if (!c.showRanges()) {
      return;
    }
    DoubleRange globalValueRange = computeValueRange(p);
    DoubleRange localValueRange = computeValueRange(p.dataGrid().get(k).data());
    PlotUtils.drawColorBar(
        g,
        configuration(),
        gm,
        new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()),
        globalValueRange,
        localValueRange,
        c.colorRange(),
        c.legendImageHRate() * gm.h(),
        c.legendSteps(),
        Configuration.Text.Use.TICK_LABEL,
        configuration().colors().tickLabelColor(),
        AnchorV.T,
        null
    );
  }

  @Override
  public void drawPlot(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Key k,
      Axis xA,
      Axis yA,
      HeatPolyMapPlot p
  ) {
    Map<Polygon, Double> data = p.dataGrid().get(k).data();
    DoubleFunction<Color> colorF = v -> c.colorRange()
        .interpolate(computeValueRange(p).normalize(v));
    data.forEach((poly, value) -> {
      Shape shape = toShape(r, xA, yA, poly, c.erodeRate() * gm.refL());
      if (Objects.nonNull(value) && Double.isFinite(value)) {
        g.setColor(colorF.apply(value));
        g.fill(shape);
      }
      if (c.strokeSizeRate() > 0) {
        g.setColor(c.polyBorderColor());
        g.setStroke(new BasicStroke((float) (c.strokeSizeRate() * gm.refL())));
        g.draw(shape);
      }
    });
  }
}