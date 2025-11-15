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

import io.github.ericmedvet.jviz.core.drawer.Drawer.ImageInfo;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

public class SvgG2DProvider implements G2DProvider<String> {

  private final SVGGraphics2D g2D;

  public SvgG2DProvider(ImageInfo imageInfo) {
    Document doc = GenericDOMImplementation.getDOMImplementation()
        .createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
    g2D = new org.apache.batik.svggen.SVGGraphics2D(doc);
    g2D.setClip(new Rectangle2D.Double(0, 0, imageInfo.w(), imageInfo.h()));
  }

  @Override
  public Graphics2D g2D() {
    return g2D;
  }

  @Override
  public String output() {
    StringWriter sw = new StringWriter();
    try (sw) {
      g2D.stream(sw);
    } catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, "Cannot write svg file", ex);
    }
    // hack to fix the wrong behavior of batik who writes the <!DOCTYPE> twice
    return sw.toString().lines().skip(1).collect(Collectors.joining(System.lineSeparator()));
  }
}
