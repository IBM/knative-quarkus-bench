package com.ibm.trl.funqy.micro;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Stream;

import java.lang.Integer;
import java.lang.String;

import com.ibm.trl.funqy.cosutils.COSUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import io.quarkus.funqy.Funq;

public class NetworkBenchmark {
    @Funq("Hello")
    public String helloOnly() {
        return "Hello!";
    }

    @Funq
    public String hello(String msg) {
        return "Hello " + msg;
    }

    @Funq
    public RetVal network_benchmark(MySocket s) {
        String newLine = System.getProperty("line.separator");
        String request_id = s.getRequest_id();
        String address = s.getServer_address();
        int port = Integer.valueOf(s.getServer_port());
        Integer ip = port;
        int repetitions = Integer.valueOf(s.getRepetitions());
        String output_bucket = s.getOutput_bucket();

//	System.out.println("Starting echo server");
	// start echo server: THIS DEPENDS ON NCAT BEING INSTALLED!!!
	Process proc = null;
        try {
	    String [] cmd = {"/usr/bin/ncat","-l",ip.toString(),"--keep-open","--exec","/usr/bin/cat"};
	    proc = Runtime.getRuntime().exec(cmd);
	    TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }


        int i = 0;
	int size = 0;

	System.out.println("Starting processing loop");
	long processBegin = System.nanoTime();

        try {
	    DataOutputStream out = null;
	    DataInputStream in = null;
            Socket socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(5000);
            socket.setReuseAddress(true);

//            byte[] message = new byte[0];
//            message = request_id.getBytes("UTF-8");

	    out = new DataOutputStream(socket.getOutputStream());
	    in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));


            int consecutive_failures = 0;
            long send_begin = 0;
            long recv_end = 0;
	    String line = "";

            while (i < repetitions + 1) {
//		System.out.println("Starting repetition "+i);
                try {
                    send_begin = System.nanoTime();
		    out.writeUTF(request_id+newLine);
		    out.flush();
                    line = in.readUTF();
		    size += line.length();
                    recv_end = System.nanoTime();
                } catch (SocketTimeoutException e) {
                    System.out.println("Time out "+consecutive_failures);
                    i += 1;
                    consecutive_failures += 1;
                    if (consecutive_failures == 5) {
                        System.out.println("Can not set up connection");
                        e.printStackTrace();
                        break;
                    }
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                i += 1;
                consecutive_failures = 0;
                socket.setSoTimeout(2000);
            }
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

	long processEnd = System.nanoTime();

	proc.destroy();

	Integer ii = i;
	Integer isize = size;

        RetVal retVal = new RetVal();
        retVal.result = Map.of(     "actualReps", ii.toString(),
                                    "size", isize.toString());
        retVal.measurement = Map.of("process_time", processEnd - processBegin);

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
