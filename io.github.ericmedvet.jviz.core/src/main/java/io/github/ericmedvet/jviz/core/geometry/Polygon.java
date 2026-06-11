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

import java.util.List;
import java.util.stream.IntStream;

public interface Polygon {

  List<Point> vertexes();

  default List<Segment> sides() {
    return IntStream.range(0, vertexes().size())
        .mapToObj(
            i -> new Segment(
                vertexes().get(i),
                (i == vertexes().size() - 1) ? vertexes().getFirst() : vertexes().get(i + 1)
            )
        )
        .toList();
  }

  default Point center() {
    return new Point(
        vertexes().stream().mapToDouble(Point::x).average().orElseThrow(),
        vertexes().stream().mapToDouble(Point::y).average().orElseThrow()
    );
  }

  static Polygon from(List<Point> vertexes) {
    record HardPolygon(List<Point> vertexes) implements Polygon {

    }
    return new HardPolygon(vertexes);
  }

}