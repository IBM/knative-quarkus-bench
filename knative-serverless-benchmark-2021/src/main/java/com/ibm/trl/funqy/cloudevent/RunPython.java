package com.ibm.trl.funqy.cloudevent;

// import org.jboss.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class RunPython {
//    private static final Logger log = Logger.getLogger(RunPython.class);

    public RunPython() {
    }

    public List<String> runScript(String scriptname, String arg1) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/python3", scriptname, arg1);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        List<String> results = readProcessOutput(process.getInputStream());

        process.waitFor();
        
        return (results);
    }

    public List<String> runScript(String scriptname) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/python3", scriptname);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        List<String> results = readProcessOutput(process.getInputStream());

        process.waitFor();
        
        return (results);
    }

    private List<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines().collect(Collectors.toList());
        }
    }
}
