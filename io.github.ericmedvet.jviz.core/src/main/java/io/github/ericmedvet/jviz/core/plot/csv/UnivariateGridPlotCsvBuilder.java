/*-
 * ========================LICENSE_START=================================
 * jviz-core
 * %%
 * Copyright (C) 2024 Eric Medvet
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
package io.github.ericmedvet.jviz.core.plot.csv;

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Grid.Key;
import io.github.ericmedvet.jnb.datastructure.HashMapTable;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVPrinter;

public class UnivariateGridPlotCsvBuilder extends AbstractCsvBuilder<UnivariateGridPlot> {

  public UnivariateGridPlotCsvBuilder(Configuration c, Mode mode) {
    super(c, mode);
  }

  @Override
  public String apply(UnivariateGridPlot p) {
    StringWriter sw = new StringWriter();
    try (CSVPrinter csvPrinter = new CSVPrinter(sw, c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(processRecord(List.of(p.xTitleName(), p.yTitleName(), "x", "y", "v")));
        for (XYPlot.TitledData<Grid<Double>> td : p.dataGrid().values()) {
          for (Grid.Entry<Double> e : td.data()) {
            if (e.value() != null) {
              csvPrinter.printRecord(
                  processRecord(
                      processRecord(
                          List.of(
                              td.xTitle(),
                              td.yTitle(),
                              e.key().x(),
                              e.key().y(),
                              e.value()
                          )
                      )
                  )
              );
            }
          }
        }
      } else if (mode.equals(Mode.PAPER_FRIENDLY)) {
        Table<Key, String, Number> t = new HashMapTable<>();
        for (XYPlot.TitledData<Grid<Double>> td : p.dataGrid().values()) {
          for (Grid.Entry<Double> e : td.data()) {
            t.set(e.key(), String.join(c.columnNameJoiner(), List.of(td.xTitle(), td.yTitle())), e.value());
          }
        }
        csvPrinter.printRecord(
            processRecord(
                Stream.concat(Stream.of("x", "y"), t.colIndexes().stream())
                    .toList()
            )
        );
        for (Grid.Key k : t.rowIndexes()) {
          csvPrinter.printRecord(
              processRecord(
                  Stream.concat(Stream.of(k.x(), k.y()), t.rowValues(k).stream())
                      .toList()
              )
          );
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sw.toString();
  }
}
