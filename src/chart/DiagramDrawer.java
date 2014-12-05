/**
 * OOP2 assignments 3 and 4
 * Created by V.Batytskyy on 15.04.14
 */

package chart;

import static java.lang.Math.*;
import javafx.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;


/**
 * DiagramDrawer class is used for radar chart representation.
 * Radar chart consists of a few axes, which represent some
 * parameters and points on the axes, connected in polygons,
 * to measure the relative value of the specific parameter for
 * comparison.
 * Radar chart data is taken from ArrayList of Numbers
 */
public class DiagramDrawer extends JPanel {
    // header values for table
    private final ArrayList<String> header;

    // names of the chart axes
    private final ArrayList<String> axes;

    // numeric data to represent
    private final ArrayList<ArrayList<Number>> data;

    // list of min-max pairs for each parameterf
    private ArrayList<Pair<Number, Number>> minMaxAxesVals;

    // end points of the axes
    private final ArrayList<Point> axesEnds;

    // central point of the chart
    private Point center;

    // used colors
    private final static Color[] colorList = {
            new Color(90, 107, 52, 100),
            new Color(151, 32, 130, 100),
            new Color(240, 214, 78, 100),
            new Color(52, 221, 221, 100),
            new Color(215, 183, 64, 200),
            new Color(171, 128, 36, 200),
            new Color(146, 88, 24, 200),
            new Color(137, 232, 148, 200),
            new Color(100, 100, 102, 200),
            new Color(85, 151, 47, 200),
            new Color(151, 93, 66, 200),
            new Color(78, 34, 44, 200),
            new Color(36, 37, 50, 200)
    };

    /**
     * Creates new DiagramDrawer instance with specified data
     * @param header header data for chart
     * @param axes axes names
     * @param data numeric data
     */
    public DiagramDrawer(ArrayList<String> header, ArrayList<String> axes,
                                      ArrayList<ArrayList<Number>> data) {
        this.header = header;
        this.axes = axes;
        this.data = data;

        axesEnds = new ArrayList<Point>();
        center = new Point(getWidth() / 2, getHeight() / 2);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;

        // turn anti-aliasing on
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        center = new Point(getWidth() / 2, getHeight() / 2);

        initAxesEnds();
        int paramsNumb = axes.size();


        int axeLength = center.y - axesEnds.get(0).y;

        // shift of the label, relatively to its axis
        int shift = 10;

        // list of coordinates, which represent labels
        ArrayList<Point> labelPoints = divideAxesInRatio(-(axeLength + shift) / shift);

        // draw polygons
        ArrayList<Point[]> polygonsCoord = new ArrayList<>();
        minMaxAxesVals = getMinMaxAxesVals();
        for (int i = 0; i < data.get(0).size(); i++) {
            polygonsCoord.add(drawPoly(g, i));
        }

        // draw axes and labels
        g.setStroke(new BasicStroke(3));
        for (int i = 0; i < paramsNumb; i++) {
            g.drawLine(center.x, center.y, axesEnds.get(i).x, axesEnds.get(i).y);
            g.drawString(axes.get(i), labelPoints.get(i).x, labelPoints.get(i).y);
        }

        // draw dots on the axes, where polygon touches them
        for (Point[] points : polygonsCoord) {
            for (int i = 0; i < paramsNumb; i++) {
                int d = 7;                              // diameter
                g.fillOval(points[i].x - d / 2, points[i].y - d / 2, d, d);
            }

        }

        // add legend
        g.setStroke(new BasicStroke(1));
        g.drawRect(2, 2, 150, 180);
        FontMetrics metrics = graphics.getFontMetrics();
        g.drawString("legend", 55, metrics.getHeight());

        Color initialColor = g.getColor();
        for (int i = 0; i < header.size(); i++) {
            g.drawString(header.get(i), 90,
                        (i + 2) * (metrics.getHeight() + metrics.getDescent()));

            g.setColor(colorList[i]);
            g.fillRect(10, (i + 2) * (metrics.getHeight() + metrics.getDescent()) - 12,
                       70, metrics.getHeight());

            g.setColor(initialColor);
        }
    }

    /**
     * Saves chart objects into an image
     * @param file file where to save an image
     * @param format .gif, .png or .jpeg File formats
     * @throws NullPointerException is thrown when problem while reading the
     * file occurred
     */
    public void saveTo(final File file, String format) throws IOException, NullPointerException {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        paint(graphics2D);
        ImageIO.write(image, format, file);
    }

    /**
     * Initialize list of axis-end points
     */
    private void initAxesEnds() {
        axesEnds.clear();

        // find delta of angle between two adjacent axes
        double dAlpha = 2 * Math.PI / (data.size());
        int x0 = center.x;
        int y0 = 30;

        axesEnds.add(new Point(x0, y0));

        for (int i = 1; i < data.size(); i++) {
            // turn point x0, y0 to an angle
            int xCur = (int) ((-sin(i * dAlpha) * (y0 - center.y))
                    + (cos(i * dAlpha) * (x0 - center.x)) + center.x);

            int yCur = (int)(cos(i * dAlpha)*(y0 - center.y)
                    + sin(i * dAlpha)*(x0 - center.x) + center.y);

            axesEnds.add(new Point(xCur, yCur));
        }
    }

    /**
     * Finds coordinates for vertices of the polygons
     * @param lambda division parameter
     * @return list of coordinates
     */
    private ArrayList<Point> divideAxesInRatio(double lambda) {
        ArrayList<Point> points = new ArrayList<>();

        for (Point p : axesEnds) {
            points.add(divideAxeInRatio(lambda, p));
        }

        return points;
    }

    /**
     * Finds coordinate of a new point
     * @param lambda division parameter
     * @param p the second point of the segment
     * @return coordinates of the new point, which correspond to
     * division of the segment in the exact proportion lambda
     */
    private Point divideAxeInRatio(double lambda, Point p) {
        int dX = (int)((center.x + lambda * p.x) / (1. + lambda));
        int dY = (int)((center.y + lambda * p.y) / (1. + lambda));

        return new Point(dX, dY);
    }

    /**
     * finds list of min-max pairs, where each pair is limited by
     * the specific parameter
     * @return list of the min-max pairs
     */
    private ArrayList<Pair<Number, Number>> getMinMaxAxesVals() {
        ArrayList<Pair<Number, Number>> minMaxAxesVals = new ArrayList<>();

        for (int j = 0; j < data.size(); j++) {
            Number val = data.get(j).get(0);
            minMaxAxesVals.add(new Pair(val, val));
        }

        for (int i = 0; i < data.size(); i++) {
            Number min = minMaxAxesVals.get(i).getKey();
            Number max = min;

            for (int j = 0; j < data.get(i).size(); j++) {
                if (data.get(i).get(j).doubleValue() > max.doubleValue()) {
                    max = data.get(i).get(j);
                }

                if (data.get(i).get(j).doubleValue() < min.doubleValue()) {
                    min = data.get(i).get(j);
                }
            }
            if (min.doubleValue() > 0) min = 0;

            minMaxAxesVals.set(i, new Pair<>(min, max));
        }

        return minMaxAxesVals;
    }

    /**
     * Draws polygon
     * @param g graphics context
     * @param param axis parameter
     * @return array of points
     */
    private Point[] drawPoly(Graphics2D g, int param) {
        Color initialColor = g.getColor();

        int vertices = data.size();

        int[] xPoints = new int[vertices];
        int[] yPoints = new int[vertices];
        Point[] points = new Point[vertices];

        for (int i = 0; i < data.size(); i++) {
            double min = minMaxAxesVals.get(i).getKey().doubleValue();
            double max = minMaxAxesVals.get(i).getValue().doubleValue();

            double lambda = (data.get(i).get(param).doubleValue() - min)
                         / (max - data.get(i).get(param).doubleValue());
            Point p;
            if (lambda == Double.POSITIVE_INFINITY) {
                p = divideAxeInRatio(20, axesEnds.get(i));
            } else {
                p = divideAxeInRatio(lambda, axesEnds.get(i));
            }

            xPoints[i] = p.x;
            yPoints[i] = p.y;
            points[i] = new Point(p);
        }


        g.setColor(colorList[param]);
        g.fillPolygon(xPoints, yPoints, vertices);

        g.setColor(initialColor);
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(xPoints, yPoints, vertices);

        return points;
    }
}
