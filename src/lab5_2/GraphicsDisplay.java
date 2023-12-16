package lab5_2;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JPanel;

import static java.lang.Math.abs;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    ///////////////////////////////
    private boolean showSpecialMarkers = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;


    private double[][] viewport = new double[2][2];

    private ArrayList<double[][]> undoHistory = new ArrayList(5);
    private int selectedMarker = -1;
    private ArrayList<Double[]> originalData;
    private double[] originalPoint = new double[2];
    private double scaleX;
    private double scaleY;
    private boolean scaleMode = false;
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();


    public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);

        float[] dashPattern = {
                26.0f, 4.0f,
                10.0f, 4.0f,
                10.0f, 4.0f,
                4.0f, 4.0f,
                26.0f, 4.0f,
        };
        graphicsStroke = new BasicStroke(
                3.0f, // ширина линии
                BasicStroke.CAP_BUTT,  // линии имеют плоские концы
                BasicStroke.JOIN_ROUND, // значает, что точки, где встречаются две линии, будут скруглены.
                10.0f,
                dashPattern, //Это массив, определяющий шаблон пунктирной линии. Если он null, линия будет сплошной.
                0.0f
        );

// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
       /* graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);*/
// Перо для рисования осей координат
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
        ////////////////////////////////////////////////


        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());

    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }

    // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    ///////////////////////////////////
    public void setShowSpecialMarkers(boolean showSpecialMarkers) {
        this.showSpecialMarkers = showSpecialMarkers;
        repaint();
    }

    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g) {
        /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
         * Эта функциональность - единственное, что осталось в наследство от
         * paintComponent класса JPanel
         */
        super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
        if (graphicsData == null || graphicsData.length == 0) return;
// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
// Это необходимо для определения области пространства, подлежащей отображению
// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
// Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
и Y - сколько пикселов
* приходится на единицу длины по X и по Y
*/
        scaleX = getSize().getWidth() / (viewport[1][0] - viewport[0][0]);
        scaleY = getSize().getHeight() / (viewport[0][1] - viewport[1][1]);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
// Выбираем за основу минимальный
//        scale = Math.min(scaleX, scaleY);
// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
/* Если за основу был взят масштаб по оси X, значит по оси Y
делений меньше,
* т.е. подлежащий визуализации диапазон по Y будет меньше
высоты окна.
* Значит необходимо добавить делений, сделаем это так:
* 1) Вычислим, сколько делений влезет по Y при выбранном
масштабе - getSize().getHeight()/scale
* 2) Вычтем из этого сколько делений требовалось изначально
* 3) Набросим по половине недостающего расстояния на maxY и
minY
//*/
//        if (scale == scaleX) {
//            double yIncrement = (getSize().getHeight() / scale - (maxY -
//                    minY)) / 2;
//            maxY += yIncrement;
//            minY -= yIncrement;
//        }
//        if (scale == scaleY) {
//// Если за основу был взят масштаб по оси Y, действовать по аналогии
//            double xIncrement = (getSize().getWidth() / scale - (maxX -
//                    minX)) / 2;
//            maxX += xIncrement;
//            minX -= xIncrement;
//        }
// Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.
        if (showAxis) paintAxis(canvas);
// Затем отображается сам график
        paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.


        if (showMarkers) paintMarkers(canvas);
// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);


        // ОТОБРАЖЕНИЕ СПЕЦИАЛЬНЫХ ТОЧЕК
        if (showSpecialMarkers) paintSpecialMarkers(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);

        canvas.draw(selectionRect);
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
// Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
// Выбрать цвет линии
        canvas.setColor(Color.RED);
/* Будем рисовать линию графика как путь, состоящий из множества
сегментов (GeneralPath)
* Начало пути устанавливается в первую точку графика, после чего
прямой соединяется со
* следующими точками
*/
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
// Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i > 0) {
// Не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
// Первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
// Отобразить график
        canvas.draw(graphics);
    }


    private Point2D.Double constructPoint(
            Point2D.Double point,
            double dx,
            double dy
    ) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(
                point.getX() + dx,
                point.getY() + dy
        );
        return dest;
    }


    // ОТРИСОВКА ОСОБЫХ ТОЧЕК
    protected void paintSpecialMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        for (int i = 0; i < graphicsData.length; i++) {
            Double[] point = graphicsData[i];
            if (i + 1 < graphicsData.length && point[1] * graphicsData[i + 1][1] < 0) {
                double x = (abs(point[1]) * graphicsData[i + 1][0] + abs(graphicsData[i + 1][1]) * point[0]) / (abs(point[1]) + abs(graphicsData[i + 1][1]));
                var targetPoint = xyToPoint(x, 0);
                canvas.draw(new Ellipse2D.Double(targetPoint.getX() - 10, targetPoint.getY() - 10, 20, 20));
            }
        }
    }


    public boolean isOdd(int n) {
        return (n & 1) == 1;
    }

    // Отображение маркеров точек, по которым рисовался график
    protected void paintMarkers(Graphics2D canvas) {
// Шаг 1 - Установить специальное перо для черчения контуров
//        маркеров
        canvas.setStroke(markerStroke);

// Шаг 2 - Организовать цикл по всем точкам графика

        for (int i = 0; i < graphicsData.length; i++) {
            Double[] point = graphicsData[i];
            if (selectedMarker == i) {
                canvas.setColor(Color.BLACK);
                Point2D.Double pointInWindowCoords = xyToPoint(point[0], point[1]);
                DecimalFormat format = new DecimalFormat("#.##");
                canvas.drawString("X: " + format.format(point[0]) + " Y:" + format.format(point[1]),
                        (int) pointInWindowCoords.getX(), (int) pointInWindowCoords.getY());
            }

/* Эллипс будет задаваться посредством указания координат
его центра
и угла прямоугольника, в который он вписан */
// Центр - в точке (x,y)

// Задать эллипс по центру и диагонали
            canvas.setColor(Color.BLACK);
            if (isOdd((int) Math.floor(point[1]))) {
//                System.out.`println(Arrays.toString(point));
                canvas.setColor(Color.YELLOW);
            }


            Line2D.Double horizontalLine = new Line2D.Double();
            horizontalLine.setLine(

                    constructPoint(xyToPoint(point[0], point[1]), 5, 0),
                    constructPoint(xyToPoint(point[0], point[1]), -5, 0)
            );

            Line2D.Double verticalLine = new Line2D.Double();
            verticalLine.setLine(
                    constructPoint(xyToPoint(point[0], point[1]), 0, 5),
                    constructPoint(xyToPoint(point[0], point[1]), 0, -5)
            );

            Line2D.Double horizontalThorn1 = new Line2D.Double();
            horizontalThorn1.setLine(

                    constructPoint(xyToPoint(point[0], point[1]), -5, 2),
                    constructPoint(xyToPoint(point[0], point[1]), -5, -2)
            );

            Line2D.Double verticalThorn1 = new Line2D.Double();
            verticalThorn1.setLine(
                    constructPoint(xyToPoint(point[0], point[1]), 2, -5),
                    constructPoint(xyToPoint(point[0], point[1]), -2, -5)
            );

            Line2D.Double horizontalThorn2 = new Line2D.Double();
            horizontalThorn2.setLine(

                    constructPoint(xyToPoint(point[0], point[1]), 5, -2),
                    constructPoint(xyToPoint(point[0], point[1]), 5, 2)
            );

            Line2D.Double verticalThorn2 = new Line2D.Double();
            verticalThorn2.setLine(
                    constructPoint(xyToPoint(point[0], point[1]), 2, 5),
                    constructPoint(xyToPoint(point[0], point[1]), -2, 5)
            );

            canvas.draw(verticalLine);
            canvas.draw(horizontalLine);
            canvas.draw(horizontalThorn1);
            canvas.draw(verticalThorn1);
            canvas.draw(horizontalThorn2);
            canvas.draw(verticalThorn2);
        }
    }


    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
// а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                    xyToPoint(0, minY)));
// Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5,
                    arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10,
                    arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
// Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
// Стрелка оси X
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                    arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x", (float) (labelPos.getX() -
                    bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    /* Метод-помощник, осуществляющий преобразование координат.
    * Оно необходимо, т.к. верхнему левому углу холста с координатами
    * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
    где
    * minX - это самое "левое" значение X, а
    * maxY - самое "верхнее" значение Y.
    */
    //Перевод координат
    protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - viewport[0][0];   /// Храниться макс и мин Ч
// Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    /* Метод-помощник, возвращающий экземпляр класса Point2D.Double
     * смещѐнный по отношению к исходному на deltaX, deltaY
     * К сожалению, стандартного метода, выполняющего такую задачу, нет.
     */
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX,
                                        double deltaY) {
// Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Перевод коорднинат на графике в координаты на экране
    protected double[] translatePointToXY(int x, int y) {
        return new double[]{this.viewport[0][0] + (double) x / this.scaleX, this.viewport[0][1] - (double) y / this.scaleY};
    }


    public void displayGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;

        this.minX = graphicsData[0][0];  // массив всех точек
        this.maxX = graphicsData[graphicsData.length - 1][0];
        this.minY = graphicsData[0][1];
        this.maxY = this.minY;

        for (int i = 1; i < graphicsData.length; ++i) {
            if (graphicsData[i][1] < this.minY) {
                this.minY = graphicsData[i][1];
            }

            if (graphicsData[i][1] > this.maxY) {
                this.maxY = graphicsData[i][1];
            }
        }

        // Изменение маштаба, чтобы макс были на краях экрана
        this.zoomTo(this.minX, this.maxY, this.maxX, this.minY);
    }

    // Для подписывания точки. Она находит ближайшую точки на графике
    private int findSelectedPoint(double x, double y) {
        if (graphicsData == null) {
            return -1;
        }
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(
                    graphicsData[i][0],
                    graphicsData[i][1]
            );
            double distance = (point.getX() - x) *
                    (point.getX() - x) +
                    (point.getY() - y) *
                            (point.getY() - y);
            if (distance <= 100) {
                return i;
            }
        }
        return -1;
    }


    private void zoomTo(double x1, double y1, double x2, double y2) {
        zoomToNoUpdate(x1, y1, x2, y2); // изменяет крайние точки
        repaint();
    }

    private void zoomToNoUpdate(double x1, double y1, double x2, double y2) {
        viewport[0][0] = x1; // max min x Y
        viewport[0][1] = y1;
        viewport[1][0] = x2;
        viewport[1][1] = y2;
    }

    // Обработка событий мыши
    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        // если правая кнопка мыши, то она возвращает старый маштаб
        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                if (!undoHistory.isEmpty()) {  // Сохраняет историю координат для отмены увеличения (левый верхний, правый нижний3// )
                    viewport = undoHistory.get(undoHistory.size() - 1);
                    undoHistory.remove(undoHistory.size() - 1);
                } else {
                    zoomTo(minX, maxY, maxX, minY);
                }

                repaint();
            }

        }

        // Запоминает точку, где мы зажали, чтобы от нее рисовать
        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() == 1) {
                originalPoint = translatePointToXY(ev.getX(), ev.getY());

                scaleMode = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                selectionRect.setFrame((double) ev.getX(), (double) ev.getY(), 1.0, 1.0);
            }
        }

        // Отпустили кнопку, перестало рисовать окно, увеличивает
        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {
                setCursor(Cursor.getPredefinedCursor(0));
                selectionRect.setFrame(0, 0, 0, 0);
                scaleMode = false;
                double[] finalPoint = translatePointToXY(ev.getX(), ev.getY());
                undoHistory.add(viewport);
                viewport = new double[2][2];
                zoomTo(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
                repaint();
            }
        }
    }

    // Обработчик событий мыши при движении
    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        //изменение координат в движении мыши
        public void mouseMoved(MouseEvent ev) {
            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
            if (selectedMarker >= 0) {
                setCursor(Cursor.getPredefinedCursor(8));

            } else {
                setCursor(Cursor.getPredefinedCursor(0));
            }

            repaint();
        }

        // Когда зажали кнопку и ей двигаем, рисуем прямоугольник
        public void mouseDragged(MouseEvent ev) {

            if (scaleMode) {
                double width = (double) ev.getX() - selectionRect.getX(); //прямоугольник выделения
                if (width < 5.0) {
                    width = 5.0;
                }

                double height = (double) ev.getY() - selectionRect.getY();
                if (height < 5.0) {
                    height = 5.0;
                }

                selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
                repaint();
            }
        }
    }
}