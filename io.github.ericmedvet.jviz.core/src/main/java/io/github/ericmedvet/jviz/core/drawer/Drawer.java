/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 - 2025 Eric Medvet
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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.imageio.ImageIO;

public interface Drawer<E> {

  int DEFAULT_W = 1000;
  int DEFAULT_H = 800;
  Color BG_COLOR = Color.WHITE;

  static void clean(Graphics2D g) {
    g.setColor(BG_COLOR);
    g.fill(new Rectangle2D.Double(0, 0, g.getClipBounds().getWidth(), g.getClipBounds().getHeight()));
  }

  static <E> Drawer<E> stringWriter(Color color, float fontSize, Function<E, String> f) {
    return new Drawer<E>() {
      @Override
      public void draw(Graphics2D g, E e) {
        g.setFont(g.getFont().deriveFont(fontSize));
        double x0 = g.getClipBounds().getMinX();
        double y0 = g.getClipBounds().getMinY();
        g.setColor(color);
        double lH = g.getFontMetrics().getHeight();
        AtomicInteger c = new AtomicInteger(0);
        f.apply(e)
            .lines()
            .forEach(l -> g.drawString(l, (float) x0, (float) (y0 + lH * c.incrementAndGet())));
      }

      @Override
      public ImageInfo imageInfo(E e) {
        List<Integer> lengths = f.apply(e).lines().map(String::length).toList();
        return new ImageInfo(
            Math.round(lengths.stream().max(Integer::compareTo).orElse(0) * fontSize),
            Math.round(lengths.size() * fontSize * 2)
        );
      }
    };
  }

  default Drawer<E> andThen(Drawer<E> other) {
    Drawer<E> thisDrawer = this;
    return (g, e) -> {
      thisDrawer.draw(g, e);
      other.draw(g, e);
    };
  }

  default Drawer<E> bordered(Color color) {
    Drawer<E> thisDrawer = this;
    return new Drawer<>() {
      @Override
      public void draw(Graphics2D g, E e) {
        g.setColor(color);
        g.draw(g.getClipBounds());
        thisDrawer.draw(g, e);
      }

      @Override
      public ImageInfo imageInfo(E e) {
        return thisDrawer.imageInfo(e);
      }
    };
  }

  default <O> O build(G2DProvider<O> provider, E e) {
    clean(provider.g2D());
    draw(provider.g2D(), e);
    provider.g2D().dispose();
    return provider.output();
  }

  default BufferedImage buildRaster(ImageInfo imageInfo, E e) {
    return build(new BufferedImageG2DProvider(imageInfo), e);
  }

  default String buildVectorial(ImageInfo imageInfo, E e) {
    return build(new SvgG2DProvider(imageInfo), e);
  }

  void draw(Graphics2D g, E e);

  default ImageInfo imageInfo(E e) {
    return new ImageInfo(DEFAULT_W, DEFAULT_H);
  }

  default Drawer<List<E>> multi(Arrangement arrangement) {
    Drawer<E> thisDrawer = this;
    return new Drawer<>() {
      @Override
      public void draw(Graphics2D g, List<E> es) {
        ImageInfo allII = imageInfo(es);
        Rectangle bounds = g.getClipBounds();
        double wScale = bounds.getWidth() / allII.w();
        double hScale = bounds.getHeight() / allII.h();
        g.scale(wScale, hScale);
        for (E e : es) {
          ImageInfo ii = thisDrawer.imageInfo(e);
          g.setClip(new Double(0, 0, ii.w(), ii.h()));
          AffineTransform preTransform = g.getTransform();
          thisDrawer.draw(g, e);
          g.setTransform(preTransform);
          g.translate(
              switch (arrangement) {
                case HORIZONTAL -> ii.w();
                case VERTICAL -> 0;
              },
              switch (arrangement) {
                case HORIZONTAL -> 0;
                case VERTICAL -> ii.h();
              }
          );
        }
      }

      @Override
      public ImageInfo imageInfo(List<E> es) {
        List<ImageInfo> imageInfos = es.stream().map(thisDrawer::imageInfo).toList();
        return switch (arrangement) {
          case HORIZONTAL -> new ImageInfo(
              imageInfos.stream().mapToInt(ImageInfo::w).sum(),
              imageInfos.stream().mapToInt(ImageInfo::h).max().orElse(0)
          );
          case VERTICAL -> new ImageInfo(
              imageInfos.stream().mapToInt(ImageInfo::w).max().orElse(0),
              imageInfos.stream().mapToInt(ImageInfo::h).sum()
          );
        };
      }
    };
  }

  default <F> Drawer<F> on(Function<? super F, ? extends E> function) {
    Drawer<E> thisDrawer = this;
    return new Drawer<>() {
      @Override
      public void draw(Graphics2D g2D, F f) {
        thisDrawer.draw(g2D, function.apply(f));
      }

      @Override
      public ImageInfo imageInfo(F f) {
        return thisDrawer.imageInfo(function.apply(f));
      }
    };
  }

  default void save(
      ImageInfo imageInfo,
      String formatName,
      File file,
      E e
  ) throws IOException {
    if (formatName.equalsIgnoreCase("svg")) {
      String content = buildVectorial(imageInfo, e);
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        writer.write(content);
      }
    } else {
      ImageIO.write(buildRaster(imageInfo, e), formatName, file);
    }
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
    Misc.showImage(buildRaster(imageInfo, e));
  }

  default void show(E e) {
    Misc.showImage(buildRaster(imageInfo(e), e));
  }

  enum Arrangement { HORIZONTAL, VERTICAL }

  record ImageInfo(int w, int h) {

  }
}
