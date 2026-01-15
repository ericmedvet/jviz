/*
 * Copyright 2026 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jviz.core.plot;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import smile.manifold.TSNE;

public record TrajectoryPlot(
    String title,
    String xTitleName,
    String yTitleName,
    String xName,
    String yName,
    DoubleRange xRange,
    DoubleRange yRange,
    Grid<TitledData<List<Data>>> dataGrid
) implements XYPlot<List<Data>> {

  public record Data(String name, SortedMap<Double, ReducedPoint> points) {

    public final static int UMAP_K = 10;

    private static int checkSize(SequencedMap<Double, double[]> points, int expectedSize) {
      if (points.isEmpty()) {
        return 0;
      }
      List<Integer> sizes = points.values().stream().map(p -> p.length).distinct().toList();
      if (sizes.size() > 1) {
        throw new IllegalArgumentException(
            "Non homogeneous size of points: found sizes %s".formatted(
                sizes.stream()
                    .map("%d"::formatted)
                    .collect(
                        Collectors.joining(", ")
                    )
            )
        );
      }
      if (expectedSize > 0 && sizes.getFirst() != expectedSize) {
        throw new IllegalArgumentException(
            "Wrong points size: %d found, %d expected".formatted(sizes.getFirst(), expectedSize)
        );
      }
      return sizes.getFirst();
    }

    public static List<Data> from(
        Map<String, SortedMap<Double, double[]>> seriesMap,
        ReductionType reductionType
    ) {
      if (seriesMap.isEmpty()) {
        return List.of();
      }
      SequencedMap<String, SortedMap<Double, double[]>> sequencedSeriesMap = new TreeMap<>(
          seriesMap
      );
      List<String> names = seriesMap.keySet().stream().toList();
      int p = checkSize(seriesMap.get(names.getFirst()), -1);
      for (int i = 1; i < names.size(); i = i + 1) {
        checkSize(seriesMap.get(names.get(i)), p);
      }
      return switch (reductionType) {
        case IDENTITY -> fromIdentity(sequencedSeriesMap);
        case PCA -> fromPCA(sequencedSeriesMap);
        case TSNE -> split(TSNE.fit(getData(sequencedSeriesMap)).coordinates(), sequencedSeriesMap);
      };
    }

    public static Data fromIdentity(String name, SortedMap<Double, double[]> points) {
      checkSize(points, 2);
      return new Data(
          name,
          points.entrySet()
              .stream()
              .collect(
                  Collectors.toMap(
                      Entry::getKey,
                      e -> new ReducedPoint(
                          e.getValue()[0],
                          e.getValue()[1],
                          e.getValue()
                      ),
                      (p1, p2) -> p2,
                      TreeMap::new
                  )
              )
      );
    }

    public static List<Data> fromIdentity(Map<String, SortedMap<Double, double[]>> seriesMap) {
      return seriesMap.entrySet()
          .stream()
          .sorted(Entry.comparingByKey())
          .map(e -> fromIdentity(e.getKey(), e.getValue()))
          .toList();
    }

    public static List<Data> fromPCA(SequencedMap<String, SortedMap<Double, double[]>> seriesMap) {
      RealMatrix matrix = MatrixUtils.createRealMatrix(getData(new TreeMap<>(seriesMap)));
      int n = matrix.getRowDimension();
      int p = matrix.getColumnDimension();
      RealMatrix centeredMatrix = new Array2DRowRealMatrix(n, p);
      for (int col = 0; col < p; col++) {
        double[] column = matrix.getColumn(col);
        double mean = StatUtils.mean(column);
        for (int row = 0; row < n; row++) {
          centeredMatrix.setEntry(row, col, matrix.getEntry(row, col) - mean);
        }
      }
      RealMatrix covM = new Covariance(centeredMatrix).getCovarianceMatrix();
      EigenDecomposition ed = new EigenDecomposition(covM);
      RealVector pc1 = ed.getEigenvector(0);
      RealVector pc2 = ed.getEigenvector(1);
      RealMatrix projectionMatrix = new Array2DRowRealMatrix(p, 2);
      projectionMatrix.setColumnVector(0, pc1);
      projectionMatrix.setColumnVector(1, pc2);
      return split(centeredMatrix.multiply(projectionMatrix).getData(), seriesMap);
    }

    private static double[][] getData(
        SequencedMap<String, SortedMap<Double, double[]>> seriesMap
    ) {
      return seriesMap.values()
          .stream()
          .flatMap(series -> series.values().stream())
          .toArray(double[][]::new);
    }

    private static List<Data> split(
        double[][] reduced,
        SequencedMap<String, SortedMap<Double, double[]>> seriesMap
    ) {
      List<Data> datas = new ArrayList<>(seriesMap.size());
      int c = 0;
      for (String name : seriesMap.keySet()) {
        SortedMap<Double, ReducedPoint> reducedPoints = new TreeMap<>();
        for (Map.Entry<Double, double[]> seriesEntry : seriesMap.get(name).entrySet()) {
          reducedPoints.put(
              seriesEntry.getKey(),
              new ReducedPoint(reduced[c], seriesEntry.getValue())
          );
          c = c + 1;
        }
        datas.add(new Data(name, reducedPoints));
      }
      return datas;
    }

    public enum ReductionType { IDENTITY, PCA, TSNE }

    public record ReducedPoint(double x, double y, double[] original) {

      public ReducedPoint(double[] reduced, double[] original) {
        this(reduced[0], reduced[1], original);
      }

      @Override
      public String toString() {
        return "(%+.1f;%+.1f)[%d]".formatted(x, y, original.length);
      }
    }
  }
}