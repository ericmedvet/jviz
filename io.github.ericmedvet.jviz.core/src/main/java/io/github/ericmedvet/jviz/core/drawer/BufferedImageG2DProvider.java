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
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class BufferedImageG2DProvider implements G2DProvider<BufferedImage> {

  private final BufferedImage image;
  private final Graphics2D g2D;

  public BufferedImageG2DProvider(ImageInfo imageInfo) {
    image = new BufferedImage(
        imageInfo.w(),
        imageInfo.h(),
        BufferedImage.TYPE_3BYTE_BGR
    );
    g2D = image.createGraphics();
    g2D.setClip(new Rectangle2D.Double(0, 0, imageInfo.w(), imageInfo.h()));
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  @Override
  public Graphics2D g2D() {
    return g2D;
  }

  @Override
  public BufferedImage output() {
    return image;
  }
}
