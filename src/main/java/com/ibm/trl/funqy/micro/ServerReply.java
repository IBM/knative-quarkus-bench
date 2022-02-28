package com.ibm.trl.funqy.micro;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import java.lang.Integer;
import java.lang.String;

import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;


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
    public RetVal server_reply(MySocket s) {

        long processTimeBegin = System.nanoTime();



        String address = s.getServer_address();
        int port = s.getServer_port();
	Integer ip = port;


        // start echo server: THIS DEPENDS ON NCAT BEING INSTALLED!!!
        Process proc = null;
        try {
            String [] cmd = {"/usr/bin/sh","-c", "/usr/bin/yes chargenchargenchargen | /usr/bin/ncat -l "+ip.toString()+" --keep-open --send-only"};
            proc = Runtime.getRuntime().exec(cmd);
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] msg = {};
        int readSize = 0;
        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(1);

            InputStream in = socket.getInputStream();
            readSize = in.read(msg);
            msg = Arrays.copyOf(msg, readSize);

            in.close();
                 
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
            msg = "Connection failure".getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long processTimeEnd = System.nanoTime();

        proc.destroy();

	Integer rs = readSize;

        RetVal retVal = new RetVal();
        retVal.result = Map.of(     "result", new String(msg, java.nio.charset.StandardCharsets.UTF_8),
			            "size", rs.toString());
        retVal.measurement = Map.of("process_time", processTimeEnd - processTimeBegin);

        return retVal;
    }

    public static class RetVal {
        Map<String, String> result;
        Map<String, Long> measurement;

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public Map<String, Long> getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Map<String, Long> measurement) {
            this.measurement = measurement;
        }

        RetVal() {
            measurement = new HashMap<String, Long>();
        }
    }


}
