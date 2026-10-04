#include <concepts>
#include <iostream>

// C++のコードを動かす前に、コンパイラとC++の規格を確かめる
template <typename T>
concept Number = std::integral<T> || std::floating_point<T>;

template <Number T>
T twice(T value) {
    return value * 2;
}

int main() {
#if defined(__clang__)
    std::cout << "コンパイラ: clang " << __clang_major__ << "." << __clang_minor__ << "." << __clang_patchlevel__ << "\n";
#elif defined(__GNUC__)
    std::cout << "コンパイラ: GCC " << __GNUC__ << "." << __GNUC_MINOR__ << "." << __GNUC_PATCHLEVEL__ << "\n";
#endif
    std::cout << "C++の規格（__cplusplus）: " << __cplusplus << "\n";
    std::cout << "コンセプトを使った関数: " << twice(21) << "\n";
}
