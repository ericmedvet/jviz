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
package io.github.ericmedvet.jviz.core.util;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Misc {

  private static final Logger L = Logger.getLogger(Misc.class.getName());

  private Misc() {
  }

  public static File checkExistenceAndChangeName(File file) {
    String originalFileName = file.getPath();
    while (file.exists()) {
      String newName = null;
      Matcher mNum = Pattern.compile("\\((?<n>[0-9]+)\\)\\.\\w+$").matcher(file.getPath());
      if (mNum.find()) {
        int n = Integer.parseInt(mNum.group("n"));
        newName = new StringBuilder(file.getPath())
            .replace(mNum.start("n"), mNum.end("n"), Integer.toString(n + 1))
            .toString();
      }
      Matcher mExtension = Pattern.compile("\\.\\w+$").matcher(file.getPath());
      if (newName == null && mExtension.find()) {
        newName = new StringBuilder(file.getPath())
            .replace(mExtension.start(), mExtension.end(), ".(1)" + mExtension.group())
            .toString();
      }
      if (newName == null) {
        newName = file.getPath() + ".newer";
      }
      file = new File(newName);
    }
    if (!file.getPath().equals(originalFileName)) {
      L.log(
          Level.WARNING,
          String.format("Given file name (%s) exists; will write on %s", originalFileName, file.getPath())
      );
    }
    return file;
  }

  public static double median(double... values) {
    if (values.length == 1) {
      return values[0];
    }
    double[] vs = Arrays.copyOf(values, values.length);
    Arrays.sort(vs);
    if (vs.length % 2 == 0) {
      return (vs[values.length / 2 - 1] + vs[values.length / 2]) / 2d;
    }
    return vs[values.length / 2];
  }

  public static <K> K median(Collection<K> ks, Comparator<? super K> comparator) {
    List<K> all = new ArrayList<>(ks);
    all.sort(comparator);
    return all.get(all.size() / 2);
  }

  public static <K> K percentile(Collection<K> ks, Comparator<? super K> comparator, double p) {
    List<K> collection = ks.stream().sorted(comparator).toList();
    int i = (int) Math.max(Math.min(((double) collection.size()) * p, collection.size() - 1), 0);
    return collection.get(i);
  }

  public static void showImage(BufferedImage image) {
    EventQueue.invokeLater(() -> {
      JFrame frame = new JFrame("Image");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.add(new JPanel() {
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Dimension d = getSize();
          Graphics2D g2d = (Graphics2D) g.create();
          g2d.drawImage(image, (d.width - image.getWidth()) / 2, (d.height - image.getHeight()) / 2, this);
          g2d.dispose();
        }

        public Dimension getPreferredSize() {
          return new Dimension(image.getWidth(), image.getHeight());
        }
      });
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
