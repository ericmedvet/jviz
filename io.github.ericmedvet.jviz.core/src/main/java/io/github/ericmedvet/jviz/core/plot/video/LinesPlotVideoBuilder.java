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
package io.github.ericmedvet.jviz.core.plot.video;

import io.github.ericmedvet.jviz.core.plot.image.Configuration.LinesPlot;
import io.github.ericmedvet.jviz.core.plot.image.LinesPlotDrawer;
import java.awt.Color;
import java.util.List;

public class LinesPlotVideoBuilder extends AbstractXYDataSeriesPlotVideoBuilder {

  public LinesPlotVideoBuilder(
      Configuration c,
      io.github.ericmedvet.jviz.core.plot.image.Configuration iConfiguration,
      LinesPlot lpConfiguration,
      List<Color> colors) {
    super(c, new LinesPlotDrawer(iConfiguration, lpConfiguration, colors));
  }
}
