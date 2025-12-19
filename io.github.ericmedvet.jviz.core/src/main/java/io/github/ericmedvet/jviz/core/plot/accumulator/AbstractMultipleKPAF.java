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
package io.github.ericmedvet.jviz.core.plot.accumulator;

import io.github.ericmedvet.jnb.datastructure.Accumulator;
import io.github.ericmedvet.jnb.datastructure.HashMapTable;
import io.github.ericmedvet.jnb.datastructure.Sized;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.function.Function;

public abstract class AbstractMultipleKPAF<E, P extends XYPlot<D>, K, D, L, V> implements PlotAccumulatorFactory<E, P, K, D>, Sized {

  protected final Function<? super K, ? extends L> xSubplotFunction;
  protected final Function<? super K, ? extends L> ySubplotFunction;

  private final Table<L, L, V> table;

  public AbstractMultipleKPAF(
      Function<? super K, ? extends L> xSubplotFunction,
      Function<? super K, ? extends L> ySubplotFunction
  ) {
    this.xSubplotFunction = xSubplotFunction;
    this.ySubplotFunction = ySubplotFunction;
    table = new HashMapTable<>();
  }

  protected abstract D buildData(L xL, L yL, V v);

  protected abstract P buildPlot(Table<L, L, D> data);

  protected abstract V init(L xL, L yL);

  protected abstract V update(L xL, L yL, V v, E e, K k);

  protected abstract int size(V v);

  @Override
  public int size() {
    return table.values().stream().mapToInt(this::size).sum();
  }

  @Override
  public Accumulator<E, P> build(K k) {
    L xL = xSubplotFunction.apply(k);
    L yL = ySubplotFunction.apply(k);
    return new Accumulator<>() {
      @Override
      public P get() {
        synchronized (table) {
          return buildPlot(table.mapValues((xL, yL, v) -> buildData(xL, yL, v == null ? init(xL, yL) : v)));
        }
      }

      @Override
      public void listen(E e) {
        synchronized (table) {
          V v = table.get(yL, xL);
          if (v == null) {
            v = init(xL, yL);
          }
          table.set(yL, xL, update(xL, yL, v, e, k));
        }
      }

      @Override
      public String toString() {
        return name();
      }
    };
  }

  private String name() {
    return toString();
  }
}
