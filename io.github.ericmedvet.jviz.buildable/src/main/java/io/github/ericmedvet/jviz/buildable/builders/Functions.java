/*-
 * ========================LICENSE_START=================================
 * jviz-buildable
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
import io.github.ericmedvet.jviz.core.plot.HeatPolyMapPlot;
import io.github.ericmedvet.jviz.core.plot.LandscapePlot;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldPlot;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.csv.DistributionPlotCsvBuilder;
import io.github.ericmedvet.jviz.core.plot.csv.LandscapePlotCsvBuilder;
import io.github.ericmedvet.jviz.core.plot.csv.TrajectoryPlotCsvBuilder;
import io.github.ericmedvet.jviz.core.plot.csv.UnivariateGridPlotCsvBuilder;
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
import io.github.ericmedvet.jviz.core.plot.video.BoxPlotVideoBuilder;
import io.github.ericmedvet.jviz.core.plot.video.LandscapePlotVideoBuilder;
import io.github.ericmedvet.jviz.core.plot.video.LinesPlotVideoBuilder;
import io.github.ericmedvet.jviz.core.plot.video.PointsPlotVideoBuilder;
import io.github.ericmedvet.jviz.core.plot.video.UnivariateGridPlotVideoBuilder;
import io.github.ericmedvet.jviz.core.plot.video.VectorialFieldVideoBuilder;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Discoverable(prefixTemplate = "viz.function|f")
public class Functions {

  private Functions() {
  }

  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, String> csvPlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "columnNameJoiner", dS = ".") String columnNameJoiner,
      @Param(value = "doubleFormat", dS = "%.3e") String doubleFormat,
      @Param(value = "delimiter", dS = "\t") String delimiter,
      @Param(value = "missingDataString", dS = "nan") String missingDataString,
      @Param(value = "mode", dS = "paper_friendly") io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode m
  ) {
    io.github.ericmedvet.jviz.core.plot.csv.Configuration c = new io.github.ericmedvet.jviz.core.plot.csv.Configuration(
        columnNameJoiner,
        doubleFormat,
        delimiter,
        List.of(new io.github.ericmedvet.jviz.core.plot.csv.Configuration.Replacement("\\W+", ".")),
        missingDataString
    );
    Function<P, String> f = p -> switch (p) {
      case DistributionPlot dp -> new DistributionPlotCsvBuilder(c, m).apply(dp);
      case LandscapePlot lsp -> new LandscapePlotCsvBuilder(c, m).apply(lsp);
      case XYDataSeriesPlot xyp -> new XYDataSeriesPlotCsvBuilder(c, m).apply(xyp);
      case UnivariateGridPlot ugp -> new UnivariateGridPlotCsvBuilder(c, m).apply(ugp);
      case VectorialFieldPlot vfp -> new VectorialFieldPlotCsvBuilder(c, m).apply(vfp);
      case TrajectoryPlot tp -> new TrajectoryPlotCsvBuilder(c, m).apply(tp);
      // TODO add HeatPolyMapPlot plots
      default -> throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "csv.plotter").compose(beforeF);
  }

  private static <P> Object image(P plot, Supplier<? extends Drawer<? super P>> supplier, int w, int h, String type) {
    UnaryOperator<ImageInfo> iiAdapter = ii -> new Drawer.ImageInfo(
        w == -1 ? ii.w() : w,
        h == -1 ? ii.h() : h
    );
    Drawer<? super P> drawer = supplier.get();
    return switch (type.toLowerCase()) {
      case "png" -> drawer.buildRaster(iiAdapter.apply(drawer.imageInfo(plot)), plot);
      case "svg" -> drawer.buildVectorial(iiAdapter.apply(drawer.imageInfo(plot)), plot);
      default -> throw new IllegalArgumentException(
          "Invalid type '%s', which is not 'png' nor 'svg'".formatted(type)
      );
    };
  }

  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, Object> imagePlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "configuration", dNPM = "viz.plot.configuration.image()") Configuration c,
      @Param("secondary") boolean secondary,
      @Param(value = "type", dS = "png") String type
  ) {
    Function<P, Object> f = p -> switch (p) {
      case DistributionPlot dp -> image(dp, () -> new BoxPlotDrawer(c), w, h, type);
      case LandscapePlot lsp -> image(lsp, () -> new LandscapePlotDrawer(c), w, h, type);
      case XYDataSeriesPlot xyp when secondary -> image(xyp, () -> new PointsPlotDrawer(c), w, h, type);
      case XYDataSeriesPlot xyp -> image(xyp, () -> new LinesPlotDrawer(c), w, h, type);
      case UnivariateGridPlot ugp -> image(ugp, () -> new UnivariateGridPlotDrawer(c), w, h, type);
      case VectorialFieldPlot vfp -> image(vfp, () -> new VectorialFieldPlotDrawer(c), w, h, type);
      case TrajectoryPlot tp -> image(tp, () -> new TrajectoryPlotDrawer(c), w, h, type);
      case HeatPolyMapPlot hpmp -> image(hpmp, () -> new HeatPolyMapPlotDrawer(c), w, h, type);
      default -> throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "image.plotter").compose(beforeF);
  }

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

  private static <P> Video video(
      P plot,
      Supplier<? extends VideoBuilder<? super P>> supplier,
      int w,
      int h,
      VideoUtils.EncoderFacility encoder
  ) {
    UnaryOperator<VideoBuilder.VideoInfo> viAdapter = vi -> new VideoBuilder.VideoInfo(
        w == -1 ? vi.w() : w,
        h == -1 ? vi.h() : h,
        encoder
    );
    VideoBuilder<? super P> vb = supplier.get();
    return vb.build(viAdapter.apply(vb.videoInfo(plot)), plot);
  }

  @Cacheable
  public static <X, P extends XYPlot<D>, D> NamedFunction<X, Video> videoPlotter(
      @Param(value = "of", dNPM = "f.identity()") Function<X, P> beforeF,
      @Param(value = "w", dI = -1) int w,
      @Param(value = "h", dI = -1) int h,
      @Param(value = "encoder", dS = "default") VideoUtils.EncoderFacility e,
      @Param(value = "frameRate", dD = 10) double frameRate,
      @Param(value = "configuration", dNPM = "viz.plot.configuration.image()") Configuration ic,
      @Param("secondary") boolean secondary
  ) {
    io.github.ericmedvet.jviz.core.plot.video.Configuration vc = new io.github.ericmedvet.jviz.core.plot.video.Configuration(
        io.github.ericmedvet.jviz.core.plot.video.Configuration.DEFAULT.splitType(),
        frameRate
    );
    Function<P, Video> f = p -> switch (p) {
      case DistributionPlot dp -> video(dp, () -> new BoxPlotVideoBuilder(vc, ic), w, h, e);
      case LandscapePlot lsp -> video(lsp, () -> new LandscapePlotVideoBuilder(vc, ic), w, h, e);
      case XYDataSeriesPlot xyp when secondary -> video(xyp, () -> new PointsPlotVideoBuilder(vc, ic), w, h, e);
      case XYDataSeriesPlot xyp -> video(xyp, () -> new LinesPlotVideoBuilder(vc, ic), w, h, e);
      case UnivariateGridPlot ugp -> video(ugp, () -> new UnivariateGridPlotVideoBuilder(vc, ic), w, h, e);
      case VectorialFieldPlot vfp -> video(vfp, () -> new VectorialFieldVideoBuilder(vc, ic), w, h, e);
      // TODO add TrajectoryPlot and HeatPolyMapPlot plots
      default -> throw new IllegalArgumentException(
          "Unsupported type of plot %s".formatted(p.getClass().getSimpleName())
      );
    };
    return NamedFunction.from(f, "video.plotter").compose(beforeF);
  }
}