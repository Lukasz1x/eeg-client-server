package org.example.eeg.client;

import org.example.eeg.databasecreator.Creator;
import org.example.eeg.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest
{
    private static Server server;
    private static Creator creator;
    private static final String url = "jdbc:sqlite:Z:\\Mój dysk\\Studia\\2 semestr\\Programowanie Obiektowe\\powtorzenie-pierwszy\\eeg.db";

    @BeforeAll
    public static void start()
    {
        creator=new Creator();
        creator.create(url);
        server=new Server();
        server.setURL(url);
        new Thread(()-> {
            try {
                server.start(2137);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @AfterAll
    public static void  stop()
    {
        server.stop();
        //creator.delete(url);
    }


    @ParameterizedTest
    @MethodSource("linesCsv")
    public void clientTest(String username, String filepath, int electrode_number, String image)
    {
        Client client =new Client();
        client.sendData(username,filepath);
        String imagefromdb = getImage(username, electrode_number);
        assertEquals(image, imagefromdb);
    }

    public String getImage(String username, int electrode_number)
    {
        String image = null;
        String sql = "SELECT image FROM user_eeg WHERE username = ? AND electrode_number = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, electrode_number);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    image = rs.getString("image");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return image;
    }

    static Stream<Arguments> linesCsv()
    {
        List<Arguments> argumentsList = new ArrayList<>();
        try {
            BufferedReader bufferedReader =new BufferedReader(new FileReader("Z:\\Mój dysk\\Studia\\2 semestr\\Programowanie Obiektowe\\powtorzenie-pierwszy\\test.csv"));
            String line=bufferedReader.readLine();
            while((line= bufferedReader.readLine())!=null)
            {
                String[] parts = line.split(",");
                argumentsList.add(Arguments.of(parts[0],
                        parts[1],
                        Integer.parseInt(parts[2]),
                        parts[3]));
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return argumentsList.stream();
    }
}
