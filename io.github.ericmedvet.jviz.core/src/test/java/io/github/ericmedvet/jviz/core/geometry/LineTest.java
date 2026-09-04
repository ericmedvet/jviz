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

class LineTest {

  @Test
  void containsPoint() {
    assertThat(Line.from(1, 1).contains(new Point(2, 3)))
        .as("y=x+1 contains (2;3)")
        .isTrue();
    assertThat(Line.from(new Point(1, 1), Math.PI / 4d).contains(new Point(2, 2)))
        .as("line at (1;1) with angle of pi/4 contains (2;2)")
        .isTrue();
  }

  @Test
  void intersectionWithLine() {
    assertThat(Line.from(1, 1).intersectionWith(Line.from(-1, 3)))
        .as("y=x+1 intersects y=-x+3")
        .isNotEmpty();
    assertThat(Line.from(1, 1).intersectionWith(Line.from(-1, 3)))
        .as("y=x+1 intersects y=-x+3 at (1;2)")
        .contains(new Point(1, 2));
    assertThat(new Line(0, 1, -3).intersectionWith(new Line(1, 0, -5)))
        .as("y=3 intersects x=5 at (5;3)")
        .contains(new Point(5, 3));
    assertThat(Line.from(2, 1).intersectionWith(Line.from(2, 0)))
        .as("y=2x+1 does not intersect y=2x")
        .isEmpty();
  }

  @Test
  void intersectionWithSegment() {
    assertThat(
        Line.from(1, 1)
            .intersectionWith(
                new Segment(
                    new Point(1, 0),
                    new Point(1, 3)

                )
            )
    )
        .as("y=x+1 intersects (1;0)->(1;3)")
        .isNotEmpty();
    assertThat(
        Line.from(1, 1)
            .intersectionWith(
                new Segment(
                    new Point(1, 0),
                    new Point(1, 3)

                )
            )
    )
        .as("y=x+1 intersects (1;0)->(1;3) at (1;2)")
        .contains(new Point(1, 2));
    assertThat(
        Line.from(1, 1)
            .intersectionWith(
                new Segment(
                    new Point(1, 0),
                    new Point(1, 2)

                )
            )
    )
        .as("y=x+1 intersects (1;0)->(1;2) at (1;2)")
        .contains(new Point(1, 2));
    assertThat(
        Line.from(1, 1)
            .intersectionWith(
                new Segment(
                    new Point(1, 0),
                    new Point(1, -3)

                )
            )
    )
        .as("y=x+1 does not intersects (1;0)->(1;-3)")
        .isEmpty();
  }

  @Test
  void intersectionWithSemiline() {
    assertThat(Line.from(1, 1).intersectionWith(new Semiline(new Point(1, 0), 0)))
        .as("y=x+1 does not intersects semiline (1;0)@0")
        .isEmpty();
    assertThat(Line.from(1, 1).intersectionWith(new Semiline(new Point(-1, 0), 0)))
        .as("y=x+1 intersects semiline (-1;0)@0")
        .isNotEmpty();
    assertThat(
        Line.from(1, 1)
            .intersectionWith(new Semiline(new Point(0, 3), -Math.PI / 4d))
            .orElseThrow()
            .distanceTo(new Point(1, 2))
    )
        .as("y=x+1 intersects semiline (0;3)@-pi/4 at (1;2)")
        .isCloseTo(0, within(1e-9));
  }
}
