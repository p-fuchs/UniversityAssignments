#include "images.h"

Image cond(Region region, Image this_way, Image that_way) {
  return [region = std::move(region), this_way = std::move(this_way),
          that_way = std::move(that_way)](const Point point) {
    return region(point) ? this_way(point) : that_way(point);
  };
}

Image lerp(Blend blend, Image this_way, Image that_way) {
  return [blend = std::move(blend), this_way = std::move(this_way),
          that_way = std::move(that_way)](const Point point) {
    double w = blend(point);
    Color this_color = this_way(point);
    Color that_color = that_way(point);

    return this_color.weighted_mean(that_color, w);
  };
}

Image darken(Image image, Blend blend) {
  return [image = std::move(image), blend = std::move(blend)](const Point point) {
    return image(point).weighted_mean(Colors::black, blend(point));
  };
}

Image lighten(Image image, Blend blend) {
  return [image = std::move(image), blend = std::move(blend)](const Point point) {
    return image(point).weighted_mean(Colors::white, blend(point));
  };
}
