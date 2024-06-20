package org.example.eeg.server;

import org.example.eeg.databasecreator.Creator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {
    private static String url = "jdbc:sqlite:Z:\\Mój dysk\\Studia\\2 semestr\\Programowanie Obiektowe\\eeg.db";
    public static void main(String[] args)
    {
        //Creator database = new Creator();
        //database.delete(url);
        //database.create(url);
        try {
            new Server().start(2137);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setURL(String newurl)
    {
        url = newurl;
    }

    private ServerSocket serverSocket;
    private Map<String, ClientHandler> handlers = new HashMap<>();
    boolean running;

    public void start(int port) throws IOException {
        System.out.println("Server started");
        running=true;
        serverSocket=new ServerSocket(port);
        while(running)
        {
            Socket socket = serverSocket.accept();
            ClientHandler handler= new ClientHandler(socket, this);
            Thread thread = new Thread(handler);
            thread.start();
        }
    }

    public void stop()
    {
        running=false;
    }

    public void addClient(String login, ClientHandler handler)
    {
        handlers.put(login, handler);
        printLogins();
    }

    public void removeClient(String login)
    {
        handlers.remove(login);
    }

    public void printLogins()
    {
        System.out.println(handlers.keySet());
    }

    public void parseCSVLine(String line, String username, int number)
    {
        System.out.println("Processing line from " + username + ", line: " + line);
        String[] data =  line.split(",");
        double[] dataD = Arrays.stream(data).mapToDouble(Double::parseDouble).toArray();
        //System.out.println(Arrays.toString(dataD));
        //System.out.println(getBase64(dataD));
        insertIntoDB(username, number, getBase64(dataD));

    }

    private String getBase64(double[] points)
    {
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0,0,200,100);
        graphics2D.setColor(Color.RED);

        for(int i =0; i< points.length; i++)
        {
            //System.out.println(i + " " +(int) Math.round(50+points[i]));
            graphics2D.fillRect(i, (int) (50-points[i]),1,1);
        }
        graphics2D.dispose();
        String imageString;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image,"png",byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            Base64.Encoder encoder = Base64.getEncoder();
            byteArrayOutputStream.close();
            imageString=encoder.encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return imageString;
    }

    private void insertIntoDB(String username, int number, String image)
    {
        String insertSQL = "INSERT INTO user_eeg (username, electrode_number, image) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url))
        {
             PreparedStatement addUserStatement = connection.prepareStatement(insertSQL);
             addUserStatement.setString(1, username);
             addUserStatement.setInt(2, number);
             addUserStatement.setString(3, image);
             addUserStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }


}
