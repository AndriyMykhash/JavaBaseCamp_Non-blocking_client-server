package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Main {

    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(8090));
        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started...");

        while (true) {
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isValid()) {
                    try {
                        if (key.isAcceptable()) {
                            SocketChannel socketChannel = serverChannel.accept();
                            socketChannel.configureBlocking(false);
                            System.out.println("Connected " + socketChannel.getRemoteAddress());

                            sockets.put(socketChannel, ByteBuffer.allocate(50));
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            ByteBuffer buffer = sockets.get(socketChannel);
                            int bytesRead = socketChannel.read(buffer);
                            System.out.println("Reading from " + socketChannel.getRemoteAddress());

                            if (bytesRead == -1) {
                                System.out.println("Connection closed " + socketChannel.getRemoteAddress());
                                sockets.remove(socketChannel);
                                socketChannel.close();
                            }

                            if (bytesRead > 0 && buffer.get(buffer.position() - 1) == '\n') {
                                socketChannel.register(selector, SelectionKey.OP_WRITE);
                            }
                        } else if (key.isWritable()) {
                            int allocate = 50;
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            ByteBuffer.allocate(allocate);
                            ByteBuffer buffer = sockets.get(socketChannel);

                            buffer.flip();
                            String clientMessage = new String(buffer.array(), buffer.position(), buffer.limit());

                            String response = clientMessage.replace("\r\n", "") + ", server time to eat";

                            byte[] byteas = response.getBytes();

                            CharBuffer charBuffer = buffer.asCharBuffer();

                            int bytesWritten = socketChannel.write(buffer);

                            System.out.println("Writing to " + socketChannel.getRemoteAddress());
                            if (!buffer.hasRemaining()) {
                                buffer.compact();
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            }

            selector.selectedKeys().clear();
        }
    }

}