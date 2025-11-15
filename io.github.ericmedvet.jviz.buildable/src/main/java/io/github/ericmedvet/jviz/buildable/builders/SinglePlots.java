/*-
 * ========================LICENSE_START=================================
 * jviz-buildable
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
package io.github.ericmedvet.jviz.buildable.builders;

import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.plot.accumulator.UnivariateGridSEPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.VectorialFieldSEPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSEPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSRPAF;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "viz.plot.single|s")
public class SinglePlots {
  private SinglePlots() {
  }

  @SuppressWarnings("unused")
  public static <E, K, X, F> VectorialFieldSEPAF<E, K, X, F> field(
      @Param("title") Function<? super K, String> titleFunction,
      @Param("fields") List<Function<? super E, F>> fieldFunctions,
      @Param("pointPairs") List<Function<? super F, ? extends Map<List<Double>, List<Double>>>> pointPairsFunctions,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "unique", dB = true) boolean unique
  ) {
    return new VectorialFieldSEPAF<>(
        titleFunction,
        predicateValueFunction,
        condition,
        unique,
        fieldFunctions,
        pointPairsFunctions
    );
  }

  @SuppressWarnings("unused")
  public static <E, K, X, G> UnivariateGridSEPAF<E, K, X, G> grid(
      @Param("title") Function<? super K, String> titleFunction,
      @Param("values") List<Function<? super G, ? extends Number>> valueFunctions,
      @Param("grids") List<Function<? super E, Grid<G>>> gridFunctions,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange valueRange,
      @Param(value = "unique", dB = true) boolean unique
  ) {
    return new UnivariateGridSEPAF<>(
        titleFunction,
        predicateValueFunction,
        condition,
        unique,
        gridFunctions,
        valueFunctions,
        valueRange
    );
  }

  public static <E, K, X, P> XYDataSeriesSEPAF<E, K, X, P> xyes(
      @Param("title") Function<? super K, String> titleFunction,
      @Param("points") List<Function<? super E, Collection<P>>> pointFunctions,
      @Param("x") Function<? super P, ? extends Number> xFunction,
      @Param("y") Function<? super P, ? extends Number> yFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "unique", dB = true) boolean unique,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange
  ) {
    return new XYDataSeriesSEPAF<>(
        titleFunction,
        predicateValueFunction,
        condition,
        unique,
        pointFunctions,
        xFunction,
        yFunction,
        xRange,
        yRange
    );
  }

  @SuppressWarnings("unused")
  public static <E, K> XYDataSeriesSRPAF<E, K> xyrs(
      @Param("title") Function<? super K, String> titleFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("ys") List<Function<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange
  ) {
    return new XYDataSeriesSRPAF<>(titleFunction, xFunction, yFunctions, xRange, yRange, true, false);
  }
}
