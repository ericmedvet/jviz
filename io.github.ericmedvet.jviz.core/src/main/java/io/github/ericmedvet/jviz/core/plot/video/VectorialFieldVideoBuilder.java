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
package io.github.ericmedvet.jviz.core.plot.video;

import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldPlot;
import io.github.ericmedvet.jviz.core.plot.image.VectorialFieldPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.video.Configuration.SplitType;
import java.util.List;

public class VectorialFieldVideoBuilder extends AbstractXYPlotVideoBuilder<VectorialFieldPlot, List<VectorialFieldDataSeries>> {

  public VectorialFieldVideoBuilder(
      Configuration c,
      io.github.ericmedvet.jviz.core.plot.image.Configuration iConfiguration
  ) {
    super(c, new VectorialFieldPlotDrawer(iConfiguration));
  }

  @Override
  protected List<VectorialFieldPlot> split(VectorialFieldPlot p, SplitType splitType) {
    return split(p.dataGrid(), splitType).stream()
        .map(
            dg -> new VectorialFieldPlot(
                p.title(),
                p.xTitleName(),
                p.yTitleName(),
                p.xName(),
                p.yName(),
                p.xRange(),
                p.yRange(),
                dg
            )
        )
        .toList();
  }
}
