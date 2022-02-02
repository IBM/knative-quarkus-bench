package com.ibm.trl.funqyTest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

public class ImageRecognition {
    private COSUtils client = new COSUtils();

    /*
     * SCRIPT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__)))
     * class_idx = json.load(open(os.path.join(SCRIPT_DIR, "imagenet_class_index.json"), 'r'))
     * idx2label = [class_idx[str(k)][1] for k in range(len(class_idx))]
     * model = None
     */
    /* Currently use synset.txt and no need to create idx2label */

    @Funq
    public RetVal imagerecognition(Param param) throws IOException {
        /*
         * model_bucket = event.get('bucket').get('model')
         * input_bucket = event.get('bucket').get('input')
         * key = event.get('object').get('input')
         * model_key = event.get('object').get('model')
         * download_path = '/tmp/{}-{}'.format(key, uuid.uuid4())
         */
        String model_bucket = client.getModelBucket();
        String input_bucket = client.getInBucket();
        String key = param.getInput();
        String model_key = param.getModel();
        String download_path = String.format("/tmp/%s-%s", key, UUID.randomUUID());

        /*
         * image_download_begin = datetime.datetime.now()
         * image_path = download_path
         * client.download(input_bucket, key, download_path)
         * image_download_end = datetime.datetime.now()
         */
        long image_download_begin = System.nanoTime();
        String image_path = download_path;
        client.downloadFile(input_bucket, key, download_path);
        long image_download_end = System.nanoTime();

        /*
         * model_download_begin = datetime.datetime.now()
         * model_path = os.path.join('/tmp', model_key)
         * client.download(model_bucket, model_key, model_path)
         * model_download_end = datetime.datetime.now()
         */
        long model_download_begin = System.nanoTime();
        String model_path = String.join("/", "/tmp", model_key);
        client.downloadFile(model_bucket, model_key, model_path);
        long model_download_end = System.nanoTime();

        /*
         * model_process_begin = datetime.datetime.now()
         * model = resnet50(pretrained=False)
         * model.load_state_dict(torch.load(model_path))
         * model.eval()
         * model_process_end = datetime.datetime.now()
         */
        long model_process_begin = System.nanoTime();
        Builder<Image, Classifications> builder = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optModelPath(Paths.get(model_path));
        long model_process_end = System.nanoTime();

        /*
         * process_begin = datetime.datetime.now()
         * input_image = Image.open(image_path)
         * preprocess = transforms.Compose([
         *     transforms.Resize(256),
         *     transforms.CenterCrop(224),
         *     transforms.ToTensor(),
         *     transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
         * ])
         * input_tensor = preprocess(input_image)
         * input_batch = input_tensor.unsqueeze(0) # create a mini-batch as expected by the model 
         * output = model(input_batch)
         * _, index = torch.max(output, 1)
         * # The output has unnormalized scores. To get probabilities, you can run a softmax on it.
         * prob = torch.nn.functional.softmax(output[0], dim=0)
         * _, indices = torch.sort(output, descending = True)
         * ret = idx2label[index]
         * process_end = datetime.datetime.now()
         */
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

            Criteria<Image, Classifications> criteria = builder
                    .optTranslator(translator)
                    .build();

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

        /*
         * download_time = (image_download_end- image_download_begin) / datetime.timedelta(microseconds=1)
         * model_download_time = (model_download_end - model_download_begin) / datetime.timedelta(microseconds=1)
         * model_process_time = (model_process_end - model_process_begin) / datetime.timedelta(microseconds=1)
         * process_time = (process_end - process_begin) / datetime.timedelta(microseconds=1)
         */
        long download_time = (image_download_end - image_download_begin)/1000;
        long model_download_time = (model_download_end - model_download_begin) / 1000;
        long model_process_time = (model_process_end - model_process_begin)/1000;
        long process_time = (process_end - process_begin)/1000;

        /*
         * return {
         *   'result': {'idx': index.item(), 'class': ret},
         *   'measurement': {
         *       'download_time': download_time + model_download_time,
         *       'compute_time': process_time + model_process_time,
         *       'model_time': model_process_time,
         *       'model_download_time': model_download_time
         *   }
         */
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
