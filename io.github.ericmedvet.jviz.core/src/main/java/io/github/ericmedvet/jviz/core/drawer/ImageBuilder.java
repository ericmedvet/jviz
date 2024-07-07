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

  int DEFAULT_W = 600;
  int DEFAULT_H = 400;

  BufferedImage build(ImageInfo imageInfo, E e);

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
    save(imageInfo(e), formatName, file, e);
  }

  default void save(ImageInfo imageInfo, File file, E e) throws IOException {
    String[] tokens = file.getName().split("\\.");
    save(imageInfo, tokens[tokens.length - 1], file, e);
  }

  default void save(File file, E e) throws IOException {
    save(imageInfo(e), file, e);
  }

  default void show(ImageInfo imageInfo, E e) {
    Misc.showImage(build(imageInfo, e));
  }

  default void show(E e) {
    show(imageInfo(e), e);
  }

  record ImageInfo(int w, int h) {}
}
