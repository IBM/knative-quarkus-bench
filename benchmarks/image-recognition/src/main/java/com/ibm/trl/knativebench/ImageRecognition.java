package com.ibm.trl.knativebench;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ai.djl.Model;
import ai.djl.basicmodelzoo.basic.Mlp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.Criteria.Builder;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.funqy.Funq;
import javax.inject.Inject;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


public class ImageRecognition {
    private static final double nanosecInSec = 1_000_000_000.0;

    private static Builder<Image, Classifications> model;

    private static final Object lock = new Object();

    @Inject
    S3Client s3;
    @Inject
    Logger log;

    @ConfigProperty(name = "knativebench.image-recognition.input_bucket")
    String input_bucket;


    @Funq("image-recognition")
    public RetValType image_recognition(FunInput input) throws IOException {
        String key = input.getInput();
        String key_path = String.format("/tmp/%s-%s", key, UUID.randomUUID());

        String model_key = input.getModel();
        String model_key_path = String.join("/", "/tmp", model_key);

        String synset = input.getSynset();
        String synset_path = String.join("/", "/tmp", synset);

        if (input.getInput_bucket() != null) {
            input_bucket = input.getInput_bucket();
        }

        String image_path = key_path;
        long image_download_begin = System.nanoTime();
        try {
            downloadFile(input_bucket, key, key_path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long image_download_end = System.nanoTime();

        long synset_download_begin = 0l;
        long synset_download_end = 0l;
        long model_download_begin = 0l;
        long model_download_end = 0l;
        long model_process_begin = 0l;
        long model_process_end = 0l;
        if (model != null) {
            synset_download_begin = System.nanoTime();
            synset_download_end = synset_download_begin;
            model_download_begin = System.nanoTime();
            model_download_end = model_download_begin;
            model_process_begin = System.nanoTime();
            model_process_end = model_process_begin;
        } else {
            synchronized(lock) {
                if (model != null) {
                    synset_download_begin = System.nanoTime();
                    synset_download_end = synset_download_begin;
                    model_download_begin = System.nanoTime();
                    model_download_end = model_download_begin;
                    model_process_begin = System.nanoTime();
                    model_process_end = model_process_begin;
                } else {
                    try {
                        synset_download_begin = System.nanoTime();
                        downloadFile(input_bucket, synset, synset_path);
                        synset_download_end = System.nanoTime();

                        model_download_begin = System.nanoTime();
                        downloadFile(input_bucket, model_key, model_key_path);
                        model_download_end = System.nanoTime();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    model_process_begin = System.nanoTime();
                    model = Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                       .optModelPath(Paths.get(model_key_path));
                    model_process_end = System.nanoTime();
               }
            }
        }

        long process_begin = System.nanoTime();
        Image img = ImageFactory.getInstance().fromFile(Paths.get(image_path));
        img.getWrappedImage();

        String ret = "";
        try {
            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                .addTransform(new Resize(256))
                .addTransform(new CenterCrop(224, 224))
                .addTransform(new ToTensor())
                .addTransform(new Normalize(
                        new float[] {0.485f, 0.456f, 0.406f}, /*mean*/
                        new float[] {0.229f, 0.224f, 0.225f}) /*std*/)
                .optApplySoftmax(true)
                .optSynsetUrl("file:" + synset_path)
                .build();

            Criteria<Image, Classifications> criteria = model.optTranslator(translator).build();
            ZooModel<Image, Classifications> zooModel = criteria.loadModel();
            Predictor<Image, Classifications> predictor = zooModel.newPredictor();
            String tokens = predictor.predict(img).best().getClassName();
            ret = tokens.substring(tokens.indexOf(' ') + 1);
            zooModel.getNDManager().close();
        } catch (ModelNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedModelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TranslateException e) {
            e.printStackTrace();
        }
        long process_end = System.nanoTime();

        double image_download_time = (image_download_end - image_download_begin)/nanosecInSec;
        double model_download_time = (model_download_end - model_download_begin) / nanosecInSec;
        double synset_download_time = (synset_download_end - synset_download_begin) / nanosecInSec;
        double model_process_time = (model_process_end - model_process_begin)/nanosecInSec;
        double process_time = (process_end - process_begin)/nanosecInSec;

        RetValType retVal = new RetValType();
        retVal.result = Map.of(     "class", ret);
        retVal.measurement = Map.of("download_time", image_download_time + model_download_time + synset_download_time,
                                    "compute_time", process_time + model_process_time,
                                    "model_time", model_process_time,
                                    "model_download_time", model_download_time);
        
        log.info("retVal.measurement="+retVal.measurement.toString());

        Files.delete(Paths.get(URI.create("file:///" + key_path)));

        return retVal; 
    }

    public void downloadFile(String bucket, String key, String filePath) throws Exception {
        File theFile = new File(filePath);
        File theDir = theFile.getParentFile();
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(request);
        byte[] data = objectBytes.asByteArray();
        OutputStream os = new FileOutputStream(theFile);
        os.write(data);
        os.close();
    }

    public static class FunInput {
        private String input;
        private String model;
        private String synset;
        private String input_bucket;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSynset() {
            return synset;
        }

        public void setSynset(String synset) {
            this.synset = synset;
        }

        public String getInput_bucket() {
            return input_bucket;
        }

        public void setInput_bucket(String input_bucket) {
            this.input_bucket = input_bucket;
        }
    }

    public static class RetValType {
        Map<String, String> result;
        Map<String, Double> measurement;

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public Map<String, Double> getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Map<String, Double> measurement) {
            this.measurement = measurement;
        }

        RetValType() {
            measurement = new HashMap<String, Double>();
        }
    }
}
