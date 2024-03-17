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
import javax.imageio.ImageIO;

public interface ImageBuilder<E> {
  BufferedImage build(int w, int h, E e);

  default void save(int w, int h, String formatName, File file, E e) throws IOException {
    ImageIO.write(build(w, h, e), formatName, file);
  }

  default void save(int w, int h, File file, E e) throws IOException {
    String[] tokens = file.getName().split("\\.");
    save(w, h, tokens[tokens.length - 1], file, e);
  }

  default void show(int w, int h, E e) {
    Misc.showImage(build(w, h, e));
  }
}
