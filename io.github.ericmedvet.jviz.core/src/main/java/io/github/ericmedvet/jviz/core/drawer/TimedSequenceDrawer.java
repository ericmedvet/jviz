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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public interface TimedSequenceDrawer<E> extends Drawer<SortedMap<Double, E>> {

  void drawSingle(Graphics2D g, double t, E e);

  default Drawer<Map.Entry<Double, E>> single() {
    return (g, entry) -> drawSingle(g, entry.getKey(), entry.getValue());
  }

  default void drawAll(Graphics2D g, SortedMap<Double, E> map) {}

  @Override
  default void draw(Graphics2D g, SortedMap<Double, E> map) {
    double t = map.lastKey();
    drawSingle(g, t, map.get(t));
    drawAll(g, map);
  }

  default List<BufferedImage> drawAll(int w, int h, SortedMap<Double, E> map) {
    return map.entrySet().stream().map(e -> single().draw(w, h, e)).toList();
  }

  default void saveVideo(
      int w, int h, File file, double frameRate, VideoUtils.EncoderFacility encoder, SortedMap<Double, E> map)
      throws IOException {
    VideoUtils.encodeAndSave(drawAll(w, h, map), frameRate, file, encoder);
  }

  default void saveVideo(int w, int h, File file, double frameRate, SortedMap<Double, E> map) throws IOException {
    saveVideo(w, h, file, frameRate, VideoUtils.EncoderFacility.JCODEC, map);
  }

  default void saveVideo(int w, int h, File file, VideoUtils.EncoderFacility encoder, SortedMap<Double, E> map)
      throws IOException {
    double frameRate = (double) map.size() / (map.lastKey() - map.firstKey());
    saveVideo(w, h, file, frameRate, encoder, map);
  }

  default void saveVideo(int w, int h, File file, SortedMap<Double, E> map) throws IOException {
    saveVideo(w, h, file, VideoUtils.EncoderFacility.JCODEC, map);
  }
}
