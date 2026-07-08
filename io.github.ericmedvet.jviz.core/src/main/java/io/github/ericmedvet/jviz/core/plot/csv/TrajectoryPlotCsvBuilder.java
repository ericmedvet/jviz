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
package io.github.ericmedvet.jviz.core.plot.csv;

import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot.Data.ReducedPoint;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVPrinter;

public class TrajectoryPlotCsvBuilder extends AbstractCsvBuilder<TrajectoryPlot> {

  public TrajectoryPlotCsvBuilder(Configuration c, Mode mode) {
    super(c, mode);
  }

  @Override
  public String apply(TrajectoryPlot p) {
    StringWriter sw = new StringWriter();
    try (CSVPrinter csvPrinter = new CSVPrinter(sw, c.getCSVFormat())) {
      csvPrinter.printRecord(
          processRecord(
              List.of(
                  p.xTitleName(),
                  p.yTitleName(),
                  "name",
                  "t",
                  "x",
                  "y",
                  "original"
              )
          )
      );
      for (XYPlot.TitledData<List<TrajectoryPlot.Data>> td : p.dataGrid().values()) {
        for (TrajectoryPlot.Data d : td.data()) {
          for (Map.Entry<Double, ReducedPoint> e : d.points().entrySet()) {
            csvPrinter.printRecord(
                processRecord(
                    List.of(
                        td.xTitle(),
                        td.yTitle(),
                        d.name(),
                        e.getKey(),
                        e.getValue().x(),
                        e.getValue().y(),
                        Arrays.stream(e.getValue().original())
                            .mapToObj(Double::toString)
                            .collect(Collectors.joining(" "))
                    )
                )
            );
          }
        }
      }
    } catch (
      IOException e) {
      throw new RuntimeException(e);
    }
    return sw.toString();
  }
}
