package com.ibm.trl.knativebench;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;

public class Sleep {
    static double nanosecInSec = 1_000_000_000.0;
    static Map<String, Integer> size_generators = Map.of("test",  1,
                                                         "small", 100,
                                                         "large", 1000);
    @Inject
    Logger log;
    
    @Funq
    public HashMap<String, Long> helloworld(String size) {
        HashMap<String, Long> retVal = new HashMap<String, Long>();
        long hello_count = 0;

        if (size != null) {
            Integer hc = size_generators.get(size);
            if (hc != null) {
                hello_count = hc.intValue();
            }
        }
        long processTimeBegin = System.nanoTime();

	String hello = "world!";
	for (int i = 0; i<hello_count; i++)
	{
		hello = "Hello " + hello;
	}
        long processTimeEnd = System.nanoTime();
        retVal.put("result", hello_count);
        
        log.info("retVal.measurement="+String.valueOf((processTimeEnd - processTimeBegin)/nanosecInSec));

        return retVal;
    }
}
