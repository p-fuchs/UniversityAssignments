#ifndef _FUNCTIONAL_H__
#define _FUNCTIONAL_H__

#include <functional>

inline auto compose() {
  return []<typename T>(T &&value) { return std::forward<T>(value); };
}

template <typename Head> auto compose(Head &&func) {
  return std::forward<Head>(func);
}

template <typename Head, typename... Tail>
auto compose(Head &&func, Tail &&...tail) {
  auto tail_function = compose(std::forward<Tail>(tail)...);

  return [func = std::forward<Head>(func),
          tail_function = std::move(tail_function)]<typename T>(T &&value) {
    return tail_function(func(std::forward<T>(value)));
  };
}

template <typename Head> auto lift(Head &&func) {
  return [func = std::forward<Head>(func)]<typename T>(T &&value) {
    return func();
  };
}

template <typename Head, typename... Tail>
auto lift(Head &&func, Tail &&...tail) {
  return [func = std::forward<Head>(func),
          ... tail = std::forward<Tail>(tail)]<typename T>(T &&value) {
    auto argument = std::forward<T>(value);

    return func(tail(argument)...);
  };
}

#endif
