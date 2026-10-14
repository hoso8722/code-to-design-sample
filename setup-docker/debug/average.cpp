// 実行: g++ -std=c++20 -g -O0 average.cpp -o average && ./average
#include <iostream>
#include <vector>

double average(const std::vector<int>& scores) {
    int sum = 0;
    for (std::size_t i = 1; i < scores.size(); ++i) {
        sum += scores[i];
    }
    return static_cast<double>(sum) / scores.size();
}

int main() {
    std::vector<int> scores = {80, 70, 90};
    std::cout << "平均: " << average(scores) << "\n";
}
