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
package io.github.ericmedvet.jviz.core.plot;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;

public interface RangedValue extends Value {
  DoubleRange range();

  static RangedValue of(double v, double min, double max) {
    record HardRangedValue(double v, DoubleRange range) implements RangedValue {
      @Override
      public String toString() {
        return "%f[%f;%f]".formatted(v, this.range.min(), this.range.max());
      }
    }
    return new HardRangedValue(v, new DoubleRange(min, max));
  }

  static DoubleRange range(Value v) {
    if (v instanceof RangedValue rv) {
      return rv.range();
    }
    return new DoubleRange(v.v(), v.v());
  }
}
