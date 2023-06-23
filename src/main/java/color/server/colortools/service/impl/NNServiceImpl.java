package color.server.colortools.service.impl;

import color.server.colortools.bean.Constraint;
import color.server.colortools.bean.DataSetBox;
import color.server.colortools.entity.ResInfo;
import color.server.colortools.entity.TrainingInfo;
import color.server.colortools.entity.TrainingMessage;
import color.server.colortools.exception.BusinessException;
import color.server.colortools.service.INNService;
import color.server.colortools.service.IRedisService;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class NNServiceImpl implements INNService {

    @Autowired
    private IRedisService redisService;

    @Override
    public String train_3_1(Double[][] feats, Double[] targets, double trainRatio, double allowError, int epoches, String model_name) {
        // 构建神经网络结构
        // MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
        //         .seed(123)
        //         .updater(new Adam(0.01))
        //         .list()
        //         .layer(new DenseLayer.Builder().nIn(3).nOut(128).activation(Activation.LEAKYRELU).build())
        //         .layer(new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.LEAKYRELU).build())
        //         .layer(new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.LEAKYRELU).build())
        //         .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        //                 .nIn(128).nOut(1).activation(Activation.IDENTITY).build())
        //         .build();
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .list()
                .layer(new DenseLayer.Builder().nIn(3).nOut(128).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nIn(128).nOut(128).activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(128).nOut(1).activation(Activation.IDENTITY).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        TrainingMessage trainingMessage = new TrainingMessage();
        TrainThread trainThread = new TrainThread(model, model_name);
        trainThread.setTrainRatio(trainRatio);
        trainThread.setEpoches(epoches);
        trainThread.setTrainingMessage(trainingMessage);
        trainThread.setRedisService(redisService);
        trainThread.setAllowError(allowError);
        trainThread.setInOut(3, 1);
        trainThread.setFeats(feats);
        trainThread.setTargets(targets);
        trainThread.start();
        return trainingMessage.getId();
    }

    @Override
    public String predict_3_1(Double[][] feats, int model_id) {
        return null;
    }

    @Override
    public String train_6_1(Double[][] feats, Double[] targets, double trainRatio, double allowError, int epoches, int model_name) {
        return null;
    }

    @Override
    public String predict_6_1(Double[][] feats, int model_id) {
        return null;
    }

    @Override
    public TrainingMessage getTrainResult(String trainingId) {
        TrainingMessage trainingInfo = (TrainingMessage) redisService.get(trainingId);
        if (trainingInfo == null) {
            throw new BusinessException(ResInfo.STATUS_OPT_ERR, "模型未训练");
        }
        if (trainingInfo.isDone()) {
            System.out.println(trainingInfo.toString());
            redisService.del(trainingId);
        }
        return trainingInfo;
    }

    // 模型训练线程子类

    /**
     * 负责：
     * 1、划分数据集
     * 2、训练模型
     * 3、保存模型（模型到文件、信息到数据库）
     * 4、返回训练进度信息
     */
    private static class TrainThread extends Thread {
        private String modelName = "";
        private MultiLayerNetwork model;
        private double trainRatio;

        private Double[][] feats;
        private Double[] targets;
        private int numOutput;

        private TrainingMessage trainingMessage;
        private IRedisService redisService;

        private double allowError;
        private int numInput;

        private int epoches;


        public void setFeats(Double[][] feats) {
            this.feats = feats;
        }

        public void setTargets(Double[] targets) {
            this.targets = targets;
        }


        public void setEpoches(int epoches) {
            this.epoches = epoches;
        }


        public void setTrainRatio(double trainRatio) {
            this.trainRatio = trainRatio;
        }

        public void setTrainingMessage(TrainingMessage trainingMessage) {
            this.trainingMessage = trainingMessage;
        }

        public void setRedisService(IRedisService redisService) {
            // if(redisService == null){
            //     System.out.println("redisService为空");
            // }
            this.redisService = redisService;
        }

        public void setAllowError(double allowError) {
            this.allowError = allowError;
        }



        public TrainThread(MultiLayerNetwork  model, String modelName) {
            this.model = model;
            this.modelName = modelName;
        }



        /**
         * 输入层输入数量和输出层输出数量
         * @param numInput
         */
        public void setInOut(int numInput, int numOutput){
            this.numInput = numInput;
            this.numOutput = numOutput;
        }

        /**
         * 分割训练集和测试集
         *
         * @return
         */
        private DataSetBox splitTrainTest() {
            int trainSize = (int) (feats.length * trainRatio);
            int testSize = feats.length - trainSize;

            // 展平后的值
            double[] trainFeats = new double[trainSize * numInput];
            int lenTrainFeats = 0;
            double maxTrainFeat = 0;
            double[] trainTargets = new double[trainSize];
            double maxTrainTarget = 0;

            // 展平后的值
            double[] testFeats = new double[testSize * numInput];
            int lenTestFeats = 0;
            double maxTestFeat = 0;
            double[] testTargets = new double[testSize];
            double maxTestTarget = 0;

            // 分配训练集和测试集
            for (int i = 0; i < feats.length; i++) {
                if (i < trainSize) {
                    // 给训练集增加数据
                    for (int j = 0; j < feats[i].length; j++) {
                        trainFeats[lenTrainFeats++] = feats[i][j];
                        maxTrainFeat = Math.max(maxTrainFeat, Math.abs(feats[i][j]));
                    }
                    trainTargets[i] = targets[i];
                    maxTrainTarget = Math.max(maxTrainTarget, Math.abs(targets[i]));

                } else {
                    // 给测试集增加数据
                    for (int j = 0; j < feats[i].length; j++) {
                        testFeats[lenTestFeats++] = feats[i][j];
                        maxTestFeat = Math.max(maxTestFeat, Math.abs(feats[i][j]));
                    }
                    testTargets[i - trainSize] = targets[i];
                    maxTestTarget = Math.max(maxTestTarget, Math.abs(targets[i]));
                }
            }

            DataSetBox dataSetBox = new DataSetBox();

            dataSetBox.trainFeats = Nd4j.create(trainFeats, new int[]{trainSize, numInput});
            dataSetBox.trainTargets = Nd4j.create(trainTargets, new int[]{trainSize, numOutput});
            dataSetBox.testFeats = Nd4j.create(testFeats, new int[]{testSize, numInput});
            dataSetBox.testTargets = Nd4j.create(testTargets, new int[]{testSize, numOutput});

            dataSetBox.maxTrainFeat = maxTrainFeat;
            dataSetBox.maxTrainTarget = maxTrainTarget;
            dataSetBox.maxTestFeat = maxTestFeat;
            dataSetBox.maxTestTarget = maxTestTarget;

            return dataSetBox;
        }

        @Override
        public void run() {
            // 获取数据
            DataSetBox dataSetBox = splitTrainTest();
            System.out.println("分割数据集");
            System.out.printf(dataSetBox.trainFeats.toString());
            System.out.printf(dataSetBox.trainTargets.toString());
            // 构造训练数据集
            DataSet dataSet = new DataSet(dataSetBox.trainFeats, dataSetBox.trainTargets);
            List<DataSet> theDataSet = dataSet.asList();
            DataSetIterator iterator = new ListDataSetIterator<>(theDataSet, Math.min(theDataSet.size(), 10));

            // 设置打印日志
            model.setListeners(new ScoreIterationListener(1000));


            // 训练
            for (int i = 0; i < epoches; i++) {
                iterator.reset();
                model.fit(iterator);
                // 每隔100此epoch更新一次进度
                if ((i + 1) % 100 == 0) {
                    trainingMessage.setProgress(i + 1);
                    trainingMessage.setEpochs(epoches);
                    redisService.set(trainingMessage.getId(), trainingMessage);
                    System.out.println("trainingMessage = " + trainingMessage);
                }

            }

            // 测试数据，获取准确率
            INDArray testFeats = dataSetBox.testFeats;
            INDArray testTargets = dataSetBox.testTargets;
            INDArray predict = model.output(testFeats);
            double accuracy = 0;
            for (int i = 0; i < predict.rows(); i++) {
                double p = predict.getRow(i).getDouble(0);
                double t = testTargets.getRow(i).getDouble(0);
                if (Math.abs(p - t) < allowError) {
                    accuracy++;
                    System.out.println("accuracy " + accuracy + ", predict.rows() = " + predict.rows());
                }
            }
            System.out.println("accuracy rate: " + accuracy / predict.rows());
            trainingMessage.setAccuracy(accuracy / predict.rows());

            // 更新redis训练信息
            trainingMessage.setDone(true);

            // 保存模型
            try {
                if(!new File(Constraint.MODEL_DIR).exists()){
                    new File(Constraint.MODEL_DIR).mkdirs();
                }
                String modelPath = Constraint.MODEL_DIR + "/" + modelName + ".bin";
                ModelSerializer.writeModel(model,modelPath , true);
                System.out.println("保存模型成功");
            } catch (IOException e) {
                System.out.println("保存模型失败");
                throw new RuntimeException(e);
            }
            redisService.set(trainingMessage.getId(), trainingMessage);

        }
    }

}
