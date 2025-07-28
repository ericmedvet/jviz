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
package io.github.ericmedvet.jviz.core.plot;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import java.util.function.BiFunction;

public interface RangedGrid<T> extends Grid<T> {
  DoubleRange xRange();

  DoubleRange yRange();

  String xName();

  String yName();

  default DoubleRange xRange(int x) {
    return new DoubleRange(
        xRange().denormalize(new DoubleRange(0, w()).normalize(x)),
        xRange().denormalize(new DoubleRange(0, w()).normalize(x + 1))
    );
  }

  default DoubleRange yRange(int y) {
    return new DoubleRange(
        yRange().denormalize(new DoubleRange(0, h()).normalize(y)),
        yRange().denormalize(new DoubleRange(0, h()).normalize(y + 1))
    );
  }

  static <T> RangedGrid<T> from(Grid<T> grid, DoubleRange xRange, DoubleRange yRange, String xName, String yName) {
    return new RangedGrid<>() {
      @Override
      public DoubleRange xRange() {
        return xRange;
      }

      @Override
      public DoubleRange yRange() {
        return yRange;
      }

      @Override
      public String xName() {
        return xName;
      }

      @Override
      public String yName() {
        return yName;
      }

      @Override
      public T get(Key key) {
        return grid.get(key);
      }

      @Override
      public int h() {
        return grid.h();
      }

      @Override
      public void set(Key key, T t) {
        grid.set(key, t);
      }

      @Override
      public int w() {
        return grid.w();
      }
    };
  }

  @Override
  default <S> Grid<S> map(BiFunction<Key, T, S> function) {
    return RangedGrid.from(Grid.super.map(function), xRange(), yRange(), xName(), yName());
  }
}
