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
package io.github.ericmedvet.jviz.core.plot;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jviz.core.plot.XYPlotDrawer.AnchorH;
import io.github.ericmedvet.jviz.core.plot.XYPlotDrawer.AnchorV;
import io.github.ericmedvet.jviz.core.plot.XYPlotDrawer.Marker;
import io.github.ericmedvet.jviz.core.plot.image.Axis;
import io.github.ericmedvet.jviz.core.plot.image.ColorRange;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.Configuration.Text.Use;
import io.github.ericmedvet.jviz.core.plot.image.Layout;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class PlotUtils {

  private PlotUtils() {}

  public static Map<Use, Font> fonts(Graphics2D g, Configuration c) {
    GMetrics gm = new GMetrics(g);
    return Arrays.stream(Configuration.Text.Use.values())
        .collect(Collectors.toMap(
            u -> u,
            u -> new Font(c.text().fontName(), Font.PLAIN, (int) Math.round(gm.refL
                * c.text().sizeRates().getOrDefault(u, c.text().fontSizeRate())))));
  }

  public static double computeStringH(Graphics2D g, Configuration c, Configuration.Text.Use fontUse) {
    g.setFont(fonts(g, c).get(fontUse));
    return g.getFontMetrics().getHeight();
  }

  public static double computeStringW(Graphics2D g, Configuration c, String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts(g, c).get(fontUse));
    return g.getFontMetrics().stringWidth(s);
  }

  public static void markRectangle(Graphics2D g, Configuration c, Rectangle2D r) {
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
  }

  public static Point2D center(Rectangle2D r) {
    return new Point2D.Double(r.getCenterX(), r.getCenterY());
  }

  public static void drawBoxAndWhiskers(
      Graphics2D g,
      Configuration c,
      GMetrics gm,
      Rectangle2D r,
      Color color,
      double innerBottom,
      double center,
      double innerTop,
      double alpha,
      double whiskersWRate,
      double strokeSizeRate) {
    markRectangle(g, c, r);
    // fill box
    g.setColor(GraphicsUtils.alphaed(color, alpha));
    g.fill(new Rectangle2D.Double(r.getX(), innerBottom, r.getWidth(), innerTop - innerBottom));
    // draw
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (strokeSizeRate * gm.refL)));
    g.draw(new Rectangle2D.Double(r.getX(), innerBottom, r.getWidth(), innerTop - innerBottom));
    g.draw(new Line2D.Double(r.getX(), center, r.getMaxX(), center));
    g.draw(new Line2D.Double(r.getCenterX(), innerBottom, r.getCenterX(), r.getY()));
    g.draw(new Line2D.Double(r.getCenterX(), innerTop, r.getCenterX(), r.getMaxY()));
    g.draw(new Line2D.Double(
        r.getCenterX() - r.getWidth() * whiskersWRate / 2d,
        r.getY(),
        r.getCenterX() + r.getWidth() * whiskersWRate / 2d,
        r.getY()));
    g.draw(new Line2D.Double(
        r.getCenterX() - r.getWidth() * whiskersWRate / 2d,
        r.getMaxY(),
        r.getCenterX() + r.getWidth() * whiskersWRate / 2d,
        r.getMaxY()));
  }

  public static void drawColorBar(
      Graphics2D g,
      Configuration c,
      GMetrics gm,
      Rectangle2D r,
      DoubleRange outerRange,
      DoubleRange innerRange,
      ColorRange colorRange,
      double h,
      int steps,
      Configuration.Text.Use use,
      Color labelColor,
      AnchorV labelsAnchor) {
    Shape clip = g.getClip();
    markRectangle(g, c, r);
    // background
    double barY = labelsAnchor.equals(AnchorV.B) ? r.getY() : (r.getMaxY() - h);
    double labelsY = labelsAnchor.equals(AnchorV.B) ? (r.getMaxY() - computeStringH(g, c, use)) : r.getY();
    Rectangle2D barR = new Rectangle2D.Double(r.getX(), barY, r.getWidth(), h);
    g.setColor(c.colors().plotBgColor());
    g.fill(barR);
    // color bar
    g.setClip(barR);
    DoubleRange rRange = new DoubleRange(r.getX(), r.getMaxX());
    double step = outerRange.extent() / (double) steps;
    DoubleStream.iterate(outerRange.min(), v -> v < outerRange.max(), v -> v + step)
        .filter(v -> v + step > innerRange.min())
        .filter(v -> v < innerRange.max())
        .forEach(v -> {
          g.setColor(colorRange.interpolate(outerRange.normalize(v)));
          double rMin = rRange.denormalize(outerRange.normalize(innerRange.clip(v)));
          double rMax = rRange.denormalize(outerRange.normalize(innerRange.clip(v + step)));
          g.fill(new Rectangle2D.Double(rMin, barY, rMax - rMin, h));
        });
    // border
    g.setClip(clip);
    g.setStroke(new BasicStroke((float) (c.general().borderStrokeSizeRate() * gm.refL)));
    g.setColor(c.colors().plotBorderColor());
    g.draw(barR);
    // labels
    String format = computeTicksFormat(c, List.of(innerRange.min(), innerRange.max()));
    drawString(
        g,
        c,
        new Point2D.Double(rRange.denormalize(outerRange.normalize(innerRange.min())), labelsY),
        format.formatted(innerRange.min()),
        AnchorH.C,
        AnchorV.B,
        use,
        Configuration.Text.Direction.H,
        labelColor);
    drawString(
        g,
        c,
        new Point2D.Double(rRange.denormalize(outerRange.normalize(innerRange.max())), labelsY),
        format.formatted(innerRange.max()),
        AnchorH.C,
        AnchorV.B,
        use,
        Configuration.Text.Direction.H,
        labelColor);
  }

  public static void drawMarker(
      Graphics2D g,
      Point2D p,
      double size,
      XYPlotDrawer.Marker marker,
      Color color,
      double alpha,
      double strokeSize) {
    double l = size / 2d;
    g.setStroke(new BasicStroke((float) strokeSize));
    if (marker.equals(XYPlotDrawer.Marker.CIRCLE) || marker.equals(XYPlotDrawer.Marker.SQUARE)) {
      Shape s =
          switch (marker) {
            case CIRCLE -> new Ellipse2D.Double(p.getX() - l, p.getY() - l, size, size);
            case SQUARE -> new Rectangle2D.Double(p.getX() - l, p.getY() - l, size, size);
            default -> throw new IllegalArgumentException();
          };
      g.setColor(GraphicsUtils.alphaed(color, alpha));
      g.fill(s);
      g.setColor(color);
      g.draw(s);
    } else if (marker.equals(XYPlotDrawer.Marker.PLUS)) {
      g.setColor(color);
      g.draw(new Line2D.Double(p.getX(), p.getY() - l, p.getX(), p.getY() + l));
      g.draw(new Line2D.Double(p.getX() - l, p.getY(), p.getX() + l, p.getY()));
    } else if (marker.equals(Marker.TIMES)) {
      g.setColor(color);
      g.draw(new Line2D.Double(p.getX() - l, p.getY() - l, p.getX() + l, p.getY() + l));
      g.draw(new Line2D.Double(p.getX() - l, p.getY() + l, p.getX() + l, p.getY() - l));
    }
  }

  public static void drawYAxis(Graphics2D g, Configuration c, Rectangle2D r, String name, Axis a) {
    drawString(
        g,
        c,
        new Point2D.Double(r.getX(), r.getCenterY()),
        name,
        AnchorH.L,
        AnchorV.C,
        Configuration.Text.Use.AXIS_LABEL,
        Configuration.Text.Direction.V,
        c.colors().axisLabelColor());
    IntStream.range(0, a.ticks().size())
        .forEach(i -> drawString(
            g,
            c,
            new Point2D.Double(
                r.getX() + r.getWidth(), a.yIn(a.ticks().get(i), r)),
            a.labels().get(i),
            AnchorH.R,
            AnchorV.C,
            Configuration.Text.Use.TICK_LABEL,
            Configuration.Text.Direction.H,
            c.colors().tickLabelColor()));
  }

  public static DoubleRange enlarge(DoubleRange range, double r) {
    return new DoubleRange(
        range.min() - range.extent() * (r - 1d) / 2d, range.max() + range.extent() * (r - 1d) / 2d);
  }

  public static SortedMap<String, Color> computeSeriesDataColors(List<String> names, List<Color> colors) {
    names = names.stream().distinct().sorted(String::compareTo).toList();
    return new TreeMap<>(IntStream.range(0, names.size())
        .boxed()
        .collect(Collectors.toMap(names::get, i -> colors.get(i % colors.size()))));
  }

  public static void drawItemsLegend(
      Graphics2D g,
      Configuration c,
      Rectangle2D r,
      Map<String, Color> items,
      double legendImageW,
      double legendImageH,
      LegendImageDrawer legendImageDrawer) {
    GMetrics gm = new GMetrics(g);
    double legendW =
        computeItemsLegendSize(g, c, items, legendImageW, legendImageH).getX();
    r = new Rectangle2D.Double(r.getX() + (r.getWidth() - legendW) / 2d, r.getY(), legendW, r.getHeight());
    markRectangle(g, c, r);
    double lineH = Math.max(legendImageH, computeStringH(g, c, Configuration.Text.Use.LEGEND_LABEL));
    double x = 0;
    double y = 0;
    for (Map.Entry<String, Color> e : items.entrySet()) {
      double localL = legendImageW
          + c.layout().legendInnerMarginWRate() * gm.w
          + c.layout().legendItemsGapWRate() * gm.w
          + computeStringW(g, c, e.getKey(), Configuration.Text.Use.LEGEND_LABEL);
      if (x + localL > r.getWidth()) {
        y = y + c.layout().legendInnerMarginHRate() * gm.h + lineH;
        x = 0;
      }
      Rectangle2D legendImageR = new Rectangle2D.Double(r.getX() + x, r.getY() + y, legendImageW, legendImageH);
      g.setColor(c.colors().plotBgColor());
      g.fill(legendImageR);
      legendImageDrawer.draw(g, legendImageR, e.getValue());
      drawString(
          g,
          c,
          new Point2D.Double(
              r.getX() + x + legendImageR.getWidth() + c.layout().legendInnerMarginWRate() * gm.w,
              r.getY() + y + lineH / 2d),
          e.getKey(),
          AnchorH.L,
          AnchorV.C,
          Configuration.Text.Use.LEGEND_LABEL,
          Configuration.Text.Direction.H,
          c.colors().legendLabelColor());
      x = x + localL;
    }
  }

  public static Point2D computeItemsLegendSize(
      Graphics2D g, Configuration c, Map<String, Color> items, double legendImageW, double legendImageH) {
    GMetrics gm = new GMetrics(g);
    double lineH = Math.max(legendImageH, computeStringH(g, c, Configuration.Text.Use.LEGEND_LABEL));
    double lH = lineH;
    double lineL = 0;
    List<Double> lineLs = new ArrayList<>();
    for (String s : items.keySet()) {
      double localL = legendImageW
          + c.layout().legendInnerMarginWRate() * gm.w
          + c.layout().legendItemsGapWRate() * gm.w
          + computeStringW(g, c, s, Configuration.Text.Use.LEGEND_LABEL);
      if (lineL + localL > gm.w) {
        lineLs.add(lineL);
        lH = lH + c.layout().legendInnerMarginHRate() * gm.h + lineH;
        lineL = 0;
      }
      lineL = lineL + localL;
    }
    lineLs.add(lineL);
    return new Point2D.Double(lineLs.stream().max(Double::compareTo).orElse(0d), lH);
  }

  public static String computeTicksFormat(Configuration c, List<Double> ticks) {
    ticks = ticks.stream().distinct().toList();
    int nOfDigits = 0;
    while (nOfDigits < c.general().maxNOfDecimalDigits()) {
      final int d = nOfDigits;
      long nOfDistinct =
          ticks.stream().map(("%." + d + "f")::formatted).distinct().count();
      if (nOfDistinct == ticks.size()) {
        break;
      }
      nOfDigits = nOfDigits + 1;
    }
    return "%." + nOfDigits + "f";
  }

  public static void drawXAxis(Graphics2D g, Configuration c, Rectangle2D r, String name, Axis a) {
    drawString(
        g,
        c,
        new Point2D.Double(r.getCenterX(), r.getY() + r.getHeight()),
        name,
        AnchorH.C,
        AnchorV.T,
        Configuration.Text.Use.AXIS_LABEL,
        Configuration.Text.Direction.H,
        c.colors().axisLabelColor());
    IntStream.range(0, a.ticks().size())
        .forEach(i -> drawString(
            g,
            c,
            new Point2D.Double(a.xIn(a.ticks().get(i), r), r.getY()),
            a.labels().get(i),
            AnchorH.C,
            AnchorV.B,
            Configuration.Text.Use.TICK_LABEL,
            Configuration.Text.Direction.V,
            c.colors().tickLabelColor()));
  }

  public static void drawString(
      Graphics2D g,
      Configuration c,
      Point2D p,
      String s,
      AnchorH anchorH,
      AnchorV anchorV,
      Configuration.Text.Use use,
      Configuration.Text.Direction direction,
      Color color) {
    if (s.isEmpty()) {
      return;
    }
    g.setFont(fonts(g, c).get(use));
    double sW = computeStringW(g, c, s, use);
    double sH = computeStringH(g, c, use);
    double w =
        switch (direction) {
          case H -> sW;
          case V -> sH;
        };
    double h =
        switch (direction) {
          case H -> sH;
          case V -> sW;
        };
    double d = g.getFontMetrics().getDescent();
    double x =
        switch (anchorH) {
          case L -> p.getX();
          case C -> p.getX() - w / 2;
          case R -> p.getX() - w;
        };
    double y =
        switch (anchorV) {
          case T -> p.getY();
          case C -> p.getY() + h / 2;
          case B -> p.getY() + h;
        };
    markRectangle(g, c, new Rectangle2D.Double(x, y - h, w, h));
    g.setColor(color);
    if (direction.equals(Configuration.Text.Direction.V)) {
      g.setFont(g.getFont().deriveFont(AffineTransform.getRotateInstance(Math.toRadians(-90))));
      g.drawString(s, (float) (x + w - d), (float) y);
    } else {
      g.drawString(s, (float) x, (float) (y - d));
    }
  }

  public static <P extends XYPlot<D>, D> Layout computeLayout(
      Graphics2D g, Configuration c, P plot, XYPlotDrawer<P, D> plotDrawer) {
    GMetrics gm = new GMetrics(g);
    double initialXAxisL = computeStringW(g, c, "0", Configuration.Text.Use.TICK_LABEL)
        + 2d * c.layout().xAxisMarginHRate() * gm.h
        + c.layout().xAxisInnerMarginHRate() * gm.h;
    double initialYAxisL = computeStringW(g, c, "0", Configuration.Text.Use.TICK_LABEL)
        + 2d * c.layout().yAxisMarginWRate() * gm.w
        + c.layout().yAxisInnerMarginWRate() * gm.w;
    // build an empty layout
    Layout l = new Layout(
        gm.w,
        gm.h,
        plot.dataGrid().w(),
        plot.dataGrid().h(),
        plot.title().isEmpty()
            ? 0
            : (computeStringH(g, c, Configuration.Text.Use.TITLE)
                + 2d * c.layout().mainTitleMarginHRate() * gm.h),
        plotDrawer.computeLegendH(g, plot) + 2d * c.layout().legendMarginHRate() * gm.h,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? plot.dataGrid().entries().stream()
                    .filter(e -> e.key().y() == 0)
                    .map(e -> e.value().xTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, c, Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().colTitleMarginHRate() * gm.h)
            : 0,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? plot.dataGrid().entries().stream()
                    .filter(e ->
                        e.key().x() == plot.dataGrid().w() - 1)
                    .map(e -> e.value().yTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, c, Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().rowTitleMarginWRate() * gm.w)
            : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? initialXAxisL : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? initialYAxisL : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 : initialXAxisL,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 : initialYAxisL,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? 0
            : plot.dataGrid().entries().stream()
                    .map(e -> e.value().xTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, c, Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().colTitleMarginHRate() * gm.h),
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? 0
            : plot.dataGrid().entries().stream()
                    .map(e -> e.value().yTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, c, Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().rowTitleMarginWRate() * gm.w),
        plot.dataGrid().keys().stream()
            .mapToDouble(k -> plotDrawer.computeNoteH(g, k, plot))
            .max()
            .orElse(0d),
        c.layout(),
        plot);
    // iterate
    int nOfIterations = 3;
    for (int i = 0; i < nOfIterations; i = i + 1) {
      Grid<Axis> xAxesGrid = plotDrawer.computeXAxes(g, l, plot);
      Grid<Axis> yAxesGrid = plotDrawer.computeYAxes(g, l, plot);
      List<String> xTickLabels = xAxesGrid.values().stream()
          .map(Axis::labels)
          .flatMap(List::stream)
          .toList();
      List<String> yTickLabels = yAxesGrid.values().stream()
          .map(Axis::labels)
          .flatMap(List::stream)
          .toList();
      double maxXTickL = xTickLabels.stream()
          .mapToDouble(s -> computeStringW(g, c, s, Configuration.Text.Use.TICK_LABEL))
          .max()
          .orElse(0);
      double maxYTickL = yTickLabels.stream()
          .mapToDouble(s -> computeStringW(g, c, s, Configuration.Text.Use.TICK_LABEL))
          .max()
          .orElse(0);
      l = l.refit(
          maxXTickL
              + computeStringH(g, c, Configuration.Text.Use.AXIS_LABEL)
              + 2d * c.layout().xAxisMarginHRate() * gm.h
              + c.layout().xAxisInnerMarginHRate() * gm.h,
          maxYTickL
              + computeStringH(g, c, Configuration.Text.Use.AXIS_LABEL)
              + 2d * c.layout().yAxisMarginWRate() * gm.w
              + c.layout().yAxisInnerMarginWRate() * gm.w);
    }
    return l;
  }

  public interface LegendImageDrawer {
    void draw(Graphics2D g, Rectangle2D r, Color c);
  }

  public record GMetrics(double w, double h, double refL) {

    public GMetrics(Graphics2D g) {
      this(
          g.getClipBounds().width,
          g.getClipBounds().height,
          Math.sqrt(g.getClipBounds().width * g.getClipBounds().height));
    }
  }
}
