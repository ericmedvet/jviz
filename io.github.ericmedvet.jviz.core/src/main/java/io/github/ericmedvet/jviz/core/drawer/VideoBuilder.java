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

import io.github.ericmedvet.jviz.core.drawer.VideoBuilder.Video;
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import io.github.ericmedvet.jviz.core.util.VideoUtils.EncoderFacility;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.logging.Logger;

public interface VideoBuilder<E> extends Function<E, Video> {

  int DEFAULT_W = 300;
  int DEFAULT_H = 200;
  VideoUtils.EncoderFacility DEFAULT_ENCODER = EncoderFacility.JCODEC;

  record Video(List<BufferedImage> images, double frameRate) {
    @Override
    public String toString() {
      return "(%dx%d)x%d@%.1ffps"
          .formatted(images.get(0).getWidth(), images.get(0).getHeight(), images.size(), frameRate);
    }
  }

  record VideoInfo(int w, int h, VideoUtils.EncoderFacility encoder) {}

  Video build(VideoInfo videoInfo, E e) throws IOException;

  static <F, E> VideoBuilder<F> from(ImageBuilder<E> imageBuilder, Function<F, List<E>> splitter, double frameRate) {
    return new VideoBuilder<F>() {
      @Override
      public Video build(VideoInfo videoInfo, F f) throws IOException {
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
      public Video build(VideoInfo videoInfo, F f) throws IOException {
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
    try {
      return build(videoInfo(e), e);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  default void save(VideoInfo videoInfo, File file, E e) throws IOException {
    Logger.getLogger(getClass().getSimpleName()).fine("Building video");
    Video video = build(videoInfo, e);
    Logger.getLogger(getClass().getSimpleName()).fine("Video built: %s".formatted(video));
    VideoUtils.encodeAndSave(video.images, video.frameRate, file, videoInfo.encoder);
    Logger.getLogger(getClass().getSimpleName()).fine("Video saved on %s".formatted(file));
  }

  default void save(File file, E e) throws IOException {
    save(videoInfo(e), file, e);
  }

  default VideoInfo videoInfo(E e) {
    return new VideoInfo(DEFAULT_W, DEFAULT_H, DEFAULT_ENCODER);
  }
}
