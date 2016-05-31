

import javax.microedition.io.StreamConnection;
import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Client Connection handels all the different in- and outgoing messages
 * from and to the server
 * @see ClientConnection
 */
public class ClientConnection {

    DeviceRepresentation rep;
    DeviceRepresentation rep;
    StreamConnection connection;
    BufferedWriter writer;
    BufferedReader reader;
    final Semaphore sem = new Semaphore(1, true);

    public ClientConnection(DeviceRepresentation rep, StreamConnection connection) throws IOException {
        this.rep = rep;
        this.connection = connection;
        writer = new BufferedWriter((new OutputStreamWriter(connection.openOutputStream(), "UTF-8")));
        reader = new BufferedReader((new InputStreamReader(connection.openInputStream(), "UTF-8")));
    }

    /**
     * calls fetchFiles in a certain interval
     * @see ClientConnection
     * @see ClientConnection
     */
    public void runFileFetchBackground() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {

                        Main.filesWindow.getList1().setListData(fetchFiles().toArray());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * fetches all the available file of the server
     * @return list of all available Files in the Database
     * @throws IOException
     * @see ClientConnection
     */
    public List<String> fetchFiles() throws IOException {
        aquire();
        List<String> fileList = new ArrayList<String>();
        writer.write("list\n");
        writer.flush();
        String line = reader.readLine();
        if (line.contains("success")) {
            line = reader.readLine();
            for (String s : line.split(",")) {
                fileList.add(s);
            }
        }
        sem.release();
        return fileList;
    }

    /**
     * starts the semaphore
     * @see ClientConnection
     */
    private void aquire() {
        try {
            sem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * handels the download of a file into the given directory
     * @param fileName
     * @param directory
     * @throws IOException
     * @see ClientConnection
     */
    public void fileDownload(String fileName, File directory) throws IOException {
        aquire();
        writer.write("pull\n");
        writer.write(fileName+"\n");
            writer.flush();
            String line = reader.readLine();

        if(line.contains("push")){
            fileName = reader.readLine();

            String length = reader.readLine();

            try{
                int fileSize = Integer.parseInt(length);

                int read = 0;
                ByteBuffer buffer = ByteBuffer.allocate(fileSize);
                while(read < fileSize){
                    buffer.put((byte)reader.read());
                    read++;
                }
                File file = new File(directory, fileName);
                file.createNewFile();
                FileOutputStream fout = new FileOutputStream(file);
                fout.write(buffer.array());
                fout.flush();
                fout.close();

            }catch(NumberFormatException e){
                writer.write("Filetransfer failed");
                writer.flush();
            }
        }else if(line.contains("error")){
            JOptionPane.showMessageDialog(Main.frame, "Error while transfering Files", "Filetransfer Error", JOptionPane.ERROR_MESSAGE);
        }
        sem.release();

    }

    /**
     * handels the upload of a file onto the server
     * @param file
     * @throws IOException
     * @see ClientConnection
     */
    public void uploadFile(File file) throws IOException {
        aquire();
        writer.write("push\n");
        writer.write("file\n");
        writer.write(file.getName()+"\n");
        byte[] data = Files.readAllBytes(Paths.get(file.getPath()));

        char[] charData = new char[data.length];
        for(int i = 0; i < data.length; i++) {
            charData[i] = (char)data[i];
        }
        writer.write(charData.length+"\n");
        writer.write(charData);
        writer.flush();
        sem.release();
    }

    /**
     * handles the registration of a user on the server
     * @param name
     * @param pw
     * @throws IOException
     * @see ClientConnection
     */
    public void registerUser(String name, String pw) throws IOException {
        aquire();
        writer.write("push\nuser\n"+name+"\n"+pw+"\n");
        sem.release();

    }

    /**
     * handels the authentication of a already registered user on the server
     * @param user
     * @param password
     * @throws IOException
     * @see ClientConnection
     */
    public void authenticateUser(String user, String password) throws IOException {
        aquire();
        writer.write("auth\n"+user+"\n"+password+"\n");
        writer.flush();
        String line = reader.readLine();
        System.out.println(line);
        sem.release();
    }
}
