package com.ibm.trl.knativebench;

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

import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;


public class ServerReply {
    static double nanosecInSec = 1_000_000_000.0;

    @Inject
    Logger log;
    
    @Funq("server-reply")
    public RetValType server_reply(FunInput input) {
        String address = input.getServer_address();
        int port = input.getServer_port();

        // start echo server: THIS DEPENDS ON NCAT BEING INSTALLED!!!
        Process proc = null;
        try {
            String [] cmd = {"/usr/bin/sh","-c", "/usr/bin/yes chargenchargenchargen | /usr/bin/ncat -l " + port + " --keep-open --send-only"};
            proc = Runtime.getRuntime().exec(cmd);
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            System.out.println("Server didn't launch: "+e.toString());
        }

        long processTimeBegin = System.nanoTime();

        int readSize = 0;
	String line = "";
        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(100);
	    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	    readSize = in.read(new byte[1024]);
            in.close();
            socket.close();
        } catch (SocketException e) {
            line = "SocketException: "+e.toString();
        } catch (IOException e) {
            line = "IOException: "+e.toString();
        }

        long processTimeEnd = System.nanoTime();

        proc.destroy();

        RetValType retVal = new RetValType();
        retVal.result = Map.of(     "result", line,
			            "size", readSize + "");
        retVal.measurement = Map.of("process_time", (processTimeEnd - processTimeBegin)/nanosecInSec);
        
        log.info("retVal.measurement="+retVal.measurement.toString());

        return retVal;
    }

    public static class FunInput {
        String server_address;
        int server_port;

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
    }

    public static class RetValType {
        Map<String, String> result;
        Map<String, Double> measurement;

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public Map<String, Double> getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Map<String, Double> measurement) {
            this.measurement = measurement;
        }
    }
}
