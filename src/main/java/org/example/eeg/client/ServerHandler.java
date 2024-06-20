package org.example.eeg.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerHandler {
    private Scanner input;
    private PrintWriter output;
    private Socket socket;
    public ServerHandler(String hostName, int port) throws IOException {
        socket=new Socket(hostName, port);
        this.input=new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(),true);
    }
    public void send(String message){
        output.println(message);
    }
    public void close(){
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
