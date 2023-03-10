#ifndef _IMAGES_H__
#define _IMAGES_H__

#include "color.h"
#include "coordinate.h"
#include <cmath>
#include <functional>

using Fraction = double;

template <typename T> using Base_image = std::function<T(const Point)>;

using Region = Base_image<bool>;
using Image = Base_image<Color>;
using Blend = Base_image<Fraction>;

template <typename T> Base_image<T> constant(T t) {
  return [t = std::move(t)]([[maybe_unused]] const Point point) {
    return t;
  };
}

template <typename T> Base_image<T> rotate(Base_image<T> image, double phi) {
  return [image = std::move(image), phi = phi](const Point point) {
    const Point in_polar_coords = point.is_polar ? point : to_polar(point);

    const Point rotated_backwards =
        Point(in_polar_coords.first, in_polar_coords.second - phi, true);

    return image(rotated_backwards);
  };
}

template <typename T> Base_image<T> translate(Base_image<T> image, Vector v) {
  return [image = std::move(image), v = std::move(v)](const Point point) {
    const Point in_cartesian_coords =
        point.is_polar ? from_polar(point) : point;

    const Point moved_backwards =
        Point(in_cartesian_coords.first - v.first,
              in_cartesian_coords.second - v.second, false);

    return image(moved_backwards);
  };
}

template <typename T> Base_image<T> scale(Base_image<T> image, double s) {
  return [image = std::move(image), s = s](const Point point) {
    const Point scaled = point.is_polar
                             ? Point(point.first / s, point.second, true)
                             : Point(point.first / s, point.second / s, false);

    return image(scaled);
  };
}

template <typename T>
Base_image<T> circle(Point q, double r, T inner, T outer) {
  const Point center = q.is_polar ? from_polar(q) : q;

  return [center = center, r = r, inner = std::move(inner),
          outer = std::move(outer)](const Point point) {
    auto circle_equation =
        (center.first - point.first) * (center.first - point.first) +
        (center.second - point.second) * (center.second - point.second);

    return circle_equation <= r * r ? inner : outer;
  };
}

template <typename T> Base_image<T> checker(double d, T this_way, T that_way) {
  return [this_way = std::move(this_way), that_way = std::move(that_way),
          d = d](const Point point) {
    const Point argument = point.is_polar ? from_polar(point) : point;

    int parity =
        std::floor(argument.first / d) + std::floor(argument.second / d);

    return parity % 2 == 0 ? this_way : that_way;
  };
}

template <typename T>
Base_image<T> polar_checker(double d, int n, T this_way, T that_way) {
  Base_image<T> normal_checker = checker(d, this_way, that_way);
  Base_image<T> reversed_checker = checker(d, that_way, this_way);
  double piece_angle = 2 * (M_PI / n);

  return [normal_checker = std::move(normal_checker),
          reversed_checker = std::move(reversed_checker),
          piece_angle = piece_angle, d = d, n = n](const Point point) {
    const Point polar_argument = point.is_polar ? point : to_polar(point);

    int piece = (int)(polar_argument.second * n / (2 * M_PI));

    return piece % 2 == 0
               ? normal_checker(
                     Point(polar_argument.second, polar_argument.first, false))
               : reversed_checker(
                     Point(polar_argument.second, polar_argument.first, false));
  };
}

template <typename T>
Base_image<T> rings(Point q, double d, T this_way, T that_way) {
  const Point q_param = q.is_polar ? from_polar(q) : q;

  return [q_param = std::move(q_param), d = d, this_way = std::move(this_way),
          that_way = std::move(that_way)](const Point point) {
    const Point point_arg = point.is_polar ? from_polar(point) : point;

    double distrance_from_center = distance(point_arg, q_param);
    int ring_number = (int)(distrance_from_center / d);

    return ring_number % 2 == 0 ? this_way : that_way;
  };
}

template <typename T>
Base_image<T> vertical_stripe(double d, T this_way, T that_way) {
  return [d = d, this_way = std::move(this_way),
          that_way = std::move(that_way)](const Point point) {
    const Point cartesian_form = point.is_polar ? from_polar(point) : point;

    return std::abs(cartesian_form.first) <= d / 2 ? this_way : that_way;
  };
}

Image cond(Region region, Image this_way, Image that_way);

Image lerp(Blend blend, Image this_way, Image that_way);

Image darken(Image image, Blend blend);

Image lighten(Image image, Blend blend);

#endif
