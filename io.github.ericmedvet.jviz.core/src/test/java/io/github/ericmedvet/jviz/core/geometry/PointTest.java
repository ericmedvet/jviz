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
/*
 * Copyright 2026 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain direction copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jviz.core.geometry;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PointTest {

  private static final Point P00 = Point.ORIGIN;
  private static final Point P10 = new Point(1, 0);
  private static final Point P01 = new Point(0, 1);
  private static final Point P11 = new Point(1, 1);
  private static final Point P32 = new Point(3, 2);

  @Test
  void boundingBox() {
    assertThat(P11.boundingBox().contains(P11))
        .as("bounding box contains point")
        .isTrue();
    assertThat(P11.boundingBox().width())
        .as("bounding box width is 0")
        .isEqualTo(0);
    assertThat(P11.boundingBox().height())
        .as("bounding box height is 0")
        .isEqualTo(0);
  }

  @Test
  void diff() {
    assertThat(P11.diff(P11))
        .as("point minus self is origin")
        .isEqualTo(Point.ORIGIN);
    assertThat(P32.diff(P11))
        .as("(3;2) - (1;1) is (2;1)")
        .isEqualTo(new Point(2, 1));
  }

  @Test
  void direction() {
    assertThat(P11.direction())
        .as("direction of (1;1) is pi/4")
        .isEqualTo(Math.PI / 4);
    assertThat(P10.direction())
        .as("direction of (1;0) is 0")
        .isEqualTo(0);
    assertThat(P01.direction())
        .as("direction of (0;1) is pi/2")
        .isEqualTo(Math.PI / 2);
    assertThat(new Point(-5, -5).direction())
        .as("direction of (-5;-5) is -3/4*pi")
        .isEqualTo(-Math.PI * 3d / 4d);
  }

  @Test
  void pointDistance() {
    assertThat(P11.distanceTo(P00))
        .as("(1;1) to (0;0) is sqrt(2)")
        .isEqualTo(Math.sqrt(2));
    assertThat(P32.distanceTo(P11))
        .as("distance is commutative")
        .isEqualTo(P11.distanceTo(P32));
  }

  @Test
  void lineDistance() {
    assertThat(P11.distanceTo(Line.from(P00, P01)))
        .as("(1;1) dist from line (0;0)->(0;1) is 1")
        .isEqualTo(1d);
    assertThat(P11.distanceTo(Line.from(P01, P00)))
        .as("(1;1) dist from line (0;1)->(0;0) is 1")
        .isEqualTo(1d);
    assertThat(P11.distanceTo(Line.from(P01, P10)))
        .as("(1;1) dist from line (0;1)->(1;0) is ~sqrt(2)/2")
        .isCloseTo(Math.sqrt(2d) / 2d, within(1e-9));
  }

  @Test
  void segmentDistance() {
    assertThat(P11.distanceTo(new Segment(P00, P01)))
        .as("(1;1) dist from segment (0;0)->(0;1) is 1")
        .isEqualTo(1d);
    assertThat(P11.distanceTo(new Segment(P01, P00)))
        .as("(1;1) dist from segment (0;1)->(0;0) is 1")
        .isEqualTo(1d);
    assertThat(P11.distanceTo(new Segment(P01, P10)))
        .as("(1;1) dist from segment (0;1)->(1;0) is ~sqrt(2)/2")
        .isCloseTo(Math.sqrt(2d) / 2d, within(1e-9));
    assertThat(new Point(2, 1).distanceTo(new Segment(P00, P10)))
        .as("(2;1) dist from segment (0;0)->(1;0) is ~sqrt(2)")
        .isEqualTo(Math.sqrt(2));
    assertThat(new Point(2, 1).distanceTo(new Segment(P10, P00)))
        .as("(2;1) dist from segment (1;0)->(0;0) is ~sqrt(2)")
        .isEqualTo(Math.sqrt(2));
    assertThat(new Point(2, 1).distanceTo(new Segment(P10, P00)))
        .as("(2;1) dist from segment (1;0)->(0;0) is ~sqrt(2)")
        .isEqualTo(Math.sqrt(2));
  }

  @Test
  void rotate() {
    assertThat(P01.rotate(-Math.PI / 2d).distanceTo(P10))
        .as("(0;1) rotated by -pi/2 is close to (1;0)")
        .isCloseTo(0, within(1e-9));
    assertThat(P32.rotate(-Math.PI / 7d).magnitude())
        .as("magnitude does not change with rotation")
        .isCloseTo(P32.magnitude(), within(1e-9));
    assertThat(P32.rotate(2d * Math.PI).distanceTo(P32))
        .as("rotate by 2*pi is self ")
        .isCloseTo(0, within(1e-9));
    assertThat(P32.rotate(Math.PI / 7d).direction())
        .as("rotate increments direction")
        .isCloseTo(P32.direction() + Math.PI / 7d, within(1e-9));
    assertThat(P32.rotate(-Math.PI / 7d).direction())
        .as("rotate decrements direction")
        .isCloseTo(P32.direction() - Math.PI / 7d, within(1e-9));
  }

  @Test
  void rotateWithCenter() {
    assertThat(new Point(2, 1).rotate(P11, -Math.PI / 2d).distanceTo(P10))
        .as("(2;1) rotated wrt (1;1) by -pi/2 is close to (1;0)")
        .isCloseTo(0, within(1e-9));
  }

  @Test
  void scale() {
    assertThat(P32.scale(3d).magnitude())
        .as("magnitude scales")
        .isCloseTo(3d * P32.magnitude(), within(1e-9));
    assertThat(P32.scale(3d).direction())
        .as("direction does not changes")
        .isCloseTo(P32.direction(), within(1e-9));
  }

}
