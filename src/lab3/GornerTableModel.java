package lab3;

import javax.swing.table.AbstractTableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Ключевое слово extends в
// Java используется для создания подклассов,
// которые наследуют свойства и методы из суперклассов
public class GornerTableModel extends AbstractTableModel {
    private final Double from;
    private final Double to;
    private final Double step;
    private final Double[] coeff;
    private final Object[][] table;
    private final int numberOfRows;

    public GornerTableModel(Double from,
                            Double to,
                            Double step,
                            Double[] coefficients
    ) {
        this.from = from;
        this.to = to;
        this.step = step;
        numberOfRows = (int) Math.ceil((to - from)/step) + 1;
        coeff = coefficients;
        table = new Object[numberOfRows][3];

        precalculate();
    }

    public Double getFrom() {
        return from;
    }

    public Double getTo() {
        return to;
    }

    public Double getStep() {
        return step;
    }

    public int getColumnCount() {
// В данной модели три столбца
        return 3;
    }

    /*Количество строк в таблице зависит от длины интервала табулирования
    и размера шага, поэтому его необходимо вычислять:*/

    public int getRowCount() {
// Вычислить количество точек между началом и концом отрезка
// исходя из шага табулирования
        return Double.valueOf(numberOfRows).intValue();
    }

    public Object getValueAt(int row, int col) {
// Вычислить значение X как НАЧАЛО_ОТРЕЗКА + ШАГ*НОМЕР_СТРОКИ
        return table[row][col];
    }

    public String getColumnName(int col) {
        return switch (col) {
            case 0 -> "Значение X";
            case 1 -> "Значение многочлена";
            case 2 -> "Разносторонние";
            default -> "";
        };
    }

/*    public Class<?> getColumnClass(int col) {
        return Double.class;
    }*/

    public Class<?> getColumnClass(int col) {
        //И в 1-ом и во 2-ом столбце находятся значения типа Double
        if (col != 2)
            return Double.class;
        else {
            return Boolean.class;
        }
    }

    private double calculate(double x, Double[] coeff) {

        Double[] gornerElems = new Double[coeff.length];
        gornerElems[0] = coeff[0];
        for (int i = 1; i < gornerElems.length; i++) {
            gornerElems[i] = gornerElems[i-1] * x + coeff[i];
        }
        return gornerElems[gornerElems.length - 1];
    }

    private void precalculate() {
        for (int i = 0; i < numberOfRows; i++) {
            table[i][0] = from + step * i;
            table[i][1] = calculate((Double) table[i][0], coeff);
            table[i][2] = Equilateral((Double) table[i][1]);
        }
    }

  /*  private boolean Equilateral(double num)
    {
        String[] check = Double.toString(num).split("\\."); // массив из двух элементов (до и после точки)
        return checkEven(check[0]) && checkOdd(check[1]) || checkOdd(check[0]) && checkEven(check[1]);
    }*/

   /* private boolean Equilateral(double num)
    {

        String[] check = Double.toString(num).split("\\."); // массив из двух элементов (до и после точки)
        System.out.println(check[0] + "   " + check[1]);
        double fractionalPart = Double.parseDouble(check[1]);
        return checkEven(check[0]) && checkOdd(check[1]) || checkOdd(check[0]) && checkEven(check[1]);
    }
*/
    private boolean Equilateral(double num)
    {

        String[] check = Double.toString(num).split("\\."); // массив из двух элементов (до и после точки)
        System.out.println(check[0] + "   " + check[1]);
        double fractionalPart = Double.parseDouble(check[1]);
        return checkEven(check[0]) && checkOdd(Double.toString(fractionalPart).substring(0,3)) || checkOdd(check[0]) && checkEven(Double.toString(fractionalPart).substring(0,3));
    }

    // Метод allMatch в Java является методом потока,
    // который используется для проверки, соответствует ли
    // каждый элемент этого потока предоставленному условию.
    // Этот метод возвращает true, если все элементы потока соответствуют предикату,
    // и false в противном случае.

    // Метод для проверки, что все цифры в строке четные
    // Преобразует в поток
    private boolean checkEven(String str) {
        return str.chars().allMatch(ch -> (ch - '0') % 2 == 0);
    }

    // Метод для проверки, что все цифры в строке нечетные
    private boolean checkOdd(String str) {
        return str.chars().allMatch(ch -> (ch - '0') % 2 != 0);
    }

}