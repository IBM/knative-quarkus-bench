package com.ibm.trl.funqy.micro;

import java.util.HashMap;
import java.util.Arrays;

import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;
import java.net.SocketException;

import io.quarkus.funqy.Funq;

public class ServerReply {
    @Funq("Hello")
    public String helloOnly() {
        return "Hello!";
    }

    @Funq
    public String hello(String msg) {
        return "Hello " + msg;
    }

    @Funq
    public HashMap<String, String> server_reply(MySocket s) {
        HashMap<String, String> retVal = new HashMap<String, String>();
        
        String address = s.getServer_address();
        int port = s.getServer_port();

        byte[] msg = {};
        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(1);

            InputStream in = socket.getInputStream();
            int readSize = in.read(msg);
            msg = Arrays.copyOf(msg, readSize);

            in.close();
                 
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
            msg = "Connection failure".getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        retVal.put( "result", new String(msg, java.nio.charset.StandardCharsets.UTF_8));
        return retVal;
    }
}
