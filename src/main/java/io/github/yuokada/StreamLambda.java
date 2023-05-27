package io.github.yuokada;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.logging.Logger;

@Named("test")
public class StreamLambda implements RequestStreamHandler {

    @Inject
    Logger logger;
    @Inject
    ProcessingService service;

    @Inject
    ObjectMapper mapper;

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        try {
            String inputString = reader.lines().collect(Collectors.joining("\n")).trim();
            logger.info(inputString);
            InputObject input = mapper.readValue(inputString, InputObject.class);
            OutputObject result = service.process(input);
            writer.write(mapper.writeValueAsString(result));
        } catch (RuntimeException e) {
            logger.info(e.getMessage());
            writer.write(e.getMessage());
        } finally {
            reader.close();
            writer.close();
        }
    }
}
