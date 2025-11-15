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
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.accumulator.AggregatedXYDataSeriesMKPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.DistributionMRPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.EAggregatedXYDataSeriesMKPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.KAggregatedXYDataSeriesMKPAF;
import io.github.ericmedvet.jviz.core.plot.accumulator.ScatterMRPAF;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Discoverable(prefixTemplate = "viz.plot.multi|m")
public class MultiPlots {
  private MultiPlots() {
  }

  @SuppressWarnings("unused")
  public static <E, K, X> ScatterMRPAF<E, K, String, X> scatter(
      @Param("xSubplot") Function<? super K, String> xSubplotFunction,
      @Param("ySubplot") Function<? super K, String> ySubplotFunction,
      @Param("group") Function<? super K, String> groupFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.gtEq(t=1)") Predicate<X> condition,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
      @Param(value = "limitOneYForK", dB = true) boolean limitOneYForK
  ) {
    UnaryOperator<List<XYDataSeries.Point>> rFilter = limitOneYForK ? values -> List.of(
        values.getLast()
    ) : UnaryOperator
        .identity();
    return new ScatterMRPAF<>(
        xSubplotFunction,
        ySubplotFunction,
        groupFunction,
        xFunction,
        yFunction,
        predicateValueFunction,
        condition,
        rFilter,
        xRange,
        yRange
    );
  }

  @SuppressWarnings("unused")
  public static <E, R> AggregatedXYDataSeriesMKPAF<E, R, String> xy(
      @Param("xSubplot") Function<? super R, String> xSubplotFunction,
      @Param("ySubplot") Function<? super R, String> ySubplotFunction,
      @Param("line") Function<? super R, String> lineFunction,
      @Param("x") Function<?, ? extends Number> xFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param(value = "valueAggregator", dNPM = "f.median()") Function<List<Number>, Number> valueAggregator,
      @Param(value = "minAggregator", dNPM = "f.percentile(p=25)") Function<List<Number>, Number> minAggregator,
      @Param(value = "maxAggregator", dNPM = "f.percentile(p=75)") Function<List<Number>, Number> maxAggregator,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
      @Param(value = "limitOneYForK", dB = true) boolean limitOneYForK,
      @Param("useKForX") boolean useKForX
  ) {
    UnaryOperator<List<Number>> rFilter = limitOneYForK ? values -> List.of(values.getLast()) : UnaryOperator
        .identity();
    if (useKForX) {
      //noinspection unchecked
      return new KAggregatedXYDataSeriesMKPAF<>(
          xSubplotFunction,
          ySubplotFunction,
          lineFunction,
          yFunction,
          valueAggregator,
          minAggregator,
          maxAggregator,
          rFilter,
          xRange,
          yRange,
          (Function<? super R, ? extends Number>) xFunction
      );
    }
    //noinspection unchecked
    return new EAggregatedXYDataSeriesMKPAF<>(
        xSubplotFunction,
        ySubplotFunction,
        lineFunction,
        yFunction,
        valueAggregator,
        minAggregator,
        maxAggregator,
        rFilter,
        xRange,
        yRange,
        (Function<? super E, ? extends Number>) xFunction
    );
  }

  @SuppressWarnings("unused")
  public static <E, R, X> DistributionMRPAF<E, R, String, X> yBoxplot(
      @Param("xSubplot") Function<? super R, String> xSubplotFunction,
      @Param("ySubplot") Function<? super R, String> ySubplotFunction,
      @Param("box") Function<? super R, String> boxFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.gtEq(t=1)") Predicate<X> condition,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
      @Param(value = "limitOneYForK", dB = true) boolean limitOneYForK
  ) {
    UnaryOperator<List<Number>> rFilter = limitOneYForK ? values -> List.of(values.getLast()) : UnaryOperator
        .identity();
    return new DistributionMRPAF<>(
        xSubplotFunction,
        ySubplotFunction,
        boxFunction,
        yFunction,
        predicateValueFunction,
        condition,
        rFilter,
        yRange
    );
  }

}
