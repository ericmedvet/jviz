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

  record VideoInfo(int w, int h, VideoUtils.EncoderFacility encoder) {}

  Video build(VideoInfo videoInfo, E e);

  static <F, E> VideoBuilder<F> from(ImageBuilder<E> imageBuilder, Function<F, List<E>> splitter, double frameRate) {
    return new VideoBuilder<>() {
      @Override
      public Video build(VideoInfo videoInfo, F f) {
        List<BufferedImage> images = splitter.apply(f).stream()
            .map(e -> imageBuilder.build(new ImageBuilder.ImageInfo(videoInfo.w, videoInfo.h), e))
            .toList();
        return new Video(images, frameRate);
      }

      @Override
      public VideoInfo videoInfo(F f) {
        VideoInfo vi = VideoBuilder.super.videoInfo(f);
        ImageBuilder.ImageInfo ii =
            imageBuilder.imageInfo(splitter.apply(f).get(0));
        return new VideoInfo(ii.w(), ii.h(), vi.encoder());
      }
    };
  }

  static <F, E> VideoBuilder<F> from(ImageBuilder<E> imageBuilder, Function<F, SortedMap<Double, E>> splitter) {
    return new VideoBuilder<F>() {
      @Override
      public Video build(VideoInfo videoInfo, F f) {
        SortedMap<Double, E> map = splitter.apply(f);
        List<BufferedImage> images = map.values().stream()
            .map(e -> imageBuilder.build(new ImageBuilder.ImageInfo(videoInfo.w, videoInfo.h), e))
            .toList();
        return new Video(images, ((double) map.size()) / (map.lastKey()) - map.firstKey());
      }

      @Override
      public VideoInfo videoInfo(F f) {
        VideoInfo vi = VideoBuilder.super.videoInfo(f);
        SortedMap<Double, E> map = splitter.apply(f);
        ImageBuilder.ImageInfo ii = imageBuilder.imageInfo(map.get(map.firstKey()));
        return new VideoInfo(ii.w(), ii.h(), vi.encoder());
      }
    };
  }

  @Override
  default Video apply(E e) {
    return build(videoInfo(e), e);
  }

  default void save(VideoInfo videoInfo, File file, E e) {
    try {
      Files.write(
          file.toPath(),
          build(videoInfo, e).data(videoInfo.encoder),
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  default void save(File file, E e) {
    save(videoInfo(e), file, e);
  }

  default VideoInfo videoInfo(E e) {
    return new VideoInfo(DEFAULT_W, DEFAULT_H, VideoUtils.defaultEncoder());
  }
}
