// Copyright (c) 2003-2004, Luc Maisonobe
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with
// or without modification, are permitted provided that
// the following conditions are met:
// 
//    Redistributions of source code must retain the
//    above copyright notice, this list of conditions and
//    the following disclaimer. 
//    Redistributions in binary form must reproduce the
//    above copyright notice, this list of conditions and
//    the following disclaimer in the documentation
//    and/or other materials provided with the
//    distribution. 
//    Neither the names of spaceroots.org, spaceroots.com
//    nor the names of their contributors may be used to
//    endorse or promote products derived from this
//    software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
// THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
// USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
// USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package edu.mcmaster.maplelab.common.gui;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;

/** This class represents an elliptical arc on a 2D plane.

 * <p>It is designed as an implementation of the
 * <code>java.awt.Shape</code> interface and can therefore be drawn
 * easily as any of the more traditional shapes provided by the
 * standard Java API.</p>

 * <p>This class differs from the <code>java.awt.geom.Ellipse2D</code>
 * in the fact it can handles parts of ellipse in addition to full
 * ellipses and it can handle ellipses which are not aligned with the
 * x and y reference axes of the plane. <p>

 * <p>Another improvement is that this class can handle degenerated
 * cases like for example very flat ellipses (semi-minor axis much
 * smaller than semi-major axis) and drawing of very small parts of
 * such ellipses at very high magnification scales. This imply
 * monitoring the drawing approximation error for extremely small
 * values. Such cases occur for example while drawing orbits of comets
 * near the perihelion.</p>

 * <p>When the arc does not cover the complete ellipse, the lines
 * joining the center of the ellipse to the endpoints can optionally
 * be included or not in the outline, hence allowing to use it for
 * pie-charts rendering. If these lines are not included, the curve is
 * not naturally closed.</p>

 * @author L. Maisonobe
 */
public class EllipticalArc
  implements Shape {

  private static final double _twoPi = 2 * Math.PI;

  // coefficients for error estimation
  // while using quadratic B�zier curves for approximation
  // 0 < b/a < 1/4
  private static final double[][][] _coeffs2Low = new double[][][] {
    {
      {  3.92478,   -13.5822,     -0.233377,    0.0128206   },
      { -1.08814,     0.859987,    0.000362265, 0.000229036 },
      { -0.942512,    0.390456,    0.0080909,   0.00723895  },
      { -0.736228,    0.20998,     0.0129867,   0.0103456   }
    }, {
      { -0.395018,    6.82464,     0.0995293,   0.0122198   },
      { -0.545608,    0.0774863,   0.0267327,   0.0132482   },
      {  0.0534754,  -0.0884167,   0.012595,    0.0343396   },
      {  0.209052,   -0.0599987,  -0.00723897,  0.00789976  }
    }
  };

  // coefficients for error estimation
  // while using quadratic B�zier curves for approximation
  // 1/4 <= b/a <= 1
  private static final double[][][] _coeffs2High = new double[][][] {
    {
      {  0.0863805, -11.5595,     -2.68765,     0.181224    },
      {  0.242856,   -1.81073,     1.56876,     1.68544     },
      {  0.233337,   -0.455621,    0.222856,    0.403469    },
      {  0.0612978,  -0.104879,    0.0446799,   0.00867312  }
    }, {
      {  0.028973,    6.68407,     0.171472,    0.0211706   },
      {  0.0307674,  -0.0517815,   0.0216803,  -0.0749348   },
      { -0.0471179,   0.1288,     -0.0781702,   2.0         },
      { -0.0309683,   0.0531557,  -0.0227191,   0.0434511   }
    }
  };

  // safety factor to convert the "best" error approximation
  // into a "max bound" error
  private static final double[] _safety2 = new double[] {
    0.02, 2.83, 0.125, 0.01
  };

  // coefficients for error estimation
  // while using cubic B�zier curves for approximation
  // 0 < b/a < 1/4
  private static final double[][][] _coeffs3Low = new double[][][] {
    {
      {  3.85268,   -21.229,      -0.330434,    0.0127842  },
      { -1.61486,     0.706564,    0.225945,    0.263682   },
      { -0.910164,    0.388383,    0.00551445,  0.00671814 },
      { -0.630184,    0.192402,    0.0098871,   0.0102527  }
    }, {
      { -0.162211,    9.94329,     0.13723,     0.0124084  },
      { -0.253135,    0.00187735,  0.0230286,   0.01264    },
      { -0.0695069,  -0.0437594,   0.0120636,   0.0163087  },
      { -0.0328856,  -0.00926032, -0.00173573,  0.00527385 }
    }
  };

  // coefficients for error estimation
  // while using cubic B�zier curves for approximation
  // 1/4 <= b/a <= 1
  private static final double[][][] _coeffs3High = new double[][][] {
    {
      {  0.0899116, -19.2349,     -4.11711,     0.183362   },
      {  0.138148,   -1.45804,     1.32044,     1.38474    },
      {  0.230903,   -0.450262,    0.219963,    0.414038   },
      {  0.0590565,  -0.101062,    0.0430592,   0.0204699  }
    }, {
      {  0.0164649,   9.89394,     0.0919496,   0.00760802 },
      {  0.0191603,  -0.0322058,   0.0134667,  -0.0825018  },
      {  0.0156192,  -0.017535,    0.00326508, -0.228157   },
      { -0.0236752,   0.0405821,  -0.0173086,   0.176187   }
    }
  };

  // safety factor to convert the "best" error approximation
  // into a "max bound" error
  private static final double[] _safety3 = new double[] {
    0.001, 4.98, 0.207, 0.0067
  };

  /** Abscissa of the center of the ellipse. */
  protected double _cx;

  /** Ordinate of the center of the ellipse. */
  protected double _cy;

  /** Semi-major axis. */
  protected double _a;

  /** Semi-minor axis. */
  protected double _b;

  /** Orientation of the major axis with respect to the x axis. */
  protected double _theta;
  private   double _cosTheta;
  private   double _sinTheta;

  /** Start angle of the arc. */
  protected double _eta1;

  /** End angle of the arc. */
  protected double _eta2;

  /** Abscissa of the start point. */
  protected double _x1;

  /** Ordinate of the start point. */
  protected double _y1;

  /** Abscissa of the end point. */
  protected double _x2;

  /** Ordinate of the end point. */
  protected double _y2;

  /** Abscissa of the first focus. */
  protected double _xF1;

  /** Ordinate of the first focus. */
  protected double _yF1;

  /** Abscissa of the second focus. */
  protected double _xF2;

  /** Ordinate of the second focus. */
  protected double _yF2;

  /** Abscissa of the leftmost point of the arc. */
  private double _xLeft;

  /** Ordinate of the highest point of the arc. */
  private double _yUp;

  /** Horizontal width of the arc. */
  private double _width;

  /** Vertical height of the arc. */
  private double _height;

  /** Maximal degree for B�zier curve approximation. */
  private int _maxDegree;

  /** Default flatness for B�zier curve approximation. */
  private double _defaultFlatness;

  protected double _f;
  protected double _e2;
  protected double _g;
  protected double _g2;

  /** Simple constructor.
   * Build an elliptical arc composed of the full unit circle centered
   * on origin
   */
  public EllipticalArc() {

    _cx         = 0;
    _cy         = 0;
    _a          = 1;
    _b          = 1;
    _theta      = 0;
    _eta1       = 0;
    _eta2       = 2 * Math.PI;
    _cosTheta   = 1;
    _sinTheta   = 0;
    _maxDegree  = 3;
    _defaultFlatness = 0.5; // half a pixel

    computeFocii();
    computeEndPoints();
    computeBounds();
    computeDerivedFlatnessParameters();

  }

  /** Build an elliptical arc from its canonical geometrical elements.
   * @param center center of the ellipse
   * @param a semi-major axis
   * @param b semi-minor axis
   * @param theta orientation of the major axis with respect to the x axis
   * @param lambda1 start angle of the arc
   * @param lambda2 end angle of the arc
   * @param isPieSlice if true, the lines between the center of the ellipse
   * and the endpoints are part of the shape (it is pie slice like)
   */
  public EllipticalArc(Point2D.Double center, double a, double b,
                       double theta, double lambda1, double lambda2) {
    this(center.x, center.y, a, b, theta, lambda1, lambda2);
  }

  /** Build an elliptical arc from its canonical geometrical elements.
   * @param cx abscissa of the center of the ellipse
   * @param cy ordinate of the center of the ellipse
   * @param a semi-major axis
   * @param b semi-minor axis
   * @param theta orientation of the major axis with respect to the x axis
   * @param lambda1 start angle of the arc
   * @param lambda2 end angle of the arc
   * @param isPieSlice if true, the lines between the center of the ellipse
   * and the endpoints are part of the shape (it is pie slice like)
   */
  public EllipticalArc(double cx, double cy, double a, double b,
                       double theta, double lambda1, double lambda2) {

    _cx         = cx;
    _cy         = cy;
    _a          = a;
    _b          = b;
    _theta      = theta;

    _eta1       = Math.atan2(Math.sin(lambda1) / b,
                            Math.cos(lambda1) / a);
    _eta2       = Math.atan2(Math.sin(lambda2) / b,
                            Math.cos(lambda2) / a);
    _cosTheta   = Math.cos(theta);
    _sinTheta   = Math.sin(theta);
    _maxDegree  = 3;
    _defaultFlatness = 0.5; // half a pixel

    // make sure we have eta1 <= eta2 <= eta1 + 2 PI
    _eta2 -= _twoPi * Math.floor((_eta2 - _eta1) / _twoPi);

    // the preceding correction fails if we have exactly et2 - eta1 = 2 PI
    // it reduces the interval to zero length
    if ((lambda2 - lambda1 > Math.PI) && (_eta2 - _eta1 < Math.PI)) {
      _eta2 += 2 * Math.PI;
    }

    computeFocii();
    computeEndPoints();
    computeBounds();
    computeDerivedFlatnessParameters();

  }

  /** Build a full ellipse from its canonical geometrical elements.
   * @param center center of the ellipse
   * @param a semi-major axis
   * @param b semi-minor axis
   * @param theta orientation of the major axis with respect to the x axis
   */
  public EllipticalArc(Point2D.Double center,
                       double a, double b, double theta) {
    this(center.x, center.y, a, b, theta);
  }

  /** Build a full ellipse from its canonical geometrical elements.
   * @param cx abscissa of the center of the ellipse
   * @param cy ordinate of the center of the ellipse
   * @param a semi-major axis
   * @param b semi-minor axis
   * @param theta orientation of the major axis with respect to the x axis
   */
  public EllipticalArc(double cx, double cy, double a, double b,
                       double theta) {

    _cx         = cx;
    _cy         = cy;
    _a          = a;
    _b          = b;
    _theta      = theta;

    _eta1      = 0;
    _eta2      = 2 * Math.PI;
    _cosTheta  = Math.cos(theta);
    _sinTheta  = Math.sin(theta);
    _maxDegree = 3;
    _defaultFlatness = 0.5; // half a pixel

    computeFocii();
    computeEndPoints();
    computeBounds();
    computeDerivedFlatnessParameters();

  }

  /** Set the maximal degree allowed for B�zier curve approximation.
   * @param maxDegree maximal allowed degree (must be between 1 and 3)
   * @exception IllegalArgumentException if maxDegree is not between 1 and 3
   */
  public void setMaxDegree(int maxDegree) {
    if ((maxDegree < 1) || (maxDegree > 3)) {
      throw new IllegalArgumentException("maxDegree must be between 1 and 3");
    }
    _maxDegree = maxDegree;
  }

  /** Set the default flatness for B�zier curve approximation.
   * @param defaultFlatness default flatness (must be greater than 1.0e-10)
   * @exception IllegalArgumentException if defaultFlatness is lower
   * than 1.0e-10
   */
  public void setDefaultFlatness(double defaultFlatness) {
    if (defaultFlatness < 1.0e-10) {
      throw new IllegalArgumentException("defaultFlatness must be"
                                         + " greater than 1.0e-10");
    }
    _defaultFlatness = defaultFlatness;
  }

  /** Compute the locations of the focii. */
  private void computeFocii() {

    double d  = Math.sqrt(_a * _a - _b * _b);
    double dx = d * _cosTheta;
    double dy = d * _sinTheta;

    _xF1 = _cx - dx;
    _yF1 = _cy - dy;
    _xF2 = _cx + dx;
    _yF2 = _cy + dy;

  }

  /** Compute the locations of the endpoints. */
  private void computeEndPoints() {

    // start point
    double aCosEta1 = _a * Math.cos(_eta1);
    double bSinEta1 = _b * Math.sin(_eta1);
    _x1 = _cx + aCosEta1 * _cosTheta - bSinEta1 * _sinTheta;
    _y1 = _cy + aCosEta1 * _sinTheta + bSinEta1 * _cosTheta;

    // end point
    double aCosEta2 = _a * Math.cos(_eta2);
    double bSinEta2 = _b * Math.sin(_eta2);
    _x2 = _cx + aCosEta2 * _cosTheta - bSinEta2 * _sinTheta;
    _y2 = _cy + aCosEta2 * _sinTheta + bSinEta2 * _cosTheta;

  }

  /** Compute the bounding box. */
  private void computeBounds() {

    double bOnA = _b / _a;
    double etaXMin, etaXMax, etaYMin, etaYMax;
    if (Math.abs(_sinTheta) < 0.1) {
      double tanTheta = _sinTheta / _cosTheta;
      if (_cosTheta < 0) {
        etaXMin = -Math.atan(tanTheta * bOnA);
        etaXMax = etaXMin + Math.PI;
        etaYMin = 0.5 * Math.PI - Math.atan(tanTheta / bOnA);
        etaYMax = etaYMin + Math.PI;
      } else {
        etaXMax = -Math.atan(tanTheta * bOnA);
        etaXMin = etaXMax - Math.PI;
        etaYMax = 0.5 * Math.PI - Math.atan(tanTheta / bOnA);
        etaYMin = etaYMax - Math.PI;
      }
    } else {
      double invTanTheta = _cosTheta / _sinTheta;
      if (_sinTheta < 0) {
        etaXMax = 0.5 * Math.PI + Math.atan(invTanTheta / bOnA);
        etaXMin = etaXMax - Math.PI;
        etaYMin = Math.atan(invTanTheta * bOnA);
        etaYMax = etaYMin + Math.PI;
      } else {
        etaXMin = 0.5 * Math.PI + Math.atan(invTanTheta / bOnA);
        etaXMax = etaXMin + Math.PI;
        etaYMax = Math.atan(invTanTheta * bOnA);
        etaYMin = etaYMax - Math.PI;
      }
    }

    etaXMin -= _twoPi * Math.floor((etaXMin - _eta1) / _twoPi);
    etaYMin -= _twoPi * Math.floor((etaYMin - _eta1) / _twoPi);
    etaXMax -= _twoPi * Math.floor((etaXMax - _eta1) / _twoPi);
    etaYMax -= _twoPi * Math.floor((etaYMax - _eta1) / _twoPi);

    _xLeft = (etaXMin <= _eta2)
      ? (_cx + _a * Math.cos(etaXMin) * _cosTheta - _b * Math.sin(etaXMin) * _sinTheta)
      : Math.min(_x1, _x2);
    _yUp = (etaYMin <= _eta2)
      ? (_cy + _a * Math.cos(etaYMin) * _sinTheta + _b * Math.sin(etaYMin) * _cosTheta)
      : Math.min(_y1, _y2);
    _width = ((etaXMax <= _eta2)
             ? (_cx + _a * Math.cos(etaXMax) * _cosTheta - _b * Math.sin(etaXMax) * _sinTheta)
             : Math.max(_x1, _x2)) - _xLeft;
    _height = ((etaYMax <= _eta2)
              ? (_cy + _a * Math.cos(etaYMax) * _sinTheta + _b * Math.sin(etaYMax) * _cosTheta)
              : Math.max(_y1, _y2)) - _yUp;

  }

  private void computeDerivedFlatnessParameters() {
    _f   = (_a - _b) / _a;
    _e2  = _f * (2.0 - _f);
    _g   = 1.0 - _f;
    _g2  = _g * _g;
  }

  /** Compute the value of a rational function.
   * This method handles rational functions where the numerator is
   * quadratic and the denominator is linear
   * @param x absissa for which the value should be computed
   * @param c coefficients array of the rational function
   */
  private static double rationalFunction(double x, double[] c) {
    return (x * (x * c[0] + c[1]) + c[2]) / (x + c[3]);
  }

  /** Estimate the approximation error for a sub-arc of the instance.
   * @param degree degree of the B�zier curve to use (1, 2 or 3)
   * @param tA start angle of the sub-arc
   * @param tB end angle of the sub-arc
   * @return upper bound of the approximation error between the B�zier
   * curve and the real ellipse
   */
  protected double estimateError(int degree, double etaA, double etaB) {

    double eta  = 0.5 * (etaA + etaB);

    if (degree < 2) {

      // start point
      double aCosEtaA  = _a * Math.cos(etaA);
      double bSinEtaA  = _b * Math.sin(etaA);
      double xA        = _cx + aCosEtaA * _cosTheta - bSinEtaA * _sinTheta;
      double yA        = _cy + aCosEtaA * _sinTheta + bSinEtaA * _cosTheta;

      // end point
      double aCosEtaB  = _a * Math.cos(etaB);
      double bSinEtaB  = _b * Math.sin(etaB);
      double xB        = _cx + aCosEtaB * _cosTheta - bSinEtaB * _sinTheta;
      double yB        = _cy + aCosEtaB * _sinTheta + bSinEtaB * _cosTheta;

      // maximal error point
      double aCosEta   = _a * Math.cos(eta);
      double bSinEta   = _b * Math.sin(eta);
      double x         = _cx + aCosEta * _cosTheta - bSinEta * _sinTheta;
      double y         = _cy + aCosEta * _sinTheta + bSinEta * _cosTheta;

      double dx = xB - xA;
      double dy = yB - yA;

      return Math.abs(x * dy - y * dx + xB * yA - xA * yB)
           / Math.sqrt(dx * dx + dy * dy);

    } else {

      double x    = _b / _a;
      double dEta = etaB - etaA;
      double cos2 = Math.cos(2 * eta);
      double cos4 = Math.cos(4 * eta);
      double cos6 = Math.cos(6 * eta);

      // select the right coeficients set according to degree and b/a
      double[][][] coeffs;
      double[] safety;
      if (degree == 2) {
        coeffs = (x < 0.25) ? _coeffs2Low : _coeffs2High;
        safety = _safety2;
      } else {
        coeffs = (x < 0.25) ? _coeffs3Low : _coeffs3High;
        safety = _safety3;
      }

      double c0 = rationalFunction(x, coeffs[0][0])
         + cos2 * rationalFunction(x, coeffs[0][1])
         + cos4 * rationalFunction(x, coeffs[0][2])
         + cos6 * rationalFunction(x, coeffs[0][3]);

      double c1 = rationalFunction(x, coeffs[1][0])
         + cos2 * rationalFunction(x, coeffs[1][1])
         + cos4 * rationalFunction(x, coeffs[1][2])
         + cos6 * rationalFunction(x, coeffs[1][3]);

      return rationalFunction(x, safety) * _a * Math.exp(c0 + c1 * dEta);

    }

  }

  /** Get the elliptical arc point for a given angular parameter.
   * @param lambda angular parameter for which point is desired
   * @param p placeholder where to put the point, if null a new Point
   * well be allocated
   * @return the object p or a new object if p was null, set to the
   * desired elliptical arc point location
   */
  public Point2D.Double pointAt(double lambda, Point2D.Double p) {

    if (p == null) {
      p = new Point2D.Double();
    }

    double eta      = Math.atan2(Math.sin(lambda) / _b, Math.cos(lambda) / _a);
    double aCosEta  = _a * Math.cos(eta);
    double bSinEta  = _b * Math.sin(eta);

    p.x = _cx + aCosEta * _cosTheta - bSinEta * _sinTheta;
    p.y = _cy + aCosEta * _sinTheta + bSinEta * _cosTheta;

    return p;

  }

  /** Tests if the specified coordinates are inside the boundary of the Shape.
   * @param x abscissa of the test point
   * @param y ordinate of the test point
   * @return true if the specified coordinates are inside the Shape
   * boundary; false otherwise
   */
  public boolean contains(double x, double y) {

    // position relative to the focii
    double dx1 = x - _xF1;
    double dy1 = y - _yF1;
    double dx2 = x - _xF2;
    double dy2 = y - _yF2;
    if ((dx1 * dx1 + dy1 * dy1 + dx2 * dx2 + dy2 * dy2) > (4 * _a * _a)) {
      // the point is outside of the ellipse
      return false;
    }

    // check the location of the test point with respect to the
    // line joining the start and end points
    double dx = _x2 - _x1;
    double dy = _y2 - _y1;
    return ((x * dy - y * dx + _x2 * _y1 - _x1 * _y2) >= 0);

  }

  /** Tests if a line segment intersects the arc.
   * @param xA abscissa of the first point of the line segment
   * @param yA ordinate of the first point of the line segment
   * @param xB abscissa of the second point of the line segment
   * @param yB ordinate of the second point of the line segment
   * @return true if the two line segments intersect
   */
  private boolean intersectArc(double xA, double yA,
                               double xB, double yB) {

    double dx = xA - xB;
    double dy = yA - yB;
    double l  = Math.sqrt(dx * dx + dy * dy);
    if (l < (1.0e-10 * _a)) {
      // too small line segment, we consider it doesn't intersect anything
      return false;
    }
    double cz = (dx * _cosTheta + dy * _sinTheta) / l;
    double sz = (dy * _cosTheta - dx * _sinTheta) / l;

    // express position of the first point in canonical frame
    dx = xA - _cx;
    dy = yA - _cy;
    double u = dx * _cosTheta + dy * _sinTheta;
    double v = dy * _cosTheta - dx * _sinTheta;

    double u2         = u * u;
    double v2         = v * v;
    double g2u2ma2    = _g2 * (u2 - _a * _a);
    double g2u2ma2pv2 = g2u2ma2 + v2;

    // compute intersections with the ellipse along the line
    // as the roots of a 2nd degree polynom : c0 k^2 - 2 c1 k + c2 = 0
    double c0   = 1.0 - _e2 * cz * cz;
    double c1   = _g2 * u * cz + v * sz;
    double c2   = g2u2ma2pv2;
    double c12  = c1 * c1;
    double c0c2 = c0 * c2;

    if (c12 < c0c2) {
      // the line does not intersect the ellipse at all
      return false;
    }

    double k = (c1 >= 0)
             ? (c1 + Math.sqrt(c12 - c0c2)) / c0
             : c2 / (c1 - Math.sqrt(c12 - c0c2));
    if ((k >= 0) && (k <= l)) {
      double uIntersect = u - k * cz;
      double vIntersect = v - k * sz;
      double eta = Math.atan2(vIntersect / _b, uIntersect / _a);
      eta -= _twoPi * Math.floor((eta - _eta1) / _twoPi);
      if (eta <= _eta2) {
        return true;
      }
    }

    k = c2 / (k * c0);
    if ((k >= 0) && (k <= l)) {
      double uIntersect = u - k * cz;
      double vIntersect = v - k * sz;
      double eta = Math.atan2(vIntersect / _b, uIntersect / _a);
      eta -= _twoPi * Math.floor((eta - _eta1) / _twoPi);
      if (eta <= _eta2) {
        return true;
      }
    }

    return false;

  }

  /** Tests if two line segments intersect.
   * @param x1 abscissa of the first point of the first line segment
   * @param y1 ordinate of the first point of the first line segment
   * @param x2 abscissa of the second point of the first line segment
   * @param y2 ordinate of the second point of the first line segment
   * @param xA abscissa of the first point of the second line segment
   * @param yA ordinate of the first point of the second line segment
   * @param xB abscissa of the second point of the second line segment
   * @param yB ordinate of the second point of the second line segment
   * @return true if the two line segments intersect
   */
  private static boolean intersect(double x1, double y1,
                                   double x2, double y2,
                                   double xA, double yA,
                                   double xB, double yB) {

    // elements of the equation of the (1, 2) line segment
    double dx12 = x2 - x1;
    double dy12 = y2 - y1;
    double k12  = x2 * y1 - x1 * y2;

    // elements of the equation of the (A, B) line segment
    double dxAB = xB - xA;
    double dyAB = yB - yA;
    double kAB  = xB * yA - xA * yB;

    // compute relative positions of endpoints versus line segments
    double pAvs12 = xA * dy12 - yA * dx12 + k12;
    double pBvs12 = xB * dy12 - yB * dx12 + k12;
    double p1vsAB = x1 * dyAB - y1 * dxAB + kAB;
    double p2vsAB = x2 * dyAB - y2 * dxAB + kAB;

    return (pAvs12 * pBvs12 <= 0) && (p1vsAB * p2vsAB <= 0);

  }

  /** Tests if a line segment intersects the outline.
   * @param xA abscissa of the first point of the line segment
   * @param yA ordinate of the first point of the line segment
   * @param xB abscissa of the second point of the line segment
   * @param yB ordinate of the second point of the line segment
   * @return true if the two line segments intersect
   */
  private boolean intersectOutline(double xA, double yA,
                                   double xB, double yB) {

    if (intersectArc(xA, yA, xB, yB)) {
      return true;
    }

    return intersect(_x1, _y1, _x2, _y2, xA, yA, xB, yB);
  }

  /** Tests if the interior of the Shape entirely contains the
   * specified rectangular area.
   * @param x abscissa of the upper-left corner of the test rectangle
   * @param y ordinate of the upper-left corner of the test rectangle
   * @param w width of the test rectangle
   * @param h height of the test rectangle
   * @return true if the interior of the Shape entirely contains the
   * specified rectangular area; false otherwise
   */
  public boolean contains(double x, double y, double w, double h) {
    double xPlusW = x + w;
    double yPlusH = y + h;
    return (   contains(x, y)
            && contains(xPlusW, y)
            && contains(x, yPlusH)
            && contains(xPlusW, yPlusH)
            && (! intersectOutline(x,      y,      xPlusW, y))
            && (! intersectOutline(xPlusW, y,      xPlusW, yPlusH))
            && (! intersectOutline(xPlusW, yPlusH, x,      yPlusH))
            && (! intersectOutline(x,      yPlusH, x,      y)));
  }

  /** Tests if a specified Point2D is inside the boundary of the Shape.
   * @param p test point
   * @return true if the specified point is inside the Shape
   * boundary; false otherwise
   */
  public boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
  }

  /** Tests if the interior of the Shape entirely contains the
   * specified Rectangle2D.
   * @param r test rectangle
   * @return true if the interior of the Shape entirely contains the
   * specified rectangular area; false otherwise
   */
  public boolean contains(Rectangle2D r) {
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  /** Returns an integer Rectangle that completely encloses the Shape.
   */
  public Rectangle getBounds() {
    int xMin = (int) Math.rint(_xLeft - 0.5);
    int yMin = (int) Math.rint(_yUp   - 0.5);
    int xMax = (int) Math.rint(_xLeft + _width  + 0.5);
    int yMax = (int) Math.rint(_yUp   + _height + 0.5);
    return new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
  }

  /** Returns a high precision and more accurate bounding box of the
   * Shape than the getBounds method.
   */
  public Rectangle2D getBounds2D() {
    return new Rectangle2D.Double(_xLeft, _yUp, _width, _height);
  }

  private PathIterator buildStraightLine(AffineTransform at) {
      GeneralPath path = new GeneralPath(PathIterator.WIND_EVEN_ODD);
      return path.getPathIterator(at);      
  }

  /** Build an approximation of the instance outline.
   * @param degree degree of the B�zier curve to use
   * @param threshold acceptable error
   * @param at affine transformation to apply
   * @return a path iterator
   */
  private PathIterator buildPathIterator(int degree, double threshold,
                                         AffineTransform at) {

    // find the number of B�zier curves needed
    boolean found = false;
    int n = 1;
    while ((! found) && (n < 1024)) {
      double dEta = (_eta2 - _eta1) / n;
      if (dEta <= 0.5 * Math.PI) {
        double etaB = _eta1;
        found = true;
        for (int i = 0; found && (i < n); ++i) {
          double etaA = etaB;
          etaB += dEta;
          found = (estimateError(degree, etaA, etaB) <= threshold);
        }
      }
      n = n << 1;
    }

    GeneralPath path = new GeneralPath(PathIterator.WIND_EVEN_ODD);
    double dEta = (_eta2 - _eta1) / n;
    double etaB = _eta1;

    double cosEtaB  = Math.cos(etaB);
    double sinEtaB  = Math.sin(etaB);
    double aCosEtaB = _a * cosEtaB;
    double bSinEtaB = _b * sinEtaB;
    double aSinEtaB = _a * sinEtaB;
    double bCosEtaB = _b * cosEtaB;
    double xB       = _cx + aCosEtaB * _cosTheta - bSinEtaB * _sinTheta;
    double yB       = _cy + aCosEtaB * _sinTheta + bSinEtaB * _cosTheta;
    double xBDot    = -aSinEtaB * _cosTheta - bCosEtaB * _sinTheta;
    double yBDot    = -aSinEtaB * _sinTheta + bCosEtaB * _cosTheta;

    path.moveTo((float) xB, (float) yB);

    double t     = Math.tan(0.5 * dEta);
    double alpha = Math.sin(dEta) * (Math.sqrt(4 + 3 * t * t) - 1) / 3;

    for (int i = 0; i < n; ++i) {

      double xA    = xB;
      double yA    = yB;
      double xADot = xBDot;
      double yADot = yBDot;

      etaB    += dEta;
      cosEtaB  = Math.cos(etaB);
      sinEtaB  = Math.sin(etaB);
      aCosEtaB = _a * cosEtaB;
      bSinEtaB = _b * sinEtaB;
      aSinEtaB = _a * sinEtaB;
      bCosEtaB = _b * cosEtaB;
      xB       = _cx + aCosEtaB * _cosTheta - bSinEtaB * _sinTheta;
      yB       = _cy + aCosEtaB * _sinTheta + bSinEtaB * _cosTheta;
      xBDot    = -aSinEtaB * _cosTheta - bCosEtaB * _sinTheta;
      yBDot    = -aSinEtaB * _sinTheta + bCosEtaB * _cosTheta;

      if (degree == 1) {
        path.lineTo((float) xB, (float) yB);
      } else if (degree == 2) {
        double k = (yBDot * (xB - xA) - xBDot * (yB - yA))
                 / (xADot * yBDot - yADot * xBDot);
        path.quadTo((float) (xA + k * xADot), (float) (yA + k * yADot),
                    (float) xB, (float) yB);
      } else {
        path.curveTo((float) (xA + alpha * xADot), (float) (yA + alpha * yADot),
                     (float) (xB - alpha * xBDot), (float) (yB - alpha * yBDot),
                     (float) xB,                   (float) yB);
      }

    }

    return path.getPathIterator(at);

  }

  /** Returns an iterator object that iterates along the Shape
   * boundary and provides access to the geometry of the Shape
   * outline.
   */
  public PathIterator getPathIterator(AffineTransform at) {
    return buildPathIterator(_maxDegree, _defaultFlatness, at);
  }

  /** Returns an iterator object that iterates along the Shape
   * boundary and provides access to the geometry of the Shape
   * outline.
   */
  public PathIterator getPathIterator(AffineTransform at, boolean isStraightLine) {
    if (isStraightLine) {
        return buildStraightLine(at);
    } else {
        return buildPathIterator(_maxDegree, _defaultFlatness, at);
    }
  }
  
  /** Returns an iterator object that iterates along the Shape
   * boundary and provides access to a flattened view of the Shape
   * outline geometry.
   */
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return buildPathIterator(1, flatness, at);
  }

  /** Tests if the interior of the Shape intersects the interior of a
   * specified rectangular area.
   */
  public boolean intersects(double x, double y, double w, double h) {
    double xPlusW = x + w;
    double yPlusH = y + h;
    return contains(x, y)
        || contains(xPlusW, y)
        || contains(x, yPlusH)
        || contains(xPlusW, yPlusH)
        || intersectOutline(x,      y,      xPlusW, y)
        || intersectOutline(xPlusW, y,      xPlusW, yPlusH)
        || intersectOutline(xPlusW, yPlusH, x,      yPlusH)
        || intersectOutline(x,      yPlusH, x,      y);
  }

  /** Tests if the interior of the Shape intersects the interior of a
   * specified Rectangle2D.
   */
  public boolean intersects(Rectangle2D r) {
    return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

}
