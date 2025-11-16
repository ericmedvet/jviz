/*-
 * ========================LICENSE_START=================================
 * jviz-buildable
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
package io.github.ericmedvet.jviz.buildable.builders;

import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.drawer.Drawer.ImageInfo;
import io.github.ericmedvet.jviz.core.drawer.Video;
import io.github.ericmedvet.jviz.core.drawer.VideoBuilder;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot;
import io.github.ericmedvet.jviz.core.plot.LandscapePlot;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldPlot;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.image.BoxPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.LandscapePlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.LinesPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.PointsPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.UnivariateGridPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.VectorialFieldPlotDrawer;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Discoverable(prefixTemplate = "viz.function|f")
public class Functions {

  private Functions() {
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, Object> imagePlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "configuration", dNPM = "viz.plot.configuration.image()") Configuration configuration,
      @Param("secondary") boolean secondary,
      @Param(value = "type", dS = "png") String type
  ) {
    UnaryOperator<ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    class ConditionedDrawer<Y> implements BiFunction<Drawer<Y>, Y, Object> {

      @Override
      public Object apply(Drawer<Y> drawer, Y y) {
        return switch (type.toLowerCase()) {
          case "png" -> drawer.buildRaster(iiAdapter.apply(drawer.imageInfo(y)), y);
          case "svg" -> drawer.buildVectorial(iiAdapter.apply(drawer.imageInfo(y)), y);
          default -> throw new IllegalArgumentException(
              "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
          );
        };
      }
    }
    Function<P, Object> f = p -> {
      if (p instanceof DistributionPlot dp) {
        return new ConditionedDrawer<DistributionPlot>().apply(
            new BoxPlotDrawer(configuration),
            dp
        );
      }
      if (p instanceof LandscapePlot lsp) {
        return new ConditionedDrawer<LandscapePlot>().apply(
            new LandscapePlotDrawer(configuration),
            lsp
        );
      }
      if (p instanceof XYDataSeriesPlot xyp) {
        if (secondary) {
          return new ConditionedDrawer<XYDataSeriesPlot>().apply(
              new PointsPlotDrawer(configuration),
              xyp
          );
        }
        return new ConditionedDrawer<XYDataSeriesPlot>().apply(
            new LinesPlotDrawer(configuration),
            xyp
        );
      }
      if (p instanceof UnivariateGridPlot ugp) {
        return new ConditionedDrawer<UnivariateGridPlot>().apply(
            new UnivariateGridPlotDrawer(configuration),
            ugp
        );
      }
      if (p instanceof VectorialFieldPlot vfp) {
        return new ConditionedDrawer<VectorialFieldPlot>().apply(
            new VectorialFieldPlotDrawer(configuration),
            vfp
        );
      }
      throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "image.plotter").compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Object> toImage(
      @Param(value = "of", dNPM = "f.identity()") Function<X, D> beforeF,
      @Param("drawer") Drawer<D> drawer,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "type", dS = "png") String type
  ) {
    UnaryOperator<Drawer.ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    Function<D, Object> f = d -> switch (type.toLowerCase()) {
      case "png" -> drawer.buildRaster(iiAdapter.apply(drawer.imageInfo(d)), d);
      case "svg" -> drawer.buildVectorial(iiAdapter.apply(drawer.imageInfo(d)), d);
      default -> throw new IllegalArgumentException(
          "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
      );
    };
    return NamedFunction.from(f, "to.image[%s]".formatted(drawer)).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Video> toImagesVideo(
      @Param(value = "of", dNPM = "f.identity()") Function<X, List<D>> beforeF,
      @Param("drawer") Drawer<D> drawer,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "frameRate", dD = 10) double frameRate,
      @Param(value = "encoder", dS = "default") VideoUtils.EncoderFacility encoder
  ) {
    UnaryOperator<VideoBuilder.VideoInfo> viAdapter = vi -> new VideoBuilder.VideoInfo(
        w == -1 ? vi.w() : w,
        h == -1 ? vi.h() : h,
        encoder
    );
    VideoBuilder<List<D>> videoBuilder = VideoBuilder.from(
        drawer,
        Function.identity(),
        frameRate
    );
    Function<List<D>, Video> f = ds -> {
      if (w == -1 && h == -1) {
        return videoBuilder.apply(ds);
      }
      return videoBuilder.build(viAdapter.apply(videoBuilder.videoInfo(ds)), ds);
    };
    return NamedFunction.from(f, "to.images.video[%s]".formatted(drawer))
        .compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Object> toMultiImage(
      @Param(value = "of", dNPM = "f.identity()") Function<X, List<D>> beforeF,
      @Param("drawer") Drawer<D> drawer,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "type", dS = "png") String type,
      @Param(value = "arrangement", dS = "horizontal") Drawer.Arrangement arrangement
  ) {
    UnaryOperator<Drawer.ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    Drawer<List<D>> multiDrawer = drawer.multi(arrangement);
    Function<List<D>, Object> f = ds -> switch (type.toLowerCase()) {
      case "png" -> multiDrawer.buildRaster(iiAdapter.apply(multiDrawer.imageInfo(ds)), ds);
      case "svg" -> multiDrawer.buildVectorial(iiAdapter.apply(multiDrawer.imageInfo(ds)), ds);
      default -> throw new IllegalArgumentException(
          "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
      );
    };
    return NamedFunction.from(f, "to.image[%s]".formatted(drawer)).compose(beforeF);
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <X, D> NamedFunction<X, Video> toVideo(
      @Param(value = "of", dNPM = "f.identity()") Function<X, D> beforeF,
      @Param("video") VideoBuilder<D> videoBuilder,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "encoder", dS = "default") VideoUtils.EncoderFacility encoder
  ) {
    UnaryOperator<VideoBuilder.VideoInfo> viAdapter = vi -> new VideoBuilder.VideoInfo(
        w == -1 ? vi.w() : w,
        h == -1 ? vi.h() : h,
        encoder
    );
    Function<D, Video> f = d -> {
      if (w == -1 && h == -1) {
        return videoBuilder.apply(d);
      }
      return videoBuilder.build(viAdapter.apply(videoBuilder.videoInfo(d)), d);
    };
    return NamedFunction.from(f, "to.video[%s]".formatted(videoBuilder)).compose(beforeF);
  }

}
