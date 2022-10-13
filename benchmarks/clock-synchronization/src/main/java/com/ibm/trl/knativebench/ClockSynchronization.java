package com.ibm.trl.knativebench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;


public class ClockSynchronization {
    static double nanosecInSec = 1_000_000_000.0;
    
    @Inject
    S3Client s3;
    @Inject
    Logger log;

    @ConfigProperty(name = "knativebench.clock-synchronization.output_bucket")
    String output_bucket;

    @Funq("clock-synchronization")
    public HashMap<String, String> clock_synchronization(FunInput input) {
        HashMap<String, String> retVal = new HashMap<String, String>();
        String key = "filename_tmp";

        String request_id = input.getRequest_id();
        if (request_id == null) {
            request_id = "test";
        }
        String address = input.getServer_address();
        Integer port = Integer.valueOf(input.getServer_port());
        int rep = input.getRepetitions();
        Integer repetitions = Integer.valueOf(rep == 0? 1 : rep);
        if (input.getOutput_bucket() != null) {
            output_bucket = input.getOutput_bucket();
        }
        boolean debug = input.getDebug();

        List<Long[]> times = new ArrayList<Long[]>();
        System.out.printf("Starting communication with %s:%s\n", address, String.valueOf(port));
        int i = 0;
        
        long processTimeBegin = System.nanoTime();

        try {
            DatagramSocket sendSocket = new DatagramSocket(null);
            sendSocket.setSoTimeout(4);
            sendSocket.setReuseAddress(true);
            sendSocket.bind(new InetSocketAddress("", 0));

            DatagramSocket recvSocket = new DatagramSocket(port);

            byte[] message = new byte[0];
            try {
                message = request_id.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            DatagramPacket packet = new DatagramPacket(message, message.length, new InetSocketAddress(address, port));
            DatagramPacket packet2 = new DatagramPacket(message, message.length);

            int consecutive_failures = 0;
            int measurements_not_smaller = 0;
            long cur_min = 0;

            long send_begin = 0;
            long recv_end = 0;
            while (i < 1000) {
                try {
                    send_begin = System.nanoTime();
                    sendSocket.send(packet);
                    recvSocket.receive(packet2);
                    recv_end = System.nanoTime();
                } catch (SocketTimeoutException e) {
                    ++i;
                    ++consecutive_failures;
                    if (consecutive_failures == 5) {
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
                if (cur_time > cur_min && cur_min > 0) {
                    measurements_not_smaller += 1;
                    if (measurements_not_smaller == repetitions) {
                        try {
                            message = "stop".getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        DatagramPacket packet_ = new DatagramPacket(message, message.length, new InetSocketAddress(address, port));
                        try {
                            sendSocket.send(packet_);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } else {
                    cur_min = cur_time;
                    measurements_not_smaller = 0;
                }
                ++i;
                consecutive_failures = 0;
                sendSocket.setSoTimeout(4000);
            }
            sendSocket.close();
            recvSocket.close();

            if (consecutive_failures != 5 && debug) {
                try {
                    FileWriter writer = new FileWriter("/tmp/data.csv");
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

                key = String.format("clock-synchronization-benchmark-results-%s.csv", request_id);

                try {
                    PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(output_bucket).key(key).build();
                    s3.putObject(objectRequest, RequestBody.fromFile(new File("/tmp/data.csv").toPath()));
                } catch (java.lang.Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        long processTimeEnd = System.nanoTime();

        retVal.put( "result", key );
        log.info("retVal.measurement="+String.valueOf((processTimeEnd - processTimeBegin)/nanosecInSec));
        
        return retVal;
    }

    public static class FunInput {
        String request_id;
        String server_address;
        int server_port;
        int repetitions;
        String output_bucket;
        boolean debug;

        public String getRequest_id() {
            return request_id;
        }

        public void setRequest_id(String request_id) {
            this.request_id = request_id;
        }

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

        public String getOutput_bucket() {
            return output_bucket;
        }

        public void setOutput_bucket(String output_bucket) {
            this.output_bucket = output_bucket;
        }

        public boolean getDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }
    }
}
