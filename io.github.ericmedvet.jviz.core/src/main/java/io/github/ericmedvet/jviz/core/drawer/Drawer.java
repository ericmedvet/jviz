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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2024/02/23 for jgea
 */
public interface Drawer<E> extends ImageBuilder<E> {

  Color BG_COLOR = Color.WHITE;

  void draw(Graphics2D g, E e);

  default Drawer<E> andThen(Drawer<E> other) {
    Drawer<E> thisDrawer = this;
    return (g, e) -> {
      thisDrawer.draw(g, e);
      other.draw(g, e);
    };
  }

  static void clean(Graphics2D g) {
    g.setColor(BG_COLOR);
    g.fill(new Rectangle2D.Double(0, 0, g.getClipBounds().width, g.getClipBounds().height));
  }

  @Override
  default BufferedImage build(ImageInfo imageInfo, E e) {
    BufferedImage image = new BufferedImage(imageInfo.w(), imageInfo.h(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setClip(new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight()));
    clean(g);
    draw(g, e);
    g.dispose();
    return image;
  }

  static <E> Drawer<E> stringWriter(Color color, float fontSize, Function<E, String> f) {
    return (g, e) -> {
      g.setFont(g.getFont().deriveFont(fontSize));
      double x0 = g.getClipBounds().getMinX();
      double y0 = g.getClipBounds().getMinX();
      g.setColor(color);
      double lH = g.getFontMetrics().getHeight();
      AtomicInteger c = new AtomicInteger(0);
      f.apply(e).lines().forEach(l -> g.drawString(l, (float) x0, (float) (y0 + lH * c.incrementAndGet())));
    };
  }
}
