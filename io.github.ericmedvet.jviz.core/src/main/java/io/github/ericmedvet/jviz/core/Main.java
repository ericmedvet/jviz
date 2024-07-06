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
package io.github.ericmedvet.jviz.core;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder.ImageInfo;
import io.github.ericmedvet.jviz.core.plot.LinesPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.Value;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot.TitledData;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.Colors;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.LinesPlot;
import io.github.ericmedvet.jviz.core.plot.image.ImagePlotter;
import io.github.ericmedvet.jviz.core.util.Misc;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    System.out.println("Hello world!");
    XYDataSeriesPlot p = new XYDataSeriesPlot(
        "My plot",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.SYMMETRIC_UNIT,
        DoubleRange.SYMMETRIC_UNIT,
        Grid.create(
            3,
            2,
            (gX, gy) -> new TitledData<>(
                "x title",
                "y title",
                List.of(
                    sinDS(1, DoubleRange.SYMMETRIC_UNIT, 100),
                    sinDS(2, DoubleRange.SYMMETRIC_UNIT, 100),
                    sinDS(3, DoubleRange.SYMMETRIC_UNIT, 100)))));
    LinesPlotDrawer pd = new LinesPlotDrawer(Configuration.DEFAULT, Colors.DEFAULT.dataColors(), LinesPlot.DEFAULT);
    ImageInfo ii = pd.imageInfo(p);
    pd.show(ii, p);
    ImagePlotter ip = new ImagePlotter(ii.w(), ii.h());
    Misc.showImage(ip.lines(p));
  }

  private static XYDataSeries sinDS(double f, DoubleRange xRange, int n) {
    return new XYDataSeries() {
      @Override
      public String name() {
        return "sin(%.1f*x)".formatted(f);
      }

      @Override
      public List<Point> points() {
        return xRange.points(n)
            .mapToObj(x -> new Point(Value.of(x), Value.of(Math.sin(f * x))))
            .toList();
      }
    };
  }
}
