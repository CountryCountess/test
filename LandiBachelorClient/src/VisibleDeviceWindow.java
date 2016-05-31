
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;


public class VisibleDeviceWindow {
    private JList list1;

    private JButton closeButton;
    private JPanel panel;


    public VisibleDeviceWindow() {
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ((JList)e.getSource()).repaint();
            }
        });
    }

    public JList getList1() {
        return list1;
    }



    public JButton getCloseButton() {
        return closeButton;
    }

    public JPanel getPanel() {
        return panel;
    }

    private void createUIComponents() {

        list1 = new JList(new DefaultListModel());

    }
}
