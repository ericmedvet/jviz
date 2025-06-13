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
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public abstract class AbstractXYPlotDrawer<P extends XYPlot<D>, D> implements XYPlotDrawer<P, D> {

  private static final Logger L = Logger.getLogger(AbstractXYPlotDrawer.class.getName());

  private static final double MIN_EXTENT = 0.00001;

  private final Configuration configuration;
  private final double xExtensionRate;
  private final double yExtensionRate;

  public AbstractXYPlotDrawer(Configuration configuration, double xExtensionRate, double yExtensionRate) {
    this.configuration = configuration;
    this.xExtensionRate = xExtensionRate;
    this.yExtensionRate = yExtensionRate;
  }

  @Override
  public Configuration configuration() {
    return configuration;
  }

  protected Grid<Axis> computeAxes(Graphics2D g, Layout l, boolean isXAxis, P p) {
    return computeRanges(isXAxis, isXAxis ? xExtensionRate : yExtensionRate, p).entries()
        .stream()
        .map(e -> {
          Rectangle2D r = l.innerPlot(e.key().x(), e.key().y());
          double size = isXAxis ? r.getWidth() : r.getHeight();
          return new Grid.Entry<>(
              e.key(),
              computeAxis(g, size, p.dataGrid().get(e.key()).data(), e.value(), isXAxis)
          );
        })
        .collect(Grid.collector());
  }

  protected Axis computeAxis(Graphics2D g, double size, D data, DoubleRange range, boolean isXAxis) {
    DoubleRange innerRange = PlotUtils.enlarge(range, configuration.general().plotDataRatio());
    double labelLineL = PlotUtils.computeStringH(
        g,
        configuration,
        Configuration.Text.Use.TICK_LABEL
    ) * (1d + configuration.general().tickLabelGapRatio());
    int n = (int) Math.round(size / labelLineL);
    List<Double> ticks = DoubleStream.iterate(
        innerRange.min(),
        v -> v <= range.max(),
        v -> v + innerRange.extent() / (double) n
    )
        .boxed()
        .toList();
    String format = PlotUtils.computeTicksFormat(configuration, ticks);
    return new Axis(range, ticks, ticks.stream().map(format::formatted).toList());
  }

  protected abstract DoubleRange computeRange(D data, boolean isXAxis, P p);

  protected Grid<DoubleRange> computeRanges(boolean isXAxis, double extensionRate, P p) {
    Grid<DoubleRange> grid = p.dataGrid()
        .map((k, td) -> {
          DoubleRange extRange = isXAxis ? p.xRange() : p.yRange();
          if (extRange.equals(DoubleRange.UNBOUNDED)) {
            extRange = computeRange(td.data(), isXAxis, p).extend(extensionRate);
          }
          return extRange;
        })
        .map(r -> {
          if (r == null || r.extent() == 0 || r.extent() < MIN_EXTENT) {
            L.fine("Computed axis has ~0 extent: enlarging it to unit extent");
            return Objects.isNull(r) ? DoubleRange.UNIT : DoubleRange.UNIT.delta(r.min() - 0.5);
          }
          return r;
        });
    List<DoubleRange> colLargestRanges = grid.columns().stream().map(DoubleRange::largest).toList();
    List<DoubleRange> rowLargestRanges = grid.rows().stream().map(DoubleRange::largest).toList();
    DoubleRange largestRange = DoubleRange.largest(
        Stream.of(colLargestRanges, rowLargestRanges)
            .flatMap(List::stream)
            .toList()
    );
    return grid.keys()
        .stream()
        .map(
            k -> new Grid.Entry<>(
                k,
                plotRange(
                    isXAxis,
                    grid.get(k),
                    colLargestRanges.get(k.x()),
                    rowLargestRanges.get(k.y()),
                    largestRange
                )
            )
        )
        .collect(Grid.collector());
  }

  protected DoubleRange plotRange(
      boolean isXAxis,
      DoubleRange originalRange,
      DoubleRange colLargestRange,
      DoubleRange rowLargestRange,
      DoubleRange allLargestRange
  ) {
    if (configuration.plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.ALL)) {
      return originalRange;
    }
    if (isXAxis && configuration.plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.COLS)) {
      return colLargestRange;
    }
    if (!isXAxis && configuration.plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.ROWS)) {
      return rowLargestRange;
    }
    return allLargestRange;
  }

  @Override
  public Grid<Axis> computeXAxes(Graphics2D g, Layout l, P p) {
    return computeAxes(g, l, true, p);
  }

  @Override
  public Grid<Axis> computeYAxes(Graphics2D g, Layout l, P p) {
    return computeAxes(g, l, false, p);
  }
}
