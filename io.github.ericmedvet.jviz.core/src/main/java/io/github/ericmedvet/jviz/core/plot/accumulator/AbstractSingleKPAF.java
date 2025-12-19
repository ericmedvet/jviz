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
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractSingleKPAF<E, P extends XYPlot<D>, K, D> implements PlotAccumulatorFactory<E, P, K, D> {

  protected final Function<? super K, String> titleFunction;

  public AbstractSingleKPAF(Function<? super K, String> titleFunction) {
    this.titleFunction = titleFunction;
  }

  protected abstract D buildData(List<E> es, K k);

  protected abstract P buildPlot(D data, K k);

  @Override
  public Accumulator<E, P> build(K k) {
    List<E> es = new ArrayList<>();
    return new Accumulator<>() {
      @Override
      public P get() {
        synchronized (es) {
          return buildPlot(buildData(es, k), k);
        }
      }

      @Override
      public void listen(E e) {
        synchronized (es) {
          es.add(e);
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
