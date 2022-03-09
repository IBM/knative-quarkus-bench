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
    public RetVal server_reply(Param s) {

        String address = s.getServer_address();
        int port = s.getServer_port();
	Integer ip = port;

	int repetitions = Integer.valueOf(s.getRepetitions());

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
	        i += 1;
		line = in.readUTF();
		readSize += line.length();
//		System.out.println("Repetitions= "+repetitions+" i= "+i+" line= "+line+" readSize= "+readSize);
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

        RetVal retVal = new RetVal();
        retVal.result = Map.of(     "result", new String(line),
			            "size", rs.toString());
        retVal.measurement = Map.of("process_time", processTimeEnd - processTimeBegin);

        return retVal;
    }

    public static class Param {
        String request_id;
        String server_address;
        int server_port;
        int repetitions;
        String output_bucket;
        String income_timestamp;

        public String getRequest_id() { return request_id; }
        public void setRequest_id(String request_id) { this.request_id = request_id; }
        public String getServer_address() { return server_address; }
        public void setServer_address(String server_address) { this.server_address = server_address; }
        public int getServer_port() { return server_port; }
        public void setServer_port(int server_port) { this.server_port = server_port; }
        public int getRepetitions() { return repetitions; }
        public void setRepetitions(int repetitions) { this.repetitions = repetitions; }
        public String getOutput_bucket() { return output_bucket; }
        public void setOutput_bucket(String output_bucket) { this.output_bucket = output_bucket; }
        public String getIncome_timestamp() { return income_timestamp; }
        public void setIncome_timestamp(String income_timestamp) { this.income_timestamp = income_timestamp; }
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
