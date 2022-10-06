package com.ibm.trl.knativebench;

import java.sql.Timestamp;
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
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import com.hubspot.jinjava.Jinjava;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;


import io.quarkus.funqy.Funq;


/* reference: https://github.com/HubSpot/jinjava */

public class DynamicHtml {
    private Logger log;

    private String template="<!DOCTYPE html>"+
"<html>"+
"  <head>"+
"    <title>Randomly generated data.</title>"+
"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"+
"    <link href=\"http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css\" rel=\"stylesheet\" media=\"screen\">"+
"    <style type=\"text/css\">"+
"      .container {"+
"        max-width: 500px;"+
"        padding-top: 100px;"+
"      }"+
"    </style>"+
"  </head>"+
"  <body>"+
"    <div class=\"container\">"+
"      <p>Welcome {{username}}!</p>"+
"      <p>Data generated at: {{cur_time}}!</p>"+
"      <p>Requested random numbers:</p>"+
"      <ul>"+
"        {% for n in random_numbers %}"+
"        <li>{{n}}</li>"+
"        {% endfor %}"+
"      </ul>"+
"    </div>"+
"  </body>"+
"</html>";

   static Map<String, Integer> size_generators = Map.of("test",   10,
                                                         "tiny",   100,
                                                         "small",  1000,
                                                         "medium", 10000,
                                                         "large",  100000,
                                                         "huge",   1000000,
                                                         "massive",10000000);

    private int inputSize(String size) {
        int retval = 1;

        if (size != null) {
            Integer s = size_generators.get(size);
            if (s != null) {
                retval = s.intValue();
            } else if (size.length() > 0) {
                retval = Integer.parseUnsignedInt(size);
            }
        }

        return retval;
    }



    public DynamicHtml() throws Exception{
        log = Logger.getLogger(DynamicHtml.class);
    }

    @Funq("dynamic-html")
    public RetValType dynamicHtml(FunInput input) throws Exception {
        var retVal = new RetValType();
	Random rand = new Random();

	String key = "1";
        boolean debug = false;
        if (input != null) {
            if (input.size != null)
                key = input.size;
            if (input.debug != null)
		debug = Boolean.parseBoolean(input.debug);
        }

	int loadSize = inputSize(key);

        if (debug) {
		System.out.println("Starting DynamicHtml: "+key);
        }

        long startTime = System.nanoTime();

        long initStart = System.nanoTime();
	Jinjava jinjava = new Jinjava();
        long initEnd = System.nanoTime();
	HashMap<String, Object> context = new HashMap<String, Object>();

        long setupStart = System.nanoTime();
	List<Integer> integers = new ArrayList<>(loadSize);
	for (int i = 0; i < loadSize; i++) {
		integers.add(Integer.valueOf(rand.nextInt(1000000)));
	}
        long setupEnd = System.nanoTime();

	context.put("username", "testname");
	context.put("random_numbers", integers);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	context.put("cur_time", timestamp.toString());

        long renderStart = System.nanoTime();
	String renderedTemplate = jinjava.render(template, context);
        long renderEnd = System.nanoTime();
        long stopTime = renderEnd;

        double initTime   = (initEnd   - initStart)/1000000000.0;
        double setupTime  = (setupEnd  - setupStart)/1000000000.0;
        double renderTime = (renderEnd - renderStart)/1000000000.0;
        double runTime    = (stopTime  - startTime)/1000000000.0;

        retVal.result.put("input_size",      key);
        retVal.result.put("converted_size",  Integer.toString(loadSize));
        retVal.result.put("rendered_Length", Long.toString(renderedTemplate.length()));
        retVal.measurement.put("total_run_time", runTime);
        retVal.measurement.put("init_time",      initTime);
        retVal.measurement.put("setup_time",     setupTime);
        retVal.measurement.put("render_time",    renderTime);
        retVal.measurement.put("input_size",     (double)loadSize);
        retVal.measurement.put("render_size",    (double)(renderedTemplate.length()));

        if (debug) {
            retVal.result.put("rendered_HTML", renderedTemplate);
        }

        log.info("retVal.measurement="+retVal.measurement.toString());
        return (retVal);
    }

    public static class FunInput {
        public String size;
        public String debug;
    }

    public static class RetValType {
        public Map<String, String> result;
        public Map<String, Double> measurement;

        RetValType() {
            result      = new HashMap<String, String>();
            measurement = new HashMap<String, Double>();
        }
    }

}
