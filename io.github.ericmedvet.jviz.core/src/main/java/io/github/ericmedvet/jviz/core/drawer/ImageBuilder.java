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

import io.github.ericmedvet.jviz.core.util.Misc;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import javax.imageio.ImageIO;

public interface ImageBuilder<E> extends Function<E, BufferedImage> {

  int DEFAULT_W = 1000;
  int DEFAULT_H = 800;

  BufferedImage build(ImageInfo imageInfo, E e);

  default <F> ImageBuilder<F> on(Function<? super F, ? extends E> function) {
    ImageBuilder<E> thisImageBuilder = this;
    return new ImageBuilder<F>() {
      @Override
      public BufferedImage build(ImageInfo imageInfo, F f) {
        return thisImageBuilder.build(imageInfo, function.apply(f));
      }

      @Override
      public ImageInfo imageInfo(F f) {
        return thisImageBuilder.imageInfo(function.apply(f));
      }

      @Override
      public BufferedImage apply(F f) {
        E e = function.apply(f);
        return thisImageBuilder.build(thisImageBuilder.imageInfo(e), e);
      }
    };
  }

  @Override
  default BufferedImage apply(E e) {
    return build(imageInfo(e), e);
  }

  default ImageInfo imageInfo(E e) {
    return new ImageInfo(DEFAULT_W, DEFAULT_H);
  }

  default void save(ImageInfo imageInfo, String formatName, File file, E e) throws IOException {
    ImageIO.write(build(imageInfo, e), formatName, file);
  }

  default void save(String formatName, File file, E e) throws IOException {
    ImageIO.write(apply(e), formatName, file);
  }

  default void save(ImageInfo imageInfo, File file, E e) throws IOException {
    String[] tokens = file.getName().split("\\.");
    save(imageInfo, tokens[tokens.length - 1], file, e);
  }

  default void save(File file, E e) throws IOException {
    String[] tokens = file.getName().split("\\.");
    ImageIO.write(apply(e), tokens[tokens.length - 1], file);
  }

  default void show(ImageInfo imageInfo, E e) {
    Misc.showImage(build(imageInfo, e));
  }

  default void show(E e) {
    Misc.showImage(apply(e));
  }

  record ImageInfo(int w, int h) {}
}
