package com.ibm.trl.knativebench;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;

public class Sleep {
    static Map<String, Integer> size_generators = Map.of("test",  1,
                                                         "small", 100,
                                                         "large", 1000);
    @Inject
    Logger log;
    
    @Funq
    public HashMap<String, Long> sleep(String size) {
        HashMap<String, Long> retVal = new HashMap<String, Long>();
        long sleep_time = 0; // seconds

        if(size != null) {
            Integer st = size_generators.get(size);
            if(st != null) {
                sleep_time = st.intValue();
            }
        }

        try {
            TimeUnit.SECONDS.sleep(sleep_time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        retVal.put( "result", sleep_time );
        
        log.info("retVal.measurement="+retVal.toString());

        return retVal;
    }
}
