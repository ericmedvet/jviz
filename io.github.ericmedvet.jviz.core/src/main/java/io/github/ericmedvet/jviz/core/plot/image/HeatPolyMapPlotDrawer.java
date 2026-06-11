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
/*
 * Copyright 2026 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jviz.core.plot.image;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import io.github.ericmedvet.jviz.core.plot.HeatPolyMapPlot;
import io.github.ericmedvet.jviz.core.plot.HeatPolyMapPlot.ValuedPoint;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;

public class HeatPolyMapPlotDrawer extends AbstractXYPlotDrawer<HeatPolyMapPlot, Map<Polygon, ValuedPoint>> {

  private final Configuration.HeatPolyMapPlot c;

  public HeatPolyMapPlotDrawer(Configuration configuration) {
    super(configuration, 1, 1);
    this.c = configuration.heatPolyMapPlot();
  }

  @Override
  protected DoubleRange computeRange(
      Map<Polygon, ValuedPoint> data,
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
                    Math.min(r1.max().x(), r2.max().x()),
                    Math.min(r1.max().y(), r2.max().y())
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
  public double computeLegendH(Graphics2D g, HeatPolyMapPlot p) {
    // TODO fill
    return 0;
  }

  @Override
  public double computeNoteH(Graphics2D g, Key k, HeatPolyMapPlot p) {
    // TODO fill
    return 0;
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r, HeatPolyMapPlot p) {
    // TODO fill
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
    // TODO fill
  }

  @Override
  public void drawNote(Graphics2D g, GMetrics gm, Rectangle2D r, Key k, HeatPolyMapPlot p) {
    // TODO fill
  }
}
