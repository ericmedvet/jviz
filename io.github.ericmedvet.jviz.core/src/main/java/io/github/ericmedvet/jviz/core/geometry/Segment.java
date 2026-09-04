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

import java.util.Optional;

public record Segment(Point p1, Point p2) implements BoundedEntity {

  @Override
  public Rectangle boundingBox() {
    return new Rectangle(center(), Math.abs(p1.x() - p2.x()), Math.abs(p1.y() - p2.y()));
  }

  public Point center() {
    return new Point(p1.x() / 2d + p2.x() / 2d, p1.y() / 2d + p2.y() / 2d);
  }

  public double direction() {
    return p2.diff(p1).direction();
  }

  public Optional<Point> intersectionWith(Segment other) {
    Optional<Point> oP = Line.from(this).intersectionWith(other);
    if (oP.isEmpty()) {
      return oP;
    }
    Point p = oP.orElseThrow();
    if (!boundingBox().contains(p)) {
      return Optional.empty();
    }
    return oP;
  }

  public boolean intersects(Segment other) {
    return intersectionWith(other).isEmpty();
  }

  public Optional<Point> intersectionWith(Segment other, double precision) {
    final double thisDeltaX = this.p1.x() - this.p2.x();
    final double otherDeltaX = other.p1.x() - other.p2.x();
    final double thisDeltaY = this.p1.y() - this.p2.y();
    final double otherDeltaY = other.p1.y() - other.p2.y();
    double denominator = thisDeltaX * otherDeltaY - thisDeltaY * otherDeltaX;
    // if denominator is 0, the lines are parallel or coincident
    if (denominator == 0) {
      return Optional.empty();
    }
    double px = ((this.p1.x() * this.p2.y() - this.p1.y() * this.p2.x()) * otherDeltaX - thisDeltaX * (other.p1
        .x() * other.p2.y() - other.p1.y() * other.p2.x())) / denominator;
    double py = ((this.p1.x() * this.p2.y() - this.p1.y() * this.p2.x()) * otherDeltaY - thisDeltaY * (other.p1
        .x() * other.p2.y() - other.p1.y() * other.p2.x())) / denominator;
    Point intersection = new Point(px, py);
    // check if the intersection point lies on both segments
    if (isPointInBoundingBox(intersection, precision) && other.isPointInBoundingBox(
        intersection,
        precision
    )) {
      return Optional.of(intersection);
    }
    return Optional.empty();
  }

  public boolean isPointInBoundingBox(Point point, double precision) {
    return point.x() >= Math.min(this.p1.x(), this.p2.x()) - precision / 2 && point.x() <= Math.max(
        this.p1.x(),
        this.p2.x()
    ) + precision / 2 && point.y() >= Math.min(this.p1.y(), this.p2.y()) - precision / 2 && point.y() <= Math.max(
        this.p1.y(),
        this.p2.y()
    ) + precision / 2;
  }

  public double length() {
    return p1.distanceTo(p2);
  }

  public Line perpendicularBisector() {
    double a = p2.x() - p1.x();
    double b = p2.y() - p1.y();
    double c = (Math.pow(p2.x(), 2) - Math.pow(p1.x(), 2) + Math.pow(p2.y(), 2) - Math.pow(p1.y(), 2)) / 2d;
    return new Line(a, b, c);
  }

  @Override
  public String toString() {
    return "s(%s->%s)".formatted(p1, p2);
  }
}
