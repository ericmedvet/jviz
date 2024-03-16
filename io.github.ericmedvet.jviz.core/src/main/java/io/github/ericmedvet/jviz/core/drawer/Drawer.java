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
import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
public interface Drawer<E> {

  Color bgColor = Color.WHITE;

  void draw(Graphics2D g, E e);

  default void clean(Graphics2D g) {
    g.setColor(bgColor);
    g.fill(new Rectangle2D.Double(0, 0, g.getClipBounds().width, g.getClipBounds().height));
  }

  default BufferedImage draw(int w, int h, E e) {
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setClip(new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight()));
    clean(g);
    draw(g, e);
    g.dispose();
    return image;
  }

  default void saveImage(int w, int h, String formatName, File file, E e) throws IOException {
    ImageIO.write(draw(w, h, e), formatName, file);
  }

  default void saveImage(int w, int h, File file, E e) throws IOException {
    String[] tokens = file.getName().split("\\.");
    saveImage(w, h, tokens[tokens.length - 1], file, e);
  }

  default void showImage(int w, int h, E e) {
    Misc.showImage(draw(w, h, e));
  }

  default void saveVideo(int w, int h, File file, double frameRate, VideoUtils.EncoderFacility encoder, List<E> es)
      throws IOException {
    VideoUtils.encodeAndSave(es.stream().map(e -> draw(w, h, e)).toList(), frameRate, file, encoder);
  }
}
