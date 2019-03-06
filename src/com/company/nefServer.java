package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class nefServer {

    public static void main(String... args) throws IOException {

        String host = "localhost";
        int port = 8090;
        String message = "This message is send by some user...";
        SocketChannel socket = SocketChannel.open(new InetSocketAddress(host, port));

        try {
            PrintWriter writer = new PrintWriter(socket.socket().getOutputStream());
            writer.println(message);
            writer.flush();
            System.out.println("send > " + message + " " + socket.isBlocking());

            Thread.sleep(1000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.socket().getInputStream()));

            Thread.sleep(1000);

            String response = reader.readLine();
            System.out.println("received < " + response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}