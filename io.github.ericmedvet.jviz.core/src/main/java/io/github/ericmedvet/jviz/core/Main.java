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

package io.github.ericmedvet.jviz.core;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.drawer.Drawer.Arrangement;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot.Data;
import io.github.ericmedvet.jviz.core.plot.HeatPolyMapPlot;
import io.github.ericmedvet.jviz.core.plot.LandscapePlot;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot.Data.ReductionType;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.Value;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries.Point;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldPlot;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot.TitledData;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;
import io.github.ericmedvet.jviz.core.plot.csv.VectorialFieldPlotCsvBuilder;
import io.github.ericmedvet.jviz.core.plot.csv.XYDataSeriesPlotCsvBuilder;
import io.github.ericmedvet.jviz.core.plot.image.BoxPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.HeatPolyMapPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.LandscapePlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.LinesPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.PointsPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.TrajectoryPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.UnivariateGridPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.VectorialFieldPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.video.UnivariateGridPlotVideoBuilder;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

  private static DistributionPlot.Data gaussian(
      double mu,
      double sigma,
      int n,
      double... outliers
  ) {
    RandomGenerator r = new Random();
    return new Data(
        "N(%.1f,%.1f)".formatted(mu, sigma),
        Stream.concat(
            IntStream.range(0, n).mapToObj(i -> r.nextGaussian(mu, sigma)),
            Arrays.stream(outliers).boxed()
        ).toList()
    );
  }

  private static XYDataSeries sinDS(double f, DoubleRange xRange, int n) {
    return XYDataSeries.of(
        "sin(%.1f*x)".formatted(f),
        xRange.points(n)
            .mapToObj(x -> new XYDataSeries.Point(Value.of(x), Value.of(Math.sin(f * x))))
            .toList()
    );
  }

  private static XYDataSeries preciseData() {
    return XYDataSeries.of(
        "five-four-one",
        List.of(
            new XYDataSeries.Point(Value.of(-4), Value.of(-5)),
            new XYDataSeries.Point(Value.of(-3), Value.of(-5)),
            new XYDataSeries.Point(Value.of(-3), Value.of(-4)),
            new XYDataSeries.Point(Value.of(-1), Value.of(-4)),
            new XYDataSeries.Point(Value.of(-1), Value.of(-1)),
            new XYDataSeries.Point(Value.of(1), Value.of(-1)),
            new XYDataSeries.Point(Value.of(1), Value.of(1)),
            new XYDataSeries.Point(Value.of(3), Value.of(1)),
            new XYDataSeries.Point(Value.of(3), Value.of(4))
        )
    );
  }

  public static void main(String[] args) throws IOException {
    polyPlot();
  }

  public static void stacked() {
    Drawer.stacked(
        List.of(
            Drawer.stringWriter(Color.ORANGE, Color.RED, 30, Function.identity()),
            Drawer.stringWriter(Color.GRAY, Color.BLUE, 10, Function.identity()),
            Drawer.stringWriter(Color.PINK, Color.BLACK, 20, Function.identity())
        ),
        Arrangement.VERTICAL
    ).show("cane");
  }

  public static void polyPlot() {
    int nOfIterations = 5;
    int maxSplitSize = 5;
    RandomGenerator rg = RandomGenerator.getDefault();
    List<Rectangle> rectangles = new ArrayList<>();
    rectangles.add(new Rectangle(io.github.ericmedvet.jviz.core.geometry.Point.ORIGIN, 2, 1));
    for (int i = 0; i < nOfIterations; i++) {
      Rectangle r = rectangles.get(rg.nextInt(rectangles.size()));
      rectangles.remove(r);
      if (rg.nextBoolean()) {
        rectangles.addAll(r.splitHorizontally(Math.max(rg.nextInt(maxSplitSize + 1), 2)));
      } else {
        rectangles.addAll(r.splitVertically(Math.max(rg.nextInt(maxSplitSize + 1), 2)));
      }
    }
    HeatPolyMapPlot p = new HeatPolyMapPlot(
        "Heat",
        "x title",
        "y title",
        "x name",
        "y name",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        new DoubleRange(1, 3.4),
        Grid.create(
            3,
            2,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                rectangles.stream()
                    //.filter(r -> rg.nextBoolean())
                    .collect(
                        Collectors.toMap(
                            r -> r,
                            r -> rg.nextDouble(2, 5)
                        )
                    )
            )
        )
    );
    new HeatPolyMapPlotDrawer().show(p);
  }

  public static void onePlot() throws IOException {
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
                    sinDS(5, DoubleRange.SYMMETRIC_UNIT, 100)
                )
            )
        )
    );
    new LinesPlotDrawer().save(new File("../lineplot.svg"), lp);
  }

  private static void manyPlots() throws IOException {
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
                    sinDS(5, DoubleRange.SYMMETRIC_UNIT, 100)
                )
            )
        )
    );
    new LinesPlotDrawer().show(lp);
    new LinesPlotDrawer().multi(Arrangement.HORIZONTAL).show(List.of(lp, lp));
    new LinesPlotDrawer().save(new File("../lineplot.svg"), lp);
    new LinesPlotDrawer().multi(Arrangement.VERTICAL)
        .save(new File("../lineplots.svg"), List.of(lp, lp));
    new PointsPlotDrawer().show(lp);
    new PointsPlotDrawer().save(new File("../points.svg"), lp);
    // Misc.showImage(new ImagePlotter(ImageBuilder.DEFAULT_W, ImageBuilder.DEFAULT_H).lines(lp));
    // box plot with outlier
    DistributionPlot bpo = new DistributionPlot(
        "Boxplot with outliers",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                List.of(gaussian(1d, 1d, 100, 10, -10), gaussian(1.5, 2, 100))
            )
        )
    );
    new BoxPlotDrawer().show(bpo);
    Drawer.paired(new LinesPlotDrawer(), new BoxPlotDrawer(), Arrangement.HORIZONTAL, -1)
        .show(new Pair<>(lp, bpo));
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
                List.of(gaussian(1d, 1d, 100), gaussian(1.5, 2, 100), gaussian(0.5, 0.2, 200))
            )
        )
    );
    BoxPlotDrawer bpd = new BoxPlotDrawer();
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
                        sinDS(5, DoubleRange.SYMMETRIC_UNIT, 50)
                    )
                )
            )
        )
    );
    new LandscapePlotDrawer().show(lsp);
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
                    (igx, igy) -> DoubleRange.SYMMETRIC_UNIT.normalize(
                        (gX + gY) / 6d * Math.sin((double) igx / (1 + gX) + (double) igy / (1 + gY))
                    )
                )
            )
        )
    );
    new UnivariateGridPlotDrawer().show(ugp);
    new UnivariateGridPlotVideoBuilder(
        io.github.ericmedvet.jviz.core.plot.video.Configuration.DEFAULT,
        Configuration.DEFAULT
    )
        .save(new File("../gv.mp4"), ugp);
    // field plot
    VectorialFieldPlot vfp = new VectorialFieldPlot(
        "My plot",
        "x title",
        "y title",
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            3,
            2,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                List.of(
                    VectorialFieldDataSeries.of(
                        "ds1",
                        DoubleRange.UNIT
                            .points(9)
                            .mapToObj(
                                x -> DoubleRange.UNIT
                                    .points(9)
                                    .mapToObj(y -> new Point(x, y))
                            )
                            .flatMap(ps -> ps)
                            .collect(
                                Collectors.toMap(
                                    p -> p,
                                    p -> new Point(
                                        Math.sin(p.x() * gX),
                                        Math.sin(p.y() * gY)
                                    )
                                )
                            )
                    )
                )
            )
        )
    );
    new VectorialFieldPlotDrawer().show(vfp);
    System.out.println(
        new VectorialFieldPlotCsvBuilder(
            io.github.ericmedvet.jviz.core.plot.csv.Configuration.DEFAULTS.get(
                io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode.PAPER_FRIENDLY
            ),
            io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode.PAPER_FRIENDLY
        )
            .apply(vfp)
    );
  }

  private static void pairedPlots() {
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
                    sinDS(5, DoubleRange.SYMMETRIC_UNIT, 100)
                )
            )
        )
    );
    DistributionPlot bpo = new DistributionPlot(
        "Boxplot with outliers",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                List.of(gaussian(1d, 1d, 100, 10, -10), gaussian(1.5, 2, 100))
            )
        )
    );
    Drawer.paired(new LinesPlotDrawer(), new BoxPlotDrawer(), Arrangement.HORIZONTAL, -1)
        .show(new Pair<>(lp, bpo));
    Drawer.paired(new LinesPlotDrawer(), new BoxPlotDrawer(), Arrangement.HORIZONTAL, 3)
        .show(new Pair<>(lp, bpo));
    Drawer.paired(new LinesPlotDrawer(), new BoxPlotDrawer(), Arrangement.VERTICAL, -1)
        .show(new Pair<>(lp, bpo));
    Drawer.paired(new LinesPlotDrawer(), new BoxPlotDrawer(), Arrangement.VERTICAL, 0.5)
        .show(new Pair<>(lp, bpo));
  }

  private static void missingValues() {
    XYDataSeriesPlot lp = new XYDataSeriesPlot(
        "My plot",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            (gX, gY) -> new TitledData<>(
                "gx=%d".formatted(gX),
                "gy=%d".formatted(gY),
                List.of(
                    XYDataSeries.of(
                        "zero",
                        List.of(new XYDataSeries.Point(Value.of(0), Value.of(1)))
                    ),
                    XYDataSeries.of(
                        "one",
                        List.of(new XYDataSeries.Point(Value.of(1), Value.of(1)))
                    )
                )
            )
        )
    );
    System.out.println(
        new XYDataSeriesPlotCsvBuilder(
            io.github.ericmedvet.jviz.core.plot.csv.Configuration.DEFAULTS.get(
                Mode.PAPER_FRIENDLY
            ),
            Mode.PAPER_FRIENDLY
        ).apply(lp)
    );
  }

  private static void trajectory() {
    double noiseS = 0.01;
    RandomGenerator rg = RandomGenerator.getDefault();
    TrajectoryPlot tp = new TrajectoryPlot(
        "My plot",
        "x title",
        "y title",
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            new TitledData<>(
                "",
                "",
                TrajectoryPlot.Data.from(
                    Map.ofEntries(
                        Map.entry(
                            "spiral",
                            new DoubleRange(1, 8).points(1000)
                                .boxed()
                                .collect(
                                    Collectors.toMap(
                                        t -> t,
                                        t -> new double[]{0.1 * t * Math.cos(10 * t) + noiseS * rg
                                            .nextGaussian(), 0.2 * t * Math.sin(10 * t) + noiseS * rg
                                                .nextGaussian(), 0.0001 * t + noiseS * rg.nextGaussian()
                                        },
                                        (p1, p2) -> p2,
                                        TreeMap::new
                                    )
                                )
                        ),
                        Map.entry(
                            "linear",
                            new DoubleRange(2, 11).points(100)
                                .boxed()
                                .collect(
                                    Collectors.toMap(
                                        t -> t,
                                        t -> new double[]{-0.1 + 0.15 * (t - 2) + noiseS * rg
                                            .nextGaussian(), -0.2 + 0.085 * (t - 2) + noiseS * rg
                                                .nextGaussian(), 0.0001 * t + noiseS * rg.nextGaussian()
                                        },
                                        (p1, p2) -> p2,
                                        TreeMap::new
                                    )
                                )
                        )
                    ),
                    ReductionType.PCA
                )
            )
        )
    );
    new TrajectoryPlotDrawer().show(tp);
  }
}