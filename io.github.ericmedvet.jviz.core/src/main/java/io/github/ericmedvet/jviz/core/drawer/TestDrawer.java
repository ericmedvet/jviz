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

import io.github.ericmedvet.jviz.core.util.GraphicsUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

public class TestDrawer implements Drawer<Color> {

  private final static double MARGIN_RATE = 0.1;
  private final static double STRETCH_RATE = 0.33;
  private final static double STROKE_W = 3;
  private final static double ALPHA = 0.25;
  private final static Color TEXT_COLOR = Color.BLACK;

  @Override
  public void draw(Graphics2D g, Color color) {
    Rectangle clipBounds = g.getClipBounds();
    double w = clipBounds.getWidth();
    double h = clipBounds.getHeight();
    AffineTransform initialTransform = (AffineTransform) g.getTransform().clone();
    // border
    g.setStroke(new BasicStroke((float) STROKE_W));
    g.setColor(color);
    g.draw(clipBounds);
    // scale and translate
    g.translate(
        w * MARGIN_RATE / 2d,
        h * MARGIN_RATE / 2d
    );
    g.scale(1d - MARGIN_RATE, 1d - MARGIN_RATE);
    g.setColor(GraphicsUtils.alphaed(color, ALPHA));
    // stretched x
    g.setClip(new Rectangle2D.Double(w * STRETCH_RATE, 0, w * (1 - 2 * STRETCH_RATE), h));
    g.fill(new Rectangle2D.Double(0, 0, w, h));
    // stretched y
    g.setClip(new Rectangle2D.Double(0, h * STRETCH_RATE, w, h * (1 - 2 * STRETCH_RATE)));
    g.fill(new Rectangle2D.Double(0, 0, w, h));
    // draw text
    g.setTransform(initialTransform);
    g.setClip(clipBounds);
    g.setColor(TEXT_COLOR);
    g.drawString(
        "(%03d,%03d,%03d)".formatted(color.getRed(), color.getGreen(), color.getBlue()),
        (float) clipBounds.getX(),
        (float) clipBounds.getCenterY()
    );
  }

  @Override
  public ImageInfo imageInfo(Color color) {
    return new ImageInfo(300, 200);
  }

  public static void main(String[] args) throws IOException {
    Arrangement a = Arrangement.HORIZONTAL;
    List<Color> colors = List.of(Color.RED, Color.GREEN.darker(), Color.BLUE, Color.ORANGE.darker().darker().darker());
    new TestDrawer().show(Color.RED);
    new TestDrawer().multi(a).show(colors);
  }
}
