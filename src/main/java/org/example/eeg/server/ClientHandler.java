package org.example.eeg.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable
{
    private String username;
    private Socket socket;
    private Server server;

    private Scanner input;

    private PrintWriter output;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.input = new Scanner(socket.getInputStream());
    }



    public void send(String message)
    {
        output.println(message);
    }


    @Override
    public void run() {
        username = input.nextLine();
        server.addClient(username, this);
        System.out.println("Server: " + username + " connected");
        String line;
        int i=0;
        do {
            line=input.nextLine();
            if(!line.equals("Bye")) {
                server.parseCSVLine(line, username, i);
                i++;
            }
        }while (!line.equals("Bye"));
        server.removeClient(username);
        System.out.println("Server: " + username + " disconnected");
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
