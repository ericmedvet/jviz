/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 Eric Medvet
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
package io.github.ericmedvet.jviz.core.plot;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import java.util.Map;

public interface VectorialFieldDataSeries {

  record Point(double x, double y) {

    public double norm() {
      return Math.sqrt(x * x + y * y);
    }

    @Override
    public String toString() {
      return "(%s;%s)".formatted(x, y);
    }
  }

  String name();

  Map<Point, Point> pointPairs();

  static VectorialFieldDataSeries of(String name, Map<Point, Point> pointPairs) {
    record HardVectorialFieldDataSeries(
        String name, Map<Point, Point> pointPairs
    ) implements VectorialFieldDataSeries {}
    return new HardVectorialFieldDataSeries(name, pointPairs);
  }

  default DoubleRange destinationMagnitudeRange() {
    return new DoubleRange(
        pointPairs().values().stream().mapToDouble(Point::norm).min().orElseThrow(),
        pointPairs().values().stream().mapToDouble(Point::norm).max().orElseThrow()
    );
  }

  default DoubleRange destinationXRange() {
    return new DoubleRange(
        pointPairs().values().stream().mapToDouble(p -> p.x).min().orElseThrow(),
        pointPairs().values().stream().mapToDouble(p -> p.x).max().orElseThrow()
    );
  }

  default DoubleRange destinationYRange() {
    return new DoubleRange(
        pointPairs().values().stream().mapToDouble(p -> p.y).min().orElseThrow(),
        pointPairs().values().stream().mapToDouble(p -> p.y).max().orElseThrow()
    );
  }

  default DoubleRange originXRange() {
    return new DoubleRange(
        pointPairs().keySet().stream().mapToDouble(p -> p.x).min().orElseThrow(),
        pointPairs().keySet().stream().mapToDouble(p -> p.x).max().orElseThrow()
    );
  }

  default DoubleRange originYRange() {
    return new DoubleRange(
        pointPairs().keySet().stream().mapToDouble(p -> p.y).min().orElseThrow(),
        pointPairs().keySet().stream().mapToDouble(p -> p.y).max().orElseThrow()
    );
  }
}
