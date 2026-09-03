/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 - 2026 Eric Medvet
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
package io.github.ericmedvet.jviz.core.geometry;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.plot.image.Axis;
import io.github.ericmedvet.jviz.core.plot.image.PlotUtils.GMetrics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class EntitiesDrawer implements Drawer<Collection<? extends Entity>> {

  public record Configuration(
      Color foregroundColor,
      Color axesColor,
      Color labelsColor,
      double strokeSizeRate,
      double pointSizeRate
      // TODO: add margin rate
  ) {

    public static final Configuration DEFAULT = new Configuration(
        Color.BLACK,
        Color.GRAY,
        Color.GRAY,
        0.0025,
        0.01
    );
  }

  private static final Logger L = Logger.getLogger(EntitiesDrawer.class.getName());
  private final static DoubleRange DEFAULT_X_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static DoubleRange DEFAULT_Y_RANGE = DoubleRange.SYMMETRIC_UNIT;
  private final static Rectangle DEFAULT_BOUNDING_BOX = new Rectangle(
      new Point(DEFAULT_X_RANGE.center(), DEFAULT_Y_RANGE.center()),
      DEFAULT_X_RANGE.extent(),
      DEFAULT_Y_RANGE.extent()
  );
  private final Configuration configuration;

  public EntitiesDrawer(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public ImageInfo imageInfo(Collection<? extends Entity> entities) {
    Rectangle r = boundingBox(entities);
    return new ImageInfo(DEFAULT_W, (int) Math.round(DEFAULT_W * r.height() / r.width()));
  }

  @Override
  public void draw(Graphics2D g, Collection<? extends Entity> entities) {
    GMetrics gm = new GMetrics(g);
    Rectangle entitiesR = boundingBox(entities);
    Drawer.clean(g);
    Rectangle2D r = g.getClip().getBounds2D();
    Axis xA = new Axis(entitiesR.xRange(), List.of(), List.of());
    Axis yA = new Axis(entitiesR.yRange(), List.of(), List.of());
    // TODO draw axes
    g.setColor(configuration.foregroundColor);
    for (Entity e : entities) {
      switch (e) {
        case Point p -> drawPoint(g, gm, r, xA, yA, p);
        case Polygon p -> drawPolygon(g, gm, r, xA, yA, p);
        case Segment s -> drawSegment(g, gm, r, xA, yA, s);
        case Circle c -> drawCircle(g, gm, r, xA, yA, c);
        // TODO add line and semiline
        default -> L.warning("Cannot draw %s %s".formatted(e.getClass().getSimpleName(), e));
      }
    }
  }

  private void drawPoint(Graphics2D g, GMetrics gm, Rectangle2D r, Axis xA, Axis yA, Point p) {
    g.setStroke(new BasicStroke());
    double size = gm.refL() * configuration.pointSizeRate;
    g.fill(new Double(xA.xIn(p.x(), r) - size / 2d, yA.yIn(p.y(), r) - size / 2d, size, size));
  }

  private void drawPolygon(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Axis xA,
      Axis yA,
      Polygon poly
  ) {
    g.setStroke(new BasicStroke((float) (configuration.strokeSizeRate() * gm.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(
        xA.xIn(poly.vertexes().getFirst().x(), r),
        yA.yIn(poly.vertexes().getFirst().y(), r)
    );
    poly.vertexes().stream().skip(1).forEach(p -> path.lineTo(xA.xIn(p.x(), r), yA.yIn(p.y(), r)));
    path.closePath();
    g.draw(path);
  }

  private void drawSegment(
      Graphics2D g,
      GMetrics gm,
      Rectangle2D r,
      Axis xA,
      Axis yA,
      Segment s
  ) {
    g.setStroke(new BasicStroke((float) (configuration.strokeSizeRate() * gm.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(xA.xIn(s.p1().x(), r), yA.yIn(s.p1().y(), r));
    path.lineTo(xA.xIn(s.p2().x(), r), yA.yIn(s.p2().y(), r));
    g.draw(path);
  }

  private void drawCircle(Graphics2D g, GMetrics gm, Rectangle2D r, Axis xA, Axis yA, Circle c) {
    g.setStroke(new BasicStroke((float) (configuration.strokeSizeRate() * gm.refL())));
    double d = xA.xIn(c.center().x() + c.radius(), r) - xA.xIn(c.center().x() - c.radius(), r);
    g.draw(
        new Double(xA.xIn(c.center().x(), r) - d / 2d, yA.yIn(c.center().y(), r) - d / 2d, d, d)
    );
  }

  private static Rectangle boundingBox(Collection<? extends Entity> entities) {
    if (entities.isEmpty()) {
      return DEFAULT_BOUNDING_BOX;
    }
    Rectangle r = entities.stream()
        .filter(e -> e instanceof BoundedEntity)
        .map(be -> ((BoundedEntity) be).boundingBox())
        .reduce(
            (r1, r2) -> Rectangle.of(
                new Point(
                    Math.min(r1.min().x(), r2.min().x()),
                    Math.min(r1.min().y(), r2.min().y())
                ),
                new Point(
                    Math.max(r1.max().x(), r2.max().x()),
                    Math.max(r1.max().y(), r2.max().y())
                )
            )
        )
        .orElse(DEFAULT_BOUNDING_BOX);
    if (r.width() == 0) {
      r = new Rectangle(r.center(), DEFAULT_X_RANGE.extent(), r.height());
    }
    if (r.height() == 0) {
      r = new Rectangle(r.center(), r.width(), DEFAULT_Y_RANGE.extent());
    }
    return r;
  }
}