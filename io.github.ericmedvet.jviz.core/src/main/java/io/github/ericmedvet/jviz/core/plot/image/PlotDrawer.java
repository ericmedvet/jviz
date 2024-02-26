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

import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface PlotDrawer {

  double computeLegendH(Graphics2D g);

  double computeNoteH(Graphics2D g, Grid.Key k);

  Grid<Axis> computeXAxes(Graphics2D g, Layout l);

  Grid<Axis> computeYAxes(Graphics2D g, Layout l);

  void drawLegend(Graphics2D g, Rectangle2D r);

  void drawPlot(Graphics2D g, Rectangle2D r, Grid.Key k, Axis xA, Axis yA);

  void drawNote(Graphics2D g, Rectangle2D r, Grid.Key k);
}
