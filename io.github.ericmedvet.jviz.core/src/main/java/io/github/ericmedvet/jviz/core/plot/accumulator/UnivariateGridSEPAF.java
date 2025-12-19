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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.RangedGrid;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class UnivariateGridSEPAF<E, K, X, G> extends AbstractSingleEPAF<E, UnivariateGridPlot, K, Grid<Double>, X> {
  private final List<Function<? super E, Grid<G>>> gridFunctions;
  private final List<Function<? super G, ? extends Number>> gridValueFunctions;
  private final DoubleRange valueRange;

  public UnivariateGridSEPAF(
      Function<? super K, String> titleFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      List<Function<? super E, Grid<G>>> gridFunctions,
      List<Function<? super G, ? extends Number>> gridValueFunctions,
      DoubleRange valueRange
  ) {
    super(titleFunction, predicateValueFunction, predicate, unique);
    this.gridFunctions = gridFunctions;
    this.gridValueFunctions = gridValueFunctions;
    this.valueRange = valueRange;
  }

  @Override
  protected List<Map.Entry<String, Grid<Double>>> buildData(E e, K k) {
    return gridFunctions.stream()
        .map(gf -> {
          Grid<G> grid = gf.apply(e);
          return gridValueFunctions.stream()
              .map(
                  gvf -> Map.entry(
                      gridFunctions.size() == 1 ? NamedFunction.name(gvf) : "%s on %s".formatted(
                          NamedFunction.name(gvf),
                          NamedFunction.name(gf)
                      ),
                      grid.map(g -> Objects.isNull(g) ? null : gvf.apply(g).doubleValue())
                  )
              )
              .toList();
        })
        .flatMap(List::stream)
        .toList();
  }

  @Override
  protected UnivariateGridPlot buildPlot(Table<String, String, Grid<Double>> data, K k) {
    List<String> colIndexes = new ArrayList<>(data.colIndexes());
    List<String> rowIndexes = new ArrayList<>(data.rowIndexes());
    return new UnivariateGridPlot(
        titleFunction.apply(k),
        NamedFunction.name(predicateValueFunction),
        "value",
        data.get(0, 0) instanceof RangedGrid<?> rg ? rg.xName() : "x",
        data.get(0, 0) instanceof RangedGrid<?> rg ? rg.yName() : "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        valueRange,
        Grid.create(
            data.nOfColumns(),
            data.nOfRows(),
            (x, y) -> new XYPlot.TitledData<>(
                colIndexes.get(x),
                rowIndexes.get(y),
                data.get(x, y)
            )
        )
    );
  }

  @Override
  public String toString() {
    return "gridSEPAF(gridValueFunctions=" + gridValueFunctions + ')';
  }
}
