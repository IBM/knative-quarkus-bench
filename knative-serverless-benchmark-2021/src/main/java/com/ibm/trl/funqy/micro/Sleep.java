package com.ibm.trl.funqy.micro;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.quarkus.funqy.Funq;

public class Sleep {
    static Map<String, Integer> size_generators = Map.of("test",  1,
                                                         "small", 100,
                                                         "large", 1000);

    @Funq("Hello")
    public String helloOnly() {
        return "Hello!";
    }

    @Funq
    public String hello(String msg) {
        return "Hello " + msg;
    }

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

        return retVal;
    }
}
