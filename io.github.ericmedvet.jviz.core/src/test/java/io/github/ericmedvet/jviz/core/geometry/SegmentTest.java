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
 * You may obtain a copy of the License at
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

class SegmentTest {

  @Test
  void center() {
    assertThat(new Segment(new Point(0, 0), new Point(2, 1)).center())
        .as("center of (0;0)->(2;1) is (1;0.5)")
        .isEqualTo(new Point(1, 0.5));
  }

  @Test
  void direction() {
    assertThat(new Segment(new Point(0, 0), new Point(2, 2)).direction())
        .as("direction of (0;0)->(2;2) is ~pi/4")
        .isCloseTo(Math.PI / 4d, within(1e-9));
    assertThat(new Segment(new Point(1, 1), new Point(2, 1)).direction())
        .as("direction of (1;1)->(2;1) is ~0")
        .isCloseTo(0, within(1e-9));
    assertThat(new Segment(new Point(2, 1), new Point(2, 2)).direction())
        .as("direction of (2;1)->(2;2) is ~pi/2")
        .isCloseTo(Math.PI / 2d, within(1e-9));
  }

  @Test
  void intersectionWith() {
    Point P00 = Point.ORIGIN;
    Point P10 = new Point(1, 0);
    Point P01 = new Point(0, 1);
    Point P11 = new Point(1, 1);
    Point P32 = new Point(3, 2);
    Point P99 = new Point(9, 9);
    Point P28 = new Point(2, 8);
    Point P37 = new Point(3, 7);
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P10, P01)))
        .as("%s->%s intersects %s->%s", P00, P11, P10, P01)
        .isNotEmpty();
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P10, P01)))
        .as("%s->%s intersects %s->%s at (0.5;0.5)", P00, P11, P10, P01)
        .contains(new Point(0.5, 0.5));
    assertThat(new Segment(P00, P11).intersectionWith(new Segment(P32, P32.sum(P11))))
        .as("%s->%s does not intersect %s->%s", P00, P11, P32, P32.sum(P11))
        .isEmpty();
    assertThat(new Segment(P01, P99).intersectionWith(new Segment(P28, P37)))
        .as("%s->%s does not intersect %s->%s", P01, P99, P28, P37)
        .isEmpty();
    assertThat(new Segment(P28, P37).intersectionWith(new Segment(P01, P99)))
        .as("%s->%s does not intersect %s->%s", P37, P28, P01, P99)
        .isEmpty();
  }

  @Test
  void perpendicularBisector() {
    Point P10 = new Point(1, 0);
    Point P01 = new Point(0, 1);
    assertThat(new Segment(P10, P01).perpendicularBisector().slope())
        .as("bisector of %s->%s has slope=1")
        .isCloseTo(1d, within(1e-9));
    assertThat(
        new Segment(P10, P01).perpendicularBisector().intersectionWith(new Segment(P10, P01))
    )
        .as("bisector of %s->%s intersects it at center")
        .contains(new Segment(P10, P01).center());
  }
}