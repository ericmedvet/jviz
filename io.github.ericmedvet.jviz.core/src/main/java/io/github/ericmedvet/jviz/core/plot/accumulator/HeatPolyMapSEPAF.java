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
package io.github.ericmedvet.jviz.core.plot.accumulator;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import io.github.ericmedvet.jviz.core.plot.HeatPolyMapPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HeatPolyMapSEPAF<E, K, X, V> extends AbstractSingleEPAF<E, HeatPolyMapPlot, K, Map<Polygon, Double>, X> {

  private final List<Function<? super E, Map<Polygon, V>>> mapFunctions;
  private final List<Function<? super V, ? extends Number>> mapValueFunctions;
  private final DoubleRange valueRange;

  public HeatPolyMapSEPAF(
      Function<? super K, String> titleFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      List<Function<? super E, Map<Polygon, V>>> mapFunctions,
      List<Function<? super V, ? extends Number>> mapValueFunctions,
      DoubleRange valueRange
  ) {
    super(titleFunction, predicateValueFunction, predicate, unique);
    this.mapFunctions = mapFunctions;
    this.mapValueFunctions = mapValueFunctions;
    this.valueRange = valueRange;
  }

  @Override
  protected List<Entry<String, Map<Polygon, Double>>> buildData(E e, K k) {
    return mapFunctions.stream()
        .flatMap(mf -> {
          Map<Polygon, V> vMap = mf.apply(e);
          return mapValueFunctions.stream()
              .map(
                  mvf -> Map.entry(
                      mapFunctions.size() == 1 ? NamedFunction.name(mvf) : "%s on %s".formatted(
                          NamedFunction.name(mvf),
                          NamedFunction.name(mf)
                      ),
                      vMap.entrySet()
                          .stream()
                          .collect(
                              Collectors.toMap(
                                  Entry::getKey,
                                  entry -> Optional
                                      .ofNullable(entry.getValue())
                                      .map(v -> mvf.apply(v).doubleValue())
                                      .orElse(Double.NaN)
                              )
                          )
                  )
              );
        })
        .toList();
  }

  @Override
  protected HeatPolyMapPlot buildPlot(Table<String, String, Map<Polygon, Double>> data, K k) {
    List<String> colIndexes = new ArrayList<>(data.colIndexes());
    List<String> rowIndexes = new ArrayList<>(data.rowIndexes());
    return new HeatPolyMapPlot(
        titleFunction.apply(k),
        NamedFunction.name(predicateValueFunction),
        "value",
        "x",
        "y",
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
    return "heatPolyMapSEPAF(mapValueFunctions=" + mapValueFunctions + ')';
  }
}
