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
import io.github.ericmedvet.jviz.core.plot.*;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot.Data;
import io.github.ericmedvet.jviz.core.plot.XYPlot.TitledData;
import io.github.ericmedvet.jviz.core.plot.image.*;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.BoxPlot;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.Colors;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.LinesPlot;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.PointsPlot;
import io.github.ericmedvet.jviz.core.plot.video.UnivariatePlotVideoBuilder;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class Main {

  private static DistributionPlot.Data gaussian(double mu, double sigma, int n) {
    RandomGenerator r = new Random();
    return new Data(
        "N(%.1f,%.1f)".formatted(mu, sigma),
        IntStream.range(0, n).mapToObj(i -> r.nextGaussian(mu, sigma)).toList());
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

  public static void main(String[] args) {
    // lines plot
    XYDataSeriesPlot lp = new XYDataSeriesPlot(
        "My plot",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            3,
            2,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                List.of(
                    sinDS(0.2, DoubleRange.SYMMETRIC_UNIT, 100),
                    sinDS(2, DoubleRange.SYMMETRIC_UNIT, 100),
                    sinDS(5, DoubleRange.SYMMETRIC_UNIT, 100)))));
    new LinesPlotDrawer(Configuration.DEFAULT, LinesPlot.DEFAULT, Colors.DEFAULT.dataColors()).show(lp);
    new PointsPlotDrawer(Configuration.DEFAULT, PointsPlot.DEFAULT, Colors.DEFAULT.dataColors()).show(lp);
    // Misc.showImage(new ImagePlotter(ImageBuilder.DEFAULT_W, ImageBuilder.DEFAULT_H).lines(lp));
    // box plot
    DistributionPlot bp = new DistributionPlot(
        "My plot",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.UNBOUNDED,
        Grid.create(
            3,
            2,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                List.of(gaussian(1d, 1d, 100), gaussian(1.5, 2, 100), gaussian(0.5, 0.2, 200)))));
    BoxPlotDrawer bpd = new BoxPlotDrawer(Configuration.DEFAULT, BoxPlot.DEFAULT, Colors.DEFAULT.dataColors());
    bpd.show(bp);
    // landscape plot
    LandscapePlot lsp = new LandscapePlot(
        "My plot",
        "x title",
        "y title",
        "x1",
        "x2",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            4,
            2,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                new LandscapePlot.Data(
                    (x1, x2) -> Math.sin((1 + gX) * x1) * Math.log(1 + gY + Math.abs(x2)),
                    List.of(
                        sinDS(0.2, DoubleRange.SYMMETRIC_UNIT, 100),
                        sinDS(2, DoubleRange.SYMMETRIC_UNIT, 100),
                        sinDS(5, DoubleRange.SYMMETRIC_UNIT, 50))))));
    new LandscapePlotDrawer(Configuration.DEFAULT, Configuration.LandscapePlot.DEFAULT, Colors.DEFAULT.dataColors())
        .show(lsp);
    // grid plot
    UnivariateGridPlot ugp = new UnivariateGridPlot(
        "My plot",
        "x title",
        "y title",
        "x1",
        "x2",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            4,
            2,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                Grid.create(
                    10,
                    10,
                    (igx, igy) -> Math.exp(1 + Math.sin(igx / (1 + gX) + igy / (1 + gY)))))));
    new UnivariateGridPlotDrawer(Configuration.DEFAULT, Configuration.UnivariateGridPlot.DEFAULT).show(ugp);
    new UnivariatePlotVideoBuilder(
            io.github.ericmedvet.jviz.core.plot.video.Configuration.DEFAULT,
            Configuration.DEFAULT,
            Configuration.UnivariateGridPlot.DEFAULT)
        .save(new File("../gv.mp4"), ugp);
  }
}
