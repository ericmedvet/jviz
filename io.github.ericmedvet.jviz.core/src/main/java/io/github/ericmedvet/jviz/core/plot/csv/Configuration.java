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

import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;

public record Configuration(
    String columnNameJoiner,
    String doubleFormat,
    String delimiter,
    List<Replacement> replacements,
    String missingDataString) {
  public static final Map<Mode, Configuration> DEFAULTS = Map.ofEntries(
      Map.entry(Mode.NORMAL, new Configuration(".", "%f", ";", List.of(), "")),
      Map.entry(
          Mode.PAPER_FRIENDLY,
          new Configuration(".", "%.3e", "\t", List.of(new Configuration.Replacement("\\W+", ".")), "nan")));

  public CSVFormat getCSVFormat() {
    return CSVFormat.DEFAULT.builder().setDelimiter(delimiter).build();
  }

  public enum Mode {
    NORMAL,
    PAPER_FRIENDLY
  }

  public record Replacement(String regex, String replacement) {}
}
