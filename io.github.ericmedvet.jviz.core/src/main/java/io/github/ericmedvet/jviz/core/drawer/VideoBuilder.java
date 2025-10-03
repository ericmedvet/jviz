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
package io.github.ericmedvet.jviz.core.drawer;

import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

public interface VideoBuilder<E> extends Function<E, Video> {

  int DEFAULT_W = 300;
  int DEFAULT_H = 200;

  static <F, E> VideoBuilder<F> from(Drawer<E> drawer, Function<F, List<E>> splitter, double frameRate) {
    return new VideoBuilder<>() {
      @Override
      public Video build(VideoInfo videoInfo, F f) {
        List<BufferedImage> images = splitter.apply(f)
            .stream()
            .map(e -> drawer.buildRaster(new Drawer.ImageInfo(videoInfo.w, videoInfo.h), e))
            .toList();
        return new Video(images, frameRate, videoInfo.encoder);
      }

      @Override
      public VideoInfo videoInfo(F f) {
        VideoInfo vi = VideoBuilder.super.videoInfo(f);
        Drawer.ImageInfo ii = drawer.imageInfo(splitter.apply(f).getFirst());
        return new VideoInfo(ii.w(), ii.h(), vi.encoder());
      }
    };
  }

  static <F, E> VideoBuilder<F> from(Drawer<E> drawer, Function<F, SortedMap<Double, E>> splitter) {
    return new VideoBuilder<>() {
      @Override
      public Video build(VideoInfo videoInfo, F f) {
        SortedMap<Double, E> map = splitter.apply(f);
        List<BufferedImage> images = map.values()
            .stream()
            .map(e -> drawer.buildRaster(new Drawer.ImageInfo(videoInfo.w, videoInfo.h), e))
            .toList();
        return new Video(images, ((double) map.size()) / (map.lastKey()) - map.firstKey(), videoInfo.encoder);
      }

      @Override
      public VideoInfo videoInfo(F f) {
        VideoInfo vi = VideoBuilder.super.videoInfo(f);
        SortedMap<Double, E> map = splitter.apply(f);
        Drawer.ImageInfo ii = drawer.imageInfo(map.get(map.firstKey()));
        return new VideoInfo(ii.w(), ii.h(), vi.encoder());
      }
    };
  }

  default <F> VideoBuilder<F> on(Function<? super F, ? extends E> function) {
    VideoBuilder<E> thisVideoBuilder = this;
    return new VideoBuilder<>() {
      @Override
      public Video build(VideoInfo videoInfo, F f) {
        return thisVideoBuilder.build(videoInfo, function.apply(f));
      }

      @Override
      public VideoInfo videoInfo(F f) {
        return thisVideoBuilder.videoInfo(function.apply(f));
      }

      @Override
      public Video apply(F f) {
        E e = function.apply(f);
        return thisVideoBuilder.build(thisVideoBuilder.videoInfo(e), e);
      }
    };
  }

  Video build(VideoInfo videoInfo, E e);

  @Override
  default Video apply(E e) {
    return build(videoInfo(e), e);
  }

  default void save(VideoInfo videoInfo, File file, E e) {
    save(build(videoInfo, e), file);
  }

  default void save(File file, E e) {
    save(build(videoInfo(e), e), file);
  }

  default VideoInfo videoInfo(E e) {
    return new VideoInfo(DEFAULT_W, DEFAULT_H, VideoUtils.defaultEncoder());
  }

  private void save(Video video, File file) {
    try {
      Files.write(file.toPath(), video.data(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  record VideoInfo(int w, int h, VideoUtils.EncoderFacility encoder) {}
}
