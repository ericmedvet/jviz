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
package io.github.ericmedvet.jviz.core.drawer;

import io.github.ericmedvet.jviz.core.util.VideoUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author "Eric Medvet" on 2024/08/14 for jviz
 */
public final class Video {
  private final List<BufferedImage> images;
  private final double frameRate;
  private transient byte[] data;

  public Video(List<BufferedImage> images, double frameRate) {
    this.images = images;
    this.frameRate = frameRate;
    data = null;
  }

  public byte[] data(VideoUtils.EncoderFacility encoder) {
    if (data == null) {
      try {
        data = VideoUtils.encode(images, frameRate, encoder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return data;
  }

  public byte[] data() {
    return data(VideoUtils.defaultEncoder());
  }

  public double frameRate() {
    return frameRate;
  }

  @Override
  public int hashCode() {
    return Objects.hash(images, frameRate);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (Video) obj;
    return Objects.equals(this.images, that.images)
        && Double.doubleToLongBits(this.frameRate) == Double.doubleToLongBits(that.frameRate);
  }

  @Override
  public String toString() {
    return "(%dx%d)x%d@%.1ffps"
        .formatted(images.get(0).getWidth(), images.get(0).getHeight(), images.size(), frameRate);
  }

  public List<BufferedImage> images() {
    return images;
  }
}
