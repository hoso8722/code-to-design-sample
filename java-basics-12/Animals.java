// 実行: java Animals.java
public class Animals {
    public static void main(String[] args) {
        Animal[] animals = {new Dog(), new Cat()};
        for (Animal animal : animals) {
            System.out.println(animal.getClass().getSimpleName() + ": " + animal.speak());
        }
    }
}

interface Animal {
    String speak();
}

class Dog implements Animal {
    @Override
    public String speak() {
        return "ワン";
    }
}

class Cat implements Animal {
    @Override
    public String speak() {
        return "ニャー";
    }
}
