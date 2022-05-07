package com.feifei.httpserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Handler extends Thread {
    Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream input = this.socket.getInputStream()) {
            try (OutputStream ouput = this.socket.getOutputStream()) {
                handle(input, ouput);
            }
        } catch (IOException e) {
            try {
                this.socket.close();
            } catch (IOException io) {
                System.out.println("client disconnected");
            }
        }
    }

    private void handle(InputStream input, OutputStream output) throws IOException {
        System.out.println("process new http request...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));

        boolean requestOK = false;
        String first = reader.readLine();
        if (first.startsWith("GET / HTTP/1.")) {
            requestOK = true;
        }

        for (;;) {
            String header = reader.readLine();
            if (header.isEmpty()) {
                break;
            }
            System.out.println(header);
        }

        System.out.println(requestOK ? "Response OK" : "Response Error");
        if (!requestOK) {
            // 发送错误的响应
            writer.write("HTTP/1.0 404 Not Found/r/n");
            writer.write("Content-Length: 0/r/n");
            writer.write("/r/n");
            writer.flush();
        } else {
            // 发送成功响应:
            String data = "<html><body><h1>Hello, world!</h1></body></html>";
            int length = data.getBytes(StandardCharsets.UTF_8).length;
            writer.write("HTTP/1.0 200 OK\r\n");
            writer.write("Connection: close\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + length + "\r\n");
            writer.write("\r\n"); // 空行标识Header和Body的分隔
            writer.write(data);
            writer.flush();
        }
    }
}
