package org.example.eeg.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Client
{
    public static void main(String[] args) {
        Client client = new Client();
        String username;
        String pathfile;
        Scanner scanner = new Scanner(System.in);
        username = scanner.nextLine();
        pathfile = scanner.next();
        client.sendData(username, pathfile);
    }

    public void sendData(String name, String filepath){
        ServerHandler serverHandler;


        try {
            serverHandler = new ServerHandler("localhost", 2137);
            serverHandler.send(name);
            //wczytywanie danych z pliku
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            List<String> lines =new ArrayList<>();
            String line;


            while((line=br.readLine())!=null)
            {
                lines.add(line);
                //System.out.println(line);
            }

            for(String l : lines)
            {
                serverHandler.send(l);
                sleep(2000);
            }


            serverHandler.send("Bye");
            serverHandler.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
