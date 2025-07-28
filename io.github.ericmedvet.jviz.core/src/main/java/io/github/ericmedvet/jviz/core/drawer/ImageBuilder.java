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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;

public interface ImageBuilder<E> {

  int DEFAULT_W = 1000;
  int DEFAULT_H = 800;

  <G extends Graphics2D, O> O build(
      ImageInfo imageInfo,
      E e,
      Supplier<EnhancedGraphics<G, O>> supplier,
      UnaryOperator<EnhancedGraphics<G, O>> operator
  );

  default BufferedImage buildRaster(ImageInfo imageInfo, E e) {
    return build(
        imageInfo,
        e,
        () -> {
          BufferedImage image = new BufferedImage(
              imageInfo.w(),
              imageInfo.h(),
              BufferedImage.TYPE_3BYTE_BGR
          );
          Graphics2D g2D = image.createGraphics();
          g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          return new EnhancedGraphics<>(g2D, image);
        },
        enhancedGraphics -> enhancedGraphics
    );
  }

  default String buildVectorial(ImageInfo imageInfo, E e) {
    Supplier<EnhancedGraphics<org.apache.batik.svggen.SVGGraphics2D, String>> batikSupplier = () -> {
      Document doc = GenericDOMImplementation.getDOMImplementation()
          .createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
      org.apache.batik.svggen.SVGGraphics2D svgGenerator = new org.apache.batik.svggen.SVGGraphics2D(doc);
      return new EnhancedGraphics<>(svgGenerator, "");
    };
    UnaryOperator<EnhancedGraphics<org.apache.batik.svggen.SVGGraphics2D, String>> batikOperator = enhancedGraphics -> {
      StringWriter sw = new StringWriter();
      try (sw) {
        enhancedGraphics.g2d().stream(sw);
      } catch (IOException ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Cannot write svg file for %s".formatted(e), ex);
      }
      // hack to fix the wrong behavior of batik who writes the <!DOCTYPE> twice
      String content = sw.toString().lines().skip(1).collect(Collectors.joining(System.lineSeparator()));
      return new EnhancedGraphics<>(enhancedGraphics.g2d(), content);
    };
    return build(
        imageInfo,
        e,
        batikSupplier,
        batikOperator
    );
  }

  default <F> ImageBuilder<F> on(Function<? super F, ? extends E> function) {
    ImageBuilder<E> thisImageBuilder = this;
    return new ImageBuilder<>() {
      @Override
      public <G extends Graphics2D, O> O build(
          ImageInfo imageInfo,
          F f,
          Supplier<EnhancedGraphics<G, O>> supplier,
          UnaryOperator<EnhancedGraphics<G, O>> operator
      ) {
        return thisImageBuilder.build(imageInfo, function.apply(f), supplier, operator);
      }

      @Override
      public ImageInfo imageInfo(F f) {
        return thisImageBuilder.imageInfo(function.apply(f));
      }
    };
  }

  default ImageInfo imageInfo(E e) {
    return new ImageInfo(DEFAULT_W, DEFAULT_H);
  }

  default void save(ImageInfo imageInfo, String formatName, File file, E e) throws IOException {
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

  record ImageInfo(int w, int h) {}

  record EnhancedGraphics<G extends Graphics2D, O>(G g2d, O o) {}
}
