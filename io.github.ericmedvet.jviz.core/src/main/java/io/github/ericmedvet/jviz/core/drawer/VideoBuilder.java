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
import java.io.*;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.logging.Logger;

public interface VideoBuilder<E> {

  record VideoInfo(int w, int h, VideoUtils.EncoderFacility encoder) {}

  record Video(List<BufferedImage> images, double frameRate) {
    @Override
    public String toString() {
      return "(%dx%d)x%d@%.1ffps"
          .formatted(images.get(0).getWidth(), images.get(0).getHeight(), images.size(), frameRate);
    }
  }

  Video build(VideoInfo videoInfo, E e) throws IOException;

  default void save(VideoInfo videoInfo, File file, E e) throws IOException {
    Logger.getLogger(getClass().getSimpleName()).fine("Building video");
    Video video = build(videoInfo, e);
    Logger.getLogger(getClass().getSimpleName()).fine("Video built: %s".formatted(video));
    VideoUtils.encodeAndSave(video.images, video.frameRate, file, videoInfo.encoder);
    Logger.getLogger(getClass().getSimpleName()).fine("Video saved on %s".formatted(file));
  }

  static <F, E> VideoBuilder<F> from(ImageBuilder<E> imageBuilder, Function<F, List<E>> splitter, double frameRate) {
    return (videoInfo, f) -> {
      List<BufferedImage> images = splitter.apply(f).stream()
          .map(e -> imageBuilder.build(new ImageBuilder.ImageInfo(videoInfo.w, videoInfo.h), e))
          .toList();
      return new Video(images, frameRate);
    };
  }

  static <F, E> VideoBuilder<F> from(ImageBuilder<E> imageBuilder, Function<F, SortedMap<Double, E>> splitter) {
    return (videoInfo, f) -> {
      SortedMap<Double, E> map = splitter.apply(f);
      List<BufferedImage> images = map.values().stream()
          .map(e -> imageBuilder.build(new ImageBuilder.ImageInfo(videoInfo.w, videoInfo.h), e))
          .toList();
      return new Video(images, ((double) map.size()) / (map.lastKey()) - map.firstKey());
    };
  }
}
