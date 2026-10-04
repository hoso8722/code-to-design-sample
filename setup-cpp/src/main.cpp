#include <algorithm>
#include <iostream>
#include <string>
#include <utility>
#include <vector>

class Student {
public:
    Student(std::string name, int math, int english, int science)
        : name_(std::move(name)), math_(math), english_(english), science_(science) {}

    const std::string& name() const { return name_; }
    int math_score() const { return math_; }
    int total_score() const { return math_ + english_ + science_; }
    int lowest_score() const { return std::min({math_, english_, science_}); }

private:
    std::string name_;
    int math_;
    int english_;
    int science_;
};

class PassingRule {
public:
    virtual ~PassingRule() = default;

    virtual bool is_passed(const Student& student) const = 0;
    virtual std::string description() const = 0;
};

class TotalScoreRule : public PassingRule {
public:
    TotalScoreRule(int min_total, int min_each)
        : min_total_(min_total), min_each_(min_each) {}

    bool is_passed(const Student& student) const override {
        return student.total_score() >= min_total_ && student.lowest_score() >= min_each_;
    }

    std::string description() const override {
        std::string text = "合計" + std::to_string(min_total_) + "点以上";
        if (min_each_ > 0) {
            text += "・各科目" + std::to_string(min_each_) + "点以上";
        }
        return text;
    }

private:
    int min_total_;
    int min_each_;
};

class MathRequiredRule : public PassingRule {
public:
    MathRequiredRule(int min_total, int min_math)
        : min_total_(min_total), min_math_(min_math) {}

    bool is_passed(const Student& student) const override {
        return student.total_score() >= min_total_ && student.math_score() >= min_math_;
    }

    std::string description() const override {
        return "合計" + std::to_string(min_total_) + "点以上・数学" + std::to_string(min_math_) + "点以上";
    }

private:
    int min_total_;
    int min_math_;
};

void print_results(const PassingRule& rule, const std::vector<Student>& students) {
    std::cout << "[" << rule.description() << "]\n";
    for (const Student& student : students) {
        std::cout << "  " << student.name() << ": " << (rule.is_passed(student) ? "合格" : "不合格") << "\n";
    }
}

int main() {
    std::vector<Student> students = {
        Student("Aさん", 70, 65, 80),
        Student("Bさん", 50, 60, 55),
        Student("Cさん", 60, 60, 65),
        Student("Dさん", 95, 35, 90),
    };

    TotalScoreRule standard(180, 0);
    TotalScoreRule advanced(210, 40);
    MathRequiredRule science(180, 70);

    // 異なる種類の基準を、PassingRule へのポインタとして1つにまとめる
    std::vector<const PassingRule*> rules = {&standard, &advanced, &science};

    for (const PassingRule* rule : rules) {
        print_results(*rule, students);
    }
}
