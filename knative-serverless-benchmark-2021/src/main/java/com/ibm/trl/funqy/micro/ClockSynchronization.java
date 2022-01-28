package com.ibm.trl.funqy.micro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.net.SocketException;

import io.quarkus.funqy.Funq;

import com.ibm.trl.funqy.ibmcos.IBMCOS;

public class ClockSynchronization {
    @Funq("Hello")
    public String helloOnly() {
        return "Hello!";
    }

    @Funq
    public String hello(String msg) {
        return "Hello " + msg;
    }

    @Funq
    public HashMap<String, Map<String,String>> clock_synchronization(MySocket s) {
        HashMap<String, Map<String,String>> retVal = new HashMap<String, Map<String,String>>();
        String key = "filename_tmp";

        String request_id = s.getRequest_id();
        String address = s.getServer_address();
        Integer port = Integer.valueOf(s.getServer_port());
        Integer repetitions = Integer.valueOf(s.getRepetitions());
        String output_bucket = s.getOutput_bucket();

        List<Long[]> times = List.of();
        System.out.printf("Starting communication with %s:%s", address, String.valueOf(port));
        int i = 0;

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(4);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress("", 0));

            byte[] message = new byte[0];
            try {
                message = request_id.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            DatagramPacket packet = new DatagramPacket(
                message,
                message.length,
                new InetSocketAddress(address, port)
            );

            int consecutive_failures = 0;
            int measurements_not_smaller = 0;
            long cur_min = 0;

            long send_begin = 0;
            long recv_end = 0;
            while (i < 1000) {
                try {
                    send_begin = System.nanoTime();
                    socket.send(packet);
                    socket.receive(packet);
                    recv_end = System.nanoTime();
                } catch (SocketTimeoutException e) {
                    i += 1;
                    consecutive_failures += 1;
                    if (consecutive_failures == 7) {
                        System.out.println("Can't setup the connection");
                        break;
                    }
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (i > 0) {
                    times.add( new Long[] {Long.valueOf(i), send_begin, recv_end} );
                }
                long cur_time = recv_end - send_begin;
                System.out.printf("Time {} Min Time {} NotSmaller {}", cur_time, cur_min, measurements_not_smaller);
                if (cur_time > cur_min && cur_min > 0) {
                    measurements_not_smaller += 1;
                    if (measurements_not_smaller == repetitions) {
                        try {
                            message = "stop".getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        DatagramPacket packet_ = new DatagramPacket(
                            message,
                            message.length,
                            new InetSocketAddress(address, port)
                        );
                        try {
                            socket.send(packet_);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } else {
                    cur_min = cur_time;
                    measurements_not_smaller = 0;
                }
                i += 1;
                consecutive_failures = 0;
                socket.setSoTimeout(4000);
            }
            socket.close();
            
            if (consecutive_failures != 5) {
                try {
                    FileWriter writer = new FileWriter("data.csv");
                    String header = String.join(",", "id", "client_send", "client_rcv");
                    writer.append(header);
                    for (Long[] row : times) {
                        String[] strRow = Stream.of(row).map(r -> r.toString()).toArray(String[]::new);
                        writer.append(String.join(",", strRow));
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // key = String.format("000/out/030'-results-%s.csv", request_id);
                
                // try {
                //     final IBMCOS cos = new IBMCOS();
                //     cos.uploadFile("trl-knative-benchmark-bucket", key, "data.csv");
                // } catch (java.lang.Exception e) {
                //     e.printStackTrace();
                // }

            } else {
                key = "connection failure";
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        key = String.format("000/out/030'-results-%s.csv", request_id);
                
        try {
            final IBMCOS cos = new IBMCOS();
            cos.uploadFile("trl-knative-benchmark-bucket", key, "data.csv");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

        retVal.put(
            "result",
            Map.of(
                "bucket-key", key,
                "timestamp", s.getIncome_timestamp()
            )
        );
        return retVal;
    }
}
