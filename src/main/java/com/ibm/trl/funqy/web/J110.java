package com.ibm.trl.funqy.web;

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

import io.quarkus.funqy.Funq;


/* reference: https://github.com/HubSpot/jinjava */

public class J110 {
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


    public J110() throws Exception{
        log = Logger.getLogger(J110.class);
    }

    @Funq
    public RetValType dynamicHtml(FunInput input) throws Exception {
        var retVal = new RetValType();
	Random rand = new Random();

	String key = "1000";
        if (input != null) {
            if (input.size != null)
                key = input.size;
        }


//        System.out.println("Starting J110: "+key);

        long startTime = System.nanoTime();

	Jinjava jinjava = new Jinjava();
	HashMap<String, Object> context = new HashMap<String, Object>();

	List<Integer> integers = new ArrayList<>();
	for (int i = 0; i < 1000; i++) {
		integers.add(new Integer(rand.nextInt(1000000)));
	}

	context.put("username", "testname");
	context.put("random_numbers", integers);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	context.put("cur_time", timestamp.toString());

	String renderedTemplate = jinjava.render(template, context);
        long stopTime = System.nanoTime();

	double runTime = (stopTime - startTime)/1000000000.0;

//	String retval = "j110, "+String.format("%.03f",runTime)+", "+
//                Long.toString(renderedTemplate.length());

        retVal.result.put("input_size",    key);
        retVal.result.put("rendered_Length",    Long.toString(renderedTemplate.length()));
        retVal.measurement.put("run_time",  runTime);

        return (retVal);
    }

    public static class FunInput {
//        public String bucket_name;
//        public String input_key;
//        public String output_key;
	public String size;
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
