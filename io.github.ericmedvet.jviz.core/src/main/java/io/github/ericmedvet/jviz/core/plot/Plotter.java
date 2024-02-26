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
package io.github.ericmedvet.jviz.core.plot;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public interface Plotter<O> {

  enum Type {
    LINES,
    UNIVARIATE_GRID,
    POINTS,
    LANDSCAPE,
    BOXPLOT
  }

  O boxplot(DistributionPlot plot);

  O landscape(LandscapePlot plot);

  O lines(XYDataSeriesPlot plot);

  O points(XYDataSeriesPlot plot);

  O univariateGrid(UnivariateGridPlot plot);

  default O plot(XYPlot<?> plot, Type type) {
    if (plot.dataGrid().values().isEmpty()) {
      throw new IllegalArgumentException("Empty data in plot (0x0 grid)!");
    }
    if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot && type.equals(Type.LINES)) {
      return lines(xyDataSeriesPlot);
    }
    if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot && type.equals(Type.POINTS)) {
      return points(xyDataSeriesPlot);
    }
    if (plot instanceof UnivariateGridPlot univariateGridPlot && type.equals(Type.UNIVARIATE_GRID)) {
      return univariateGrid(univariateGridPlot);
    }
    if (plot instanceof LandscapePlot landscapePlot && type.equals(Type.LANDSCAPE)) {
      return landscape(landscapePlot);
    }
    if (plot instanceof DistributionPlot distributionPlot && type.equals(Type.BOXPLOT)) {
      return boxplot(distributionPlot);
    }
    throw new UnsupportedOperationException("Unknown plot type %s with data %s"
        .formatted(type, plot.getClass().getSimpleName()));
  }
}
