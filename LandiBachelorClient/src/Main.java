import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

/**
 * Main Class, starts the Client Application
 */
public class Main {

    static AccessibleFilesWindow filesWindow = new AccessibleFilesWindow();
    static LoginWindow loginWindow = new LoginWindow();
    static VisibleDeviceWindow visibleDeviceWindow = new VisibleDeviceWindow();
    static final JFrame frame = new JFrame();
    private static ClientConnection curCon;

    /**
     * starts the client application
     * adds all functionality to the User Interface
     * calls the loadDevices() function
     * @param args
     * @see Main
     */
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame.setContentPane(visibleDeviceWindow.getPanel());
        frame.setSize(500, 500);
        frame.setTitle("Visible Devices");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        loadDevices();

        filesWindow.getBackButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.setContentPane(visibleDeviceWindow.getPanel());
                frame.setTitle("Visible Devices");
                frame.setVisible(true);
            }
        });
        filesWindow.getAuthenticateRegisterButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               frame.setContentPane(loginWindow.getPanel());
                frame.setTitle("Login");
                frame.setVisible(true);

            }
        });

        filesWindow.getUploadFileButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Thread(){
                    @Override
                    public void run() {
                        JFileChooser chooser = new JFileChooser();
                        int returnVal = chooser.showOpenDialog(frame);
                        if(returnVal == JFileChooser.APPROVE_OPTION){
                            try {
                                curCon.uploadFile(chooser.getSelectedFile());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                }.start();
            }
        });



        filesWindow.getList1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Thread (){
                    @Override
                    public void run (){
                        String fileName = (String) ((JList) e.getSource()).getSelectedValue();
                        JFileChooser chooser = new JFileChooser() {
                            @Override
                            public void approveSelection() {
                                if (getSelectedFile().isDirectory()) {
                                    super.approveSelection();
                                } else {
                                    return;
                                }
                            }
                        };
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int returnVal = chooser.showOpenDialog(frame);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                curCon.fileDownload(fileName,chooser.getSelectedFile());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                        } else {
                            return;
                        }
                    }
                }.start();
            }
        });

        visibleDeviceWindow.getCloseButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });

        visibleDeviceWindow.getList1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    Main.connectTo(((JList<DeviceRepresentation>)e.getSource()).getSelectedValue());
                }
            }

        });

        loginWindow.getRegisterButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    String user = loginWindow.getTextField1().getText();
                    String password = loginWindow.getTextField2().getText();
                    curCon.registerUser(user, password);
                    frame.setContentPane(filesWindow.getPanel());
                    frame.setTitle("Available Files");
                    frame.setVisible(true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
        loginWindow.getAuthenticateButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String user = loginWindow.getTextField1().getText();
                String password = loginWindow.getTextField2().getText();
                try {
                    curCon.authenticateUser(user, password);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                frame.setContentPane(filesWindow.getPanel());
                frame.setTitle("Available Files");
                frame.setVisible(true);
            }
        });
        loginWindow.getBackButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.setContentPane(visibleDeviceWindow.getPanel());
                frame.setTitle("Visible Devices");
                frame.setVisible(true);
            }
        });


    }

    /**
     * establishes a connection to the server with the selected name
     * @param selectedValue
     * @see Main
     */
    private static void connectTo(final DeviceRepresentation selectedValue) {

        new Thread(){
            @Override
            public void run() {
                try {
                    frame.setVisible(false);
                    StreamConnection connection = (StreamConnection) Connector.open("btspp://" + selectedValue.device.getBluetoothAddress() + ":"  + "1;authenticate=false;encrypt=false;master=true;");
                    curCon = new ClientConnection(selectedValue,connection);
                    frame.setContentPane(filesWindow.getPanel());
                    frame.setVisible(true);
                    curCon.runFileFetchBackground();

                } catch (IOException e) {
                    e.printStackTrace();
                    frame.setVisible(true);
                    JOptionPane.showMessageDialog(frame, "Error while connecting occurred", "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.start();

    }

    /**
     * loads all the bluetoothdevices available
     * @see Main
     */
    private static void loadDevices() {
        try {

            final LocalDevice device = LocalDevice.getLocalDevice();
            device.setDiscoverable(DiscoveryAgent.GIAC);
            DiscoveryAgent agent = device.getDiscoveryAgent();
            final Vector<DeviceRepresentation> deviceList = new Vector<DeviceRepresentation>();
            agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {


                @Override
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                        DeviceRepresentation rep = new DeviceRepresentation(btDevice,cod);
                        deviceList.add(rep);
                }

                @Override
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                    throw new RuntimeException(transID + " how did you get here?");
                }

                @Override
                public void serviceSearchCompleted(int transID, int respCode) {

                }

                @Override
                public void inquiryCompleted(int discType) {

                    visibleDeviceWindow.getList1().setListData(deviceList.toArray());
                    frame.repaint();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            
                        }
                    }.start();

                }
            });
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }


}

