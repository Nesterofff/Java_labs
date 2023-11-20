// Объявление класса частью пакета

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainApplication {
    // Конструктор класса отсутствует!!!
    // Главный метод главного класса

    public static void main(String[] args) {
        // Определение ссылок на продукты завтрака
        List<Food> breakfast = new ArrayList<>(); // создание массива Food
        // Анализ аргументов командной строки и создание для них
        // экземпляров соответствующих классов для завтрака

        // Сколько обедов сейчас
        boolean isCalculateCalories = false;
        boolean isSort = false;
        for (String arg : args) {
            if (arg.equals("-calories")) {
                isCalculateCalories = true;
                continue;
            }
            if (arg.equals("-sort")) {
                isSort = true;
                continue;
            }
            String[] parts = arg.split("/"); // Разделяем строку
            if (parts[0].equals("Cheese")) {
// У сыра дополнительных параметров нет
                breakfast.add(new Cheese()); // В массив завтраков добавляем Chees
            } else if (parts[0].equals("Apple")) {
// У яблока – 1 параметр, который находится в parts[1]
            breakfast.add(new Apple(parts[1], 40));
            } else if (parts[0].equals("ChewingGum")) {
                breakfast.add(new ChewingGum(parts[1], 5));
            }
// ... Продолжается анализ других продуктов для завтрака
        }
        // Подсчет одинаковых продуктов
        int counter = 0;
        ChewingGum chewi = new ChewingGum("Watermelon", 5);
        for (Food item : breakfast) {
            if (item == null) {
                break;
            }

            if (item.equals(chewi)) {
                counter++;
            }
        }
        System.out.println("Всего жевачек со вкусом арбуза " + counter);
        int sumCalories = 0;
        if (isCalculateCalories) {
            for (Food item : breakfast) {
                if (item == null) {
                    break;
                }
                sumCalories += item.calculateCalories();
            }
            System.out.println("В завтраке каллорий " + sumCalories);
        }
        if (isSort){
            breakfast.sort(new Comparator<Food>() {
                @Override
                public int compare(Food o1, Food o2) {
                    if(o1.calories < o2.calories)
                        return 1;
                    else if (o1.calories == o2.calories ) {
                        return 0;
                    }
                    return -1;
                }
            });
            System.out.println(breakfast);
        }

// Перебор всех элементов массива
        for (Food item : breakfast) // у класса Food есть метод consumable
            if (item != null)
// Если элемент не null – употребить продукт
                item.consume(); // Метод для поедания
            else
// Если дошли до элемента null – значит достигли конца
// списка продуктов, ведь 20 элементов в массиве было
// выделено с запасом, и они могут быть не
// использованы все
                break;
        System.out.println("Всего хорошего!");

        /*ChewingGum F = new ChewingGum("123");
        ChewingGum F1 = new ChewingGum("123");
        System.out.println(F.equals(F1));*/
    }
}

