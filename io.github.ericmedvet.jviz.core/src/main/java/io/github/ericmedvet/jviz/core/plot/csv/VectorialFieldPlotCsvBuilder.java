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

import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldDataSeries.Point;
import io.github.ericmedvet.jviz.core.plot.VectorialFieldPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.plot.csv.Configuration.Mode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVPrinter;

public class VectorialFieldPlotCsvBuilder extends AbstractCsvBuilder<VectorialFieldPlot> {

  public VectorialFieldPlotCsvBuilder(Configuration c, Mode mode) {
    super(c, mode);
  }

  @Override
  public String apply(VectorialFieldPlot p) {
    StringWriter sw = new StringWriter();
    try (CSVPrinter csvPrinter = new CSVPrinter(sw, c.getCSVFormat())) {
      if (mode.equals(Mode.NORMAL)) {
        csvPrinter.printRecord(
            processRecord(List.of(p.xTitleName(), p.yTitleName(), "name", "srcX", "srcY", "dstX", "dstY")));
        for (XYPlot.TitledData<List<VectorialFieldDataSeries>> td :
            p.dataGrid().values()) {
          for (VectorialFieldDataSeries ds : td.data()) {
            for (Map.Entry<Point, Point> pp : ds.pointPairs().entrySet()) {
              csvPrinter.printRecord(processRecord(processRecord(List.of(
                  td.xTitle(),
                  td.yTitle(),
                  ds.name(),
                  pp.getKey().x(),
                  pp.getKey().y(),
                  pp.getValue().x(),
                  pp.getValue().y()))));
            }
          }
        }
      } else if (mode.equals(Mode.PAPER_FRIENDLY)) {
        // TODO fill
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sw.toString();
  }
}
