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

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder.ImageInfo;
import io.github.ericmedvet.jviz.core.drawer.Video;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.video.Configuration.SplitType;
import java.util.List;
import java.util.stream.IntStream;

public abstract class AbstractXYPlotVideoBuilder<P extends XYPlot<D>, D> implements VideoBuilder<P> {

  protected final Configuration c;
  private final ImageBuilder<P> imageBuilder;

  public AbstractXYPlotVideoBuilder(Configuration c, ImageBuilder<P> imageBuilder) {
    this.c = c;
    this.imageBuilder = imageBuilder;
  }

  @Override
  public Video build(VideoInfo videoInfo, P p) {
    return new Video(
        split(p, c.splitType()).stream()
            .map(sp -> imageBuilder.build(new ImageInfo(videoInfo.w(), videoInfo.h()), sp))
            .toList(),
        c.frameRate());
  }

  protected static <T> List<Grid<T>> split(Grid<T> grid, SplitType type) {
    return switch (type) {
      case ROWS -> IntStream.range(0, grid.h())
          .mapToObj(y0 -> Grid.create(grid.w(), 1, (x, y) -> grid.get(x, y0)))
          .toList();
      case COLUMNS -> IntStream.range(0, grid.w())
          .mapToObj(x0 -> Grid.create(1, grid.h(), (x, y) -> grid.get(x0, y)))
          .toList();
    };
  }

  protected abstract List<P> split(P p, SplitType splitType);
}
