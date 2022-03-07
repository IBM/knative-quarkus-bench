package com.ibm.trl.funqy.inference;

import com.ibm.trl.funqy.cosutils.COSUtils;

import java.io.IOException;
import java.nio.file.Paths;
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

import io.quarkus.funqy.Funq;
import javax.inject.Inject;

import software.amazon.awssdk.services.s3.S3Client;


public class ImageRecognition {
    private COSUtils client;

    @Inject
    S3Client s3;

    public ImageRecognition() throws Exception {
        client = new COSUtils();
    }

    @Funq
    public RetVal imagerecognition(Param param) throws IOException {
        String model_bucket = client.getModelBucket();
        String input_bucket = client.getInBucket();
        String key = param.getInput();
        String model_key = param.getModel();
        String download_path = String.format("/tmp/%s-%s", key, UUID.randomUUID());

        long image_download_begin = System.nanoTime();
        String image_path = download_path;
	try {
            client.downloadFile(s3, input_bucket, key, download_path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long image_download_end = System.nanoTime();

        long model_download_begin = System.nanoTime();
        String model_path = String.join("/", "/tmp", model_key);
	try {
            client.downloadFile(s3, model_bucket, model_key, model_path);
        } catch (Exception e) {
            e.printStackTrace();
	}
        long model_download_end = System.nanoTime();

        long model_process_begin = System.nanoTime();
        Builder<Image, Classifications> builder = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optModelPath(Paths.get(model_path));
        long model_process_end = System.nanoTime();

        long process_begin = System.nanoTime();

        Image img = ImageFactory.getInstance().fromFile(Paths.get(image_path));
        img.getWrappedImage();

        String index = "";
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
                .optSynsetUrl("file:./src/main/resources/synset.txt")
                .build();

            Criteria<Image, Classifications> criteria = builder.optTranslator(translator).build();
            ZooModel<Image, Classifications> model = criteria.loadModel();
            Predictor<Image, Classifications> predictor = model.newPredictor();
            String tokens = predictor.predict(img).best().getClassName();
            ret = tokens.substring(tokens.indexOf(' ') + 1);
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

        long download_time = (image_download_end - image_download_begin)/1000;
        long model_download_time = (model_download_end - model_download_begin) / 1000;
        long model_process_time = (model_process_end - model_process_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        RetVal retVal = new RetVal();
        retVal.result = Map.of(     "idx", index,
                                    "class", ret);
        retVal.measurement = Map.of("download_time", download_time + model_download_time,
                                    "compute_time", process_time + model_process_time,
                                    "model_time", model_process_time,
                                    "model_download_time", model_download_time);

        return retVal; 
    }

    public static class RetVal {
        Map<String, String> result;
        Map<String, Long> measurement;

        public Map<String, String> getResult() {
            return result;
        }

        public void setResult(Map<String, String> result) {
            this.result = result;
        }

        public Map<String, Long> getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Map<String, Long> measurement) {
            this.measurement = measurement;
        }

        RetVal() {
            measurement = new HashMap<String, Long>();
        }
    }
}