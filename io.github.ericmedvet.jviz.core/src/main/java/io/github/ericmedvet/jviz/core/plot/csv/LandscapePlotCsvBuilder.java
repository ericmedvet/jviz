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
package io.github.ericmedvet.jviz.core.plot.csv;

import io.github.ericmedvet.jviz.core.plot.LandscapePlot;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;

public class LandscapePlotCsvBuilder extends AbstractCsvBuilder<LandscapePlot> {

  private final XYDataSeriesPlotCsvBuilder innerBuilder;

  public LandscapePlotCsvBuilder(Configuration c, Mode mode) {
    super(c, mode);
    this.innerBuilder = new XYDataSeriesPlotCsvBuilder(c, mode);
  }

  @Override
  public String apply(LandscapePlot p) {
    return innerBuilder.apply(p.toXYDataSeriesPlot());
  }
}
