package lab5_2;



    //����� �������� ���� ���������� MainFrame
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    // ��������� ������� ���� ����������
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    // ������ ����������� ���� ��� ������ ������
    private JFileChooser fileChooser = null;
    // ������ ����
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;

    private JCheckBoxMenuItem showMarkersMenu;
    // ���������-������������ �������
    private GraphicsDisplay display = new GraphicsDisplay();
    // ����, ����������� �� ������������� ������ �������
    private boolean fileLoaded = false;

    public MainFrame() {
// ����� ������������ ������ Frame
        super("���������� �������� ������� �� ������ ������� �������������� ������");
// ��������� �������� ����
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
// �������������� ���� ���������� �� ������
        setLocation((kit.getScreenSize().width - WIDTH) / 2,
                (kit.getScreenSize().height - HEIGHT) / 2);
// ����?�������� ���� �� ���� �����
        setExtendedState(MAXIMIZED_BOTH);
// ������� � ���������� ������ ����
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
// �������� ����� ���� "����"
        JMenu fileMenu = new JMenu("����");
        menuBar.add(fileMenu);
// ������� �������� �� �������� �����
        Action openGraphicsAction = new AbstractAction("������� ���� �  ��������") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());
            }
        };

// �������� ��������������� ������� ����
        fileMenu.add(openGraphicsAction);
        // ������� ����� ���� "������"
        JMenu graphicsMenu = new JMenu("������");
        menuBar.add(graphicsMenu);
        // ������� �������� ��� ������� �� ��������� �������� "���������� ��� ���������"
        Action showAxisAction = new AbstractAction("���������� ��� ���������") {
            public void actionPerformed(ActionEvent event) {
// �������� showAxis ������ lab4.GraphicsDisplay ������, ���� ������� ����
// showAxisMenuItem ������� �������, � ���� - � ��������� ������
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
// �������� ��������������� ������� � ����
        graphicsMenu.add(showAxisMenuItem);
// ������� �� ��������� ������� (������� �������)
        showAxisMenuItem.setSelected(true);
        // ��������� �������� ��� �������� "���������� ������� �����"
        Action showMarkersAction = new AbstractAction("���������� ������� �����") {
            public void actionPerformed(ActionEvent event) {
// �� �������� � showAxisMenuItem
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
// ������� �� ��������� ������� (������� �������)
        showMarkersMenuItem.setSelected(true);





        // ������ ������ �����
        Action showMarkers = new AbstractAction("���������� ������� ������ �����") {
            public void actionPerformed(ActionEvent event) {

                display.setShowSpecialMarkers(showMarkersMenu.isSelected());
            }
        };
        showMarkersMenu = new JCheckBoxMenuItem(showMarkers);
        graphicsMenu.add(showMarkersMenu);
        showMarkersMenu.setSelected(true);











// ���������������� ���������� �������, ��������� � ���� "������"
        graphicsMenu.addMenuListener(new GraphicsMenuListener());
// ���������� lab4.GraphicsDisplay � ���� ��������� ����������
        getContentPane().add(display, BorderLayout.CENTER);
    }

    // ���������� ������ ������� �� ������������� �����
    protected void openGraphics(File selectedFile) {
        try {
// ��� 1 - ������� ����� ������ ������, ��������� � ������� �������� �������
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
/* ��� 2 - ���� ���?� ������ � ������ ����� ����� ���������,
* ������� ������ ����� ��������������� � �������:
* ����� ���� � ������ - in.available() ����;
* ������ ������ ����� Double - Double.SIZE ���, ���
Double.SIZE/8 ����;
* ��� ��� ����� ������������ ������, �� ����� ��� ������ �
2 ����
*/
            Double[][] graphicsData = new Double[in.available() / (Double.SIZE / 8) / 2][];
// ��� 3 - ���� ������ ������ (���� � ������ ���� ������)
            int i = 0;
            while (in.available() > 0) {
// ������ �� ������ �������� ���������� ����� X
                Double x = in.readDouble();
// ����� - �������� ������� Y � ����� X
                Double y = in.readDouble();
// ����������� ���� ��������� ����������� � ������
                graphicsData[i++] = new Double[]{x, y};
            }
// ��� 4 - ��������, ������� �� � ������ � ���������� ������ ���� �� ���� ���� ���������
            if (graphicsData != null && graphicsData.length > 0) {
// �� - ���������� ���� ������������� ������
                fileLoaded = true;
// �������� ����� ����������� �������
                display.displayGraphics(graphicsData); ////////////////////////////
            }
// ��� 5 - ������� ������� �����
            in.close();
        } catch (FileNotFoundException ex) {
// � ������ �������������� �������� ���� "���� �� ������" �������� ��������� �� ������
            JOptionPane.showMessageDialog(MainFrame.this, "��������� ���� �� ������", "������ �������� ������", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException ex) {
// � ������ ������ ����� �� ��������� ������ �������� ��������� �� ������
            JOptionPane.showMessageDialog(MainFrame.this, "������ ������ ��������� ����� �� �����", "������ �������� ������",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    public static void main(String[] args) {
// ������� � �������� ��������� �������� ���� ����������
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // �����-��������� �������, ��������� � ������������ ����
    private class GraphicsMenuListener implements MenuListener {
        // ����������, ���������� ����� ������� ����
        public void menuSelected(MenuEvent e) {
// ����������� ��� ������������� ��������� ���� "������" ������������ �������������� ������
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            showMarkersMenu.setEnabled(fileLoaded);
        }

        // ����������, ���������� ����� ����, ��� ���� ������� � ������
        public void menuDeselected(MenuEvent e) {
        }

        // ����������, ���������� � ������ ������ ������ ������ ����(����� ������ ��������)
        public void menuCanceled(MenuEvent e) {
        }
    }
}
