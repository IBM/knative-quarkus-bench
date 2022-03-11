package com.ibm.trl.serverlessbench;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import java.lang.Integer;
import java.lang.String;

import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.concurrent.TimeUnit;


import io.quarkus.funqy.Funq;

public class ServerReply {

    @Funq
    public RetValType server_reply(FunInput input) {

        String address = input.getServer_address();
        int port = input.getServer_port();
	Integer ip = port;

	int repetitions = Integer.valueOf(input.getRepetitions());

        // start echo server: THIS DEPENDS ON NCAT BEING INSTALLED!!!
        Process proc = null;
        try {
            String [] cmd = {"/usr/bin/sh","-c", "/usr/bin/yes chargenchargenchargen | /usr/bin/ncat -l "+ip.toString()+" --keep-open --send-only"};
            proc = Runtime.getRuntime().exec(cmd);
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long processTimeBegin = System.nanoTime();

        int readSize = 0;
	int i = 0;
	DataInputStream in = null;
	String line = "";
        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(100);
	    in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

	    while (i<repetitions) {
	        ++i;
		line = in.readUTF();
		readSize += line.length();
	    } 
            in.close();
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
            line = "Socket Exception";
        } catch (Exception e) {
            line = "Exception";
            e.printStackTrace();
        }

        long processTimeEnd = System.nanoTime();

        proc.destroy();

	Integer rs = readSize;

        RetValType retVal = new RetValType();
        retVal.result = Map.of(     "result", new String(line),
			            "size", rs.toString());
        retVal.measurement = Map.of("process_time", processTimeEnd - processTimeBegin);

        return retVal;
    }

    public static class FunInput {
        String server_address;
        int server_port;
        int repetitions;

        public String getServer_address() {
            return server_address;
        }

        public void setServer_address(String server_address) {
            this.server_address = server_address;
        }

        public int getServer_port() {
            return server_port;
        }

        public void setServer_port(int server_port) {
            this.server_port = server_port;
        }

        public int getRepetitions() {
            return repetitions;
        }

        public void setRepetitions(int repetitions) {
            this.repetitions = repetitions;
        }
    }

    public static class RetValType {
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
    }
}
