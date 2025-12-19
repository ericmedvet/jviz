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
import io.github.ericmedvet.jviz.core.plot.LandscapePlot;
import io.github.ericmedvet.jviz.core.plot.Value;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public class LandscapeSEPAF<E, K, X, P> extends AbstractSingleEPAF<E, LandscapePlot, K, LandscapePlot.Data, X> {
  private final List<Function<? super E, Collection<P>>> pointFunctions;
  private final Function<? super P, ? extends Number> xFunction;
  private final Function<? super P, ? extends Number> yFunction;
  private final Function<E, DoubleBinaryOperator> valueFunction;
  private final DoubleRange xRange;
  private final DoubleRange yRange;
  private final DoubleRange valueRange;

  public LandscapeSEPAF(
      Function<? super K, String> titleFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      List<Function<? super E, Collection<P>>> pointFunctions,
      Function<? super P, ? extends Number> xFunction,
      Function<? super P, ? extends Number> yFunction,
      Function<E, DoubleBinaryOperator> valueFunction,
      DoubleRange xRange,
      DoubleRange yRange,
      DoubleRange valueRange
  ) {
    super(titleFunction, predicateValueFunction, predicate, unique);
    this.pointFunctions = pointFunctions;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.valueFunction = valueFunction;
    this.xRange = xRange;
    this.yRange = yRange;
    this.valueRange = valueRange;
  }

  @Override
  protected List<Map.Entry<String, LandscapePlot.Data>> buildData(E e, K k) {
    return List.of(
        Map.entry(
            "",
            new LandscapePlot.Data(
                valueFunction.apply(e),
                pointFunctions.stream()
                    .map(
                        pf -> XYDataSeries.of(
                            NamedFunction.name(pf),
                            pf.apply(e)
                                .stream()
                                .map(
                                    p -> new XYDataSeries.Point(
                                        Value.of(
                                            xFunction
                                                .apply(p)
                                                .doubleValue()
                                        ),
                                        Value.of(
                                            yFunction
                                                .apply(p)
                                                .doubleValue()
                                        )
                                    )
                                )
                                .toList()
                        )
                            .sorted()
                    )
                    .toList()
            )
        )
    );
  }

  @Override
  protected LandscapePlot buildPlot(Table<String, String, LandscapePlot.Data> data, K k) {
    List<String> colIndexes = new ArrayList<>(data.colIndexes());
    List<String> rowIndexes = new ArrayList<>(data.rowIndexes());
    return new LandscapePlot(
        titleFunction.apply(k),
        NamedFunction.name(predicateValueFunction),
        "",
        NamedFunction.name(xFunction),
        NamedFunction.name(yFunction),
        xRange,
        yRange,
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
    return "landscapeSEPAF(xFunction=" + xFunction + ";yFunction=" + yFunction + ')';
  }
}
