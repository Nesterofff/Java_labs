package lab1;

import java.util.Objects;

public abstract class Food implements Consumable, Nutritious {
    String name;
    int calories;

    public Food(String name, int calories) {
        this.name = name;
        this.calories = calories;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, calories);
    }

    public boolean equals(Object arg0) { // проверка на ввод одинаковых значений
        if (!(arg0 instanceof Food)) return false; // Шаг 1
        if (name == null || ((Food) arg0).name == null) return false; // Шаг 2
        return name.equals(((Food) arg0).name); // Шаг 3
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    @Override
    public int calculateCalories() {
        return calories;
    }

    // Реализация метода consume() удалена из базового класса lab1.Food
// Это можно сделать, потому что сам lab1.Food - абстрактный
}
