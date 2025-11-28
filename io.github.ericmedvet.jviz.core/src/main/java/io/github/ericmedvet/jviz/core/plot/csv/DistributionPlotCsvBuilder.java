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
import io.github.ericmedvet.jviz.core.plot.DistributionPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.csv.CSVPrinter;

public class DistributionPlotCsvBuilder extends AbstractCsvBuilder<DistributionPlot> {

  public DistributionPlotCsvBuilder(Configuration c, Mode mode) {
    super(c, mode);
  }

  @Override
  public String apply(DistributionPlot p) {
    StringWriter sw = new StringWriter();
    try (CSVPrinter csvPrinter = new CSVPrinter(sw, c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(
            processRecord(
                List.of(
                    p.xTitleName(),
                    p.yTitleName(),
                    p.xName(),
                    String.join(c.columnNameJoiner(), p.yName(), "min"),
                    String.join(c.columnNameJoiner(), p.yName(), "q1minus15IQR"),
                    String.join(c.columnNameJoiner(), p.yName(), "q1"),
                    String.join(c.columnNameJoiner(), p.yName(), "mean"),
                    String.join(c.columnNameJoiner(), p.yName(), "median"),
                    String.join(c.columnNameJoiner(), p.yName(), "q3"),
                    String.join(c.columnNameJoiner(), p.yName(), "q3plus15IQR"),
                    String.join(c.columnNameJoiner(), p.yName(), "max")
                )
            )
        );
        for (XYPlot.TitledData<List<DistributionPlot.Data>> td : p.dataGrid().values()) {
          for (DistributionPlot.Data ds : td.data()) {
            csvPrinter.printRecord(
                processRecord(
                    List.of(
                        td.xTitle(),
                        td.yTitle(),
                        ds.name(),
                        ds.stats().min(),
                        ds.stats().q1minus15IQR(),
                        ds.stats().q1(),
                        ds.stats().mean(),
                        ds.stats().median(),
                        ds.stats().q3(),
                        ds.stats().q3plus15IQR(),
                        ds.stats().max()
                    )
                )
            );
          }
        }
      } else if (mode.equals(Mode.PAPER_FRIENDLY)) {
        Table<Integer, String, Number> t = new HashMapTable<>();
        for (XYPlot.TitledData<List<DistributionPlot.Data>> td : p.dataGrid().values()) {
          for (DistributionPlot.Data ds : td.data()) {
            for (int i = 0; i < ds.yValues().size(); i++) {
              t.set(
                  i,
                  String.join(c.columnNameJoiner(), List.of(td.xTitle(), td.yTitle(), ds.name())),
                  ds.yValues().get(i)
              );
            }
          }
        }
        csvPrinter.printRecord(processRecord(t.colIndexes().stream().toList()));
        for (int i : t.rowIndexes()) {
          csvPrinter.printRecord(processRecord(t.rowValues(i)));
        }
      }
      return sw.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}