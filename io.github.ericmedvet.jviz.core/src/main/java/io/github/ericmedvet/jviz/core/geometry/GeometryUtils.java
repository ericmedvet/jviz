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
import io.github.ericmedvet.jviz.core.geometry.EntitiesDrawer.Configuration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeometryUtils {

  private GeometryUtils() {
  }

  public static Set<Polygon> voronoiTessellation(
      Set<Point> points,
      DoubleRange xRange,
      DoubleRange yRange
  ) {
    points = points.stream()
        .filter(p -> xRange.contains(p.x()) && yRange.contains(p.y()))
        .collect(
            Collectors.toSet()
        );
    if (points.isEmpty()) {
      return Set.of();
    }
    Rectangle domain = Rectangle.of(
        new Point(xRange.min(), yRange.min()),
        new Point(xRange.max(), yRange.max())
    );
    Set<Polygon> cells = new HashSet<>();
    for (Point p : points) {
      Polygon cell = domain;
      for (Point q : points) {
        if (p == q) {
          continue;
        }
        cell = clipPolygon(cell, new Segment(p, q).perpendicularBisector());
      }
      cells.add(cell);
    }
    return cells;
  }

  private static Polygon clipPolygon(Polygon polygon, Line l) {
    List<Point> clipped = new ArrayList<>();
    for (Segment side : polygon.sides()) {
      Point p1 = side.p1();
      Point p2 = side.p2();
      boolean p1Inside = (l.a() * p1.x() + l.b() * p1.y()) <= l.c();
      boolean p2Inside = (l.a() * p2.x() + l.b() * p2.y()) <= l.c();
      if (p1Inside && p2Inside) {
        clipped.add(p2);
      } else if (p1Inside) {
        clipped.add(getIntersection(p1, p2, l.a(), l.b(), l.c()));
      } else if (p2Inside) {
        clipped.add(getIntersection(p1, p2, l.a(), l.b(), l.c()));
        clipped.add(p2);
      }
    }
    return Polygon.of(clipped);
  }

  private static Point getIntersection(Point p1, Point p2, double A, double B, double C) {
    double dx = p2.x() - p1.x();
    double dy = p2.y() - p1.y();
    double t = (C - A * p1.x() - B * p1.y()) / (A * dx + B * dy);
    return new Point(p1.x() + t * dx, p1.y() + t * dy);
  }

  public static void main(String[] args) {
    DoubleRange xR = new DoubleRange(-0.4, 0.4);
    DoubleRange yR = new DoubleRange(-0.3, 0.35);
    RandomGenerator rg = RandomGenerator.getDefault();
    List<Entity> entities = new ArrayList<>();
    entities.add(new Rectangle(Point.ORIGIN, 2, 1));
    entities.add(new Rectangle(Point.ORIGIN, 1, 0.75));
    Set<Point> points = IntStream.range(0, 100)
        .mapToObj(
            _ -> new Point(
                xR.extend(1).denormalize(rg.nextDouble()),
                yR.denormalize(rg.nextDouble())
            )

        )
        .collect(Collectors.toSet());
    entities.addAll(points);
    entities.addAll(voronoiTessellation(points, xR, yR));
    new EntitiesDrawer(Configuration.DEFAULT).show(entities);
  }
}