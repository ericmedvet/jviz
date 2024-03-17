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
import java.util.function.Function;

public interface VideoBuilder<E> {
  byte[] build(int w, int h, E e) throws IOException;

  default void save(int w, int h, File file, E e) throws IOException {
    byte[] data = build(w, h, e);
    try (OutputStream os = new FileOutputStream(file);
        InputStream is = new ByteArrayInputStream(data)) {
      byte[] buffer = new byte[1024];
      while (true) {
        int read = is.read(buffer, 0, buffer.length);
        if (read == -1) {
          break;
        }
        os.write(buffer, 0, read);
      }
    }
  }

  static <F, E> VideoBuilder<F> from(
      ImageBuilder<E> imageBuilder,
      Function<F, List<E>> splitter,
      double frameRate,
      VideoUtils.EncoderFacility encoder) {
    return (w, h, f) -> {
      List<BufferedImage> images = splitter.apply(f).stream()
          .map(e -> imageBuilder.build(w, h, e))
          .toList();
      return VideoUtils.encode(images, frameRate, encoder);
    };
  }
}
