package com.ibm.trl.funqy.cloudevent;

import org.jboss.logging.Logger;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ibm.trl.funqy.ibmcos.IBMCOS;

public class J0000 extends BenchmarkTest {
    private Logger log;
    private UUID uuid;
    private IBMCOS cos;

    public J0000() throws Exception {
        uuid = UUID.randomUUID();
        cos = new IBMCOS();
        log = Logger.getLogger(J0000.class);
    }

    public String runTest() throws Exception {
        if (!cos.available())
            return ("ERROR: J0000.runTest() unable to run since IBMCOS unavailable.");
        System.out.println("Starting J0000: ");

	String retval = "J0000,1,2,3,4";

        return (retval);
    }

}
