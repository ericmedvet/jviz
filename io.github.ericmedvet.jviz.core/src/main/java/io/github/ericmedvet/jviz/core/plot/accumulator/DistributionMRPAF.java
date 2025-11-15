/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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
import io.github.ericmedvet.jnb.datastructure.FormattedFunction;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot.Data;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class DistributionMRPAF<E, K, L, X> extends AbstractMultipleKPAF<E, DistributionPlot, K, List<Data>, L, Map<L, Map<K, List<Number>>>> {

  protected final Function<? super E, X> predicateValueFunction;
  private final Function<? super K, ? extends L> lineFunction;
  private final Function<? super E, ? extends Number> yFunction;
  private final Predicate<? super X> predicate;
  private final UnaryOperator<List<Number>> rFilter;
  private final DoubleRange yRange;

  public DistributionMRPAF(
      Function<? super K, ? extends L> xSubplotFunction,
      Function<? super K, ? extends L> ySubplotFunction,
      Function<? super K, ? extends L> lineFunction,
      Function<? super E, ? extends Number> yFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      UnaryOperator<List<Number>> rFilter,
      DoubleRange yRange
  ) {
    super(xSubplotFunction, ySubplotFunction);
    this.lineFunction = lineFunction;
    this.yFunction = yFunction;
    this.predicateValueFunction = predicateValueFunction;
    this.predicate = predicate;
    this.rFilter = rFilter;
    this.yRange = yRange;
  }

  @Override
  protected int size(Map<L, Map<K, List<Number>>> kMap) {
    return kMap.values()
        .stream()
        .mapToInt(
            rMap -> rMap.values()
                .stream()
                .mapToInt(List::size)
                .sum()
        )
        .sum();
  }

  @Override
  protected List<DistributionPlot.Data> buildData(L xL, L yL, Map<L, Map<K, List<Number>>> map) {
    return map.entrySet()
        .stream()
        .map(
            e -> new DistributionPlot.Data(
                FormattedFunction.format(lineFunction).formatted(e.getKey()),
                e.getValue()
                    .values()
                    .stream()
                    .map(rFilter)
                    .flatMap(Collection::stream)
                    .map(Number::doubleValue)
                    .toList()
            )
        )
        .toList();
  }

  @Override
  protected DistributionPlot buildPlot(Table<L, L, List<DistributionPlot.Data>> data) {
    Grid<XYPlot.TitledData<List<DistributionPlot.Data>>> grid = Grid.create(
        data.nColumns(),
        data.nRows(),
        (x, y) -> new XYPlot.TitledData<>(
            FormattedFunction.format(xSubplotFunction)
                .formatted(data.colIndexes().get(x)),
            FormattedFunction.format(ySubplotFunction)
                .formatted(data.rowIndexes().get(y)),
            data.get(x, y)
        )
    );
    String subtitle = "";
    if (grid.w() > 1 && grid.h() == 1) {
      subtitle = "→ %s".formatted(NamedFunction.name(xSubplotFunction));
    } else if (grid.w() == 1 && grid.h() > 1) {
      subtitle = "↓ %s".formatted(NamedFunction.name(ySubplotFunction));
    } else if (grid.w() > 1 && grid.h() > 1) {
      subtitle = "→ %s, ↓ %s".formatted(
          NamedFunction.name(xSubplotFunction),
          NamedFunction.name(ySubplotFunction)
      );
    }
    return new DistributionPlot(
        "%s distribution%s"
            .formatted(
                NamedFunction.name(yFunction),
                subtitle.isEmpty() ? subtitle : (" (%s)".formatted(subtitle))
            ),
        NamedFunction.name(xSubplotFunction),
        NamedFunction.name(ySubplotFunction),
        NamedFunction.name(lineFunction),
        NamedFunction.name(yFunction),
        yRange,
        grid
    );
  }

  @Override
  protected Map<L, Map<K, List<Number>>> init(L xL, L yL) {
    return new HashMap<>();
  }

  @Override
  protected Map<L, Map<K, List<Number>>> update(
      L xL,
      L yL,
      Map<L, Map<K, List<Number>>> map,
      E e,
      K k
  ) {
    X predicateValue = predicateValueFunction.apply(e);
    if (predicate.test(predicateValue)) {
      L lineL = lineFunction.apply(k);
      map.computeIfAbsent(lineL, l -> new HashMap<>())
          .computeIfAbsent(k, thisK -> new ArrayList<>())
          .add(yFunction.apply(e));
    }
    return map;
  }

  @Override
  public String toString() {
    return "distributionMRPAF(yFunction=" + yFunction + ')';
  }
}
