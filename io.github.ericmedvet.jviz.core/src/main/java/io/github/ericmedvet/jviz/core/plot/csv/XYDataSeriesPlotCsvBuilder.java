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
package io.github.ericmedvet.jviz.core.plot.csv;

import io.github.ericmedvet.jnb.datastructure.HashMapTable;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.RangedValue;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVPrinter;

public class XYDataSeriesPlotCsvBuilder extends AbstractCsvBuilder<XYDataSeriesPlot> {

  public XYDataSeriesPlotCsvBuilder(Configuration c, Mode mode) {
    super(c, mode);
  }

  @Override
  public String apply(XYDataSeriesPlot p) {
    StringWriter sw = new StringWriter();
    try (CSVPrinter csvPrinter = new CSVPrinter(sw, c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(
            processRecord(
                List.of(
                    p.xTitleName(),
                    p.yTitleName(),
                    "series",
                    String.join(c.columnNameJoiner(), p.xName(), "min"),
                    p.xName(),
                    String.join(c.columnNameJoiner(), p.xName(), "max"),
                    String.join(c.columnNameJoiner(), p.yName(), "min"),
                    p.yName(),
                    String.join(c.columnNameJoiner(), p.yName(), "max")
                )
            )
        );
        for (XYPlot.TitledData<List<XYDataSeries>> td : p.dataGrid().values()) {
          for (XYDataSeries ds : td.data()) {
            for (XYDataSeries.Point point : ds.points()) {
              csvPrinter.printRecord(
                  processRecord(
                      List.of(
                          td.xTitle(),
                          td.yTitle(),
                          ds.name(),
                          RangedValue.range(point.x()).min(),
                          point.x().v(),
                          RangedValue.range(point.x()).max(),
                          RangedValue.range(point.y()).min(),
                          point.y().v(),
                          RangedValue.range(point.y()).max()
                      )
                  )
              );
            }
          }
        }
      } else if (mode.equals(Mode.PAPER_FRIENDLY)) {
        Table<Number, String, Number> t = new HashMapTable<>();
        for (XYPlot.TitledData<List<XYDataSeries>> td : p.dataGrid().values()) {
          for (XYDataSeries ds : td.data()) {
            for (XYDataSeries.Point point : ds.points()) {
              t.set(
                  point.x().v(),
                  String.join(
                      c.columnNameJoiner(),
                      List.of(td.xTitle(), td.yTitle(), ds.name(), p.yName())
                  ),
                  point.y().v()
              );
              t.set(
                  point.x().v(),
                  String.join(
                      c.columnNameJoiner(),
                      List.of(td.xTitle(), td.yTitle(), ds.name(), p.yName(), "min")
                  ),
                  RangedValue.range(point.y()).min()
              );
              t.set(
                  point.x().v(),
                  String.join(
                      c.columnNameJoiner(),
                      List.of(td.xTitle(), td.yTitle(), ds.name(), p.yName(), "max")
                  ),
                  RangedValue.range(point.y()).max()
              );
            }
          }
        }
        csvPrinter.printRecord(
            processRecord(
                Stream.concat(Stream.of(p.xName()), t.colIndexes().stream())
                    .toList()
            )
        );
        for (Number x : t.rowIndexes()) {
          csvPrinter.printRecord(
              processRecord(
                  Stream.concat(Stream.of(x), t.rowValues(x).stream()).toList()
              )
          );
        }
      }
      return sw.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
