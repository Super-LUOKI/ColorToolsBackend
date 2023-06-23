package color.server.colortools.service.impl;

import color.server.colortools.bean.DataSetBox;
import color.server.colortools.entity.ResInfo;
import color.server.colortools.entity.TrainingInfo;
import color.server.colortools.exception.BusinessException;
import color.server.colortools.service.IDeepLearningService;
import color.server.colortools.service.IRedisService;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DeepLearningServiceImpl implements IDeepLearningService {
    @Autowired
    private IRedisService redisService;

    @Override
    public String train(Double[][] feats, Double[] targets, double trainRatio, double allowError) {
        TrainingInfo trainingInfo = new TrainingInfo();
        TrainThread trainThread = new TrainThread(feats, targets, trainingInfo, trainRatio, allowError, redisService);
        trainThread.start();
        return trainingInfo.getId();
    }

    @Override
    public TrainingInfo getTrainResult(String trainingId) {
        TrainingInfo trainingInfo = (TrainingInfo) redisService.get(trainingId);
        if (trainingInfo == null) {
            throw new BusinessException(ResInfo.STATUS_OPT_ERR, "模型未训练");
        }
        if (trainingInfo.isDone()) {
            redisService.del(trainingId);
        }
        return trainingInfo;

    }

    private static class TrainThread extends Thread {

        public static int numInput = 3;
        public static int numOutput = 1;
        public static long seed = 12345;
        public static double learningRate = 0.01;
        public static int batchSize = 10;
        public static int epoches = 2000;
        private double trainRatio;

        private Double[][] feats;
        private Double[] targets;
        private TrainingInfo trainingInfo;

        private IRedisService redisService;

        private double allowError;

        private MultiLayerNetwork model = null;


        /**
         * @param feats        特征值
         * @param targets      目标值
         * @param trainingInfo 训练信息
         * @param trainRatio   训练集比例
         * @param service      redis服务
         */
        public TrainThread(Double[][] feats, Double[] targets, TrainingInfo trainingInfo, double trainRatio, double allowError, IRedisService service) {
            initModel();
            this.feats = feats;
            this.targets = targets;
            this.trainingInfo = trainingInfo;
            this.trainRatio = trainRatio;
            this.allowError = allowError;
            this.redisService = service;
            trainingInfo.setEpochs(epoches);
            trainingInfo.setProgress(0);
            redisService.set(trainingInfo.getId(), trainingInfo);
        }

        private void initModel() {
            // 构建一层神经网络
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Sgd(learningRate))
                    .list(
                            new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                                    .activation(Activation.IDENTITY)
                                    .nIn(numInput).nOut(numOutput)
                                    .build()
                    )
                    .build();


            model = new MultiLayerNetwork(conf);
            // 初始化模型参数
            model.init();

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
            // 训练数据归一化处理
            INDArray trainFeats = dataSetBox.trainFeats.div(dataSetBox.maxTrainFeat);
            INDArray trainTargets = dataSetBox.trainTargets.div(dataSetBox.maxTrainTarget);

            // 构造训练数据集
            DataSet dataSet = new DataSet(trainFeats, trainTargets);
            DataSetIterator iterator = new ListDataSetIterator(dataSet.asList(), batchSize);

            // 设置打印日志
            model.setListeners(new ScoreIterationListener(1000));


            // 训练
            for (int i = 0; i < epoches; i++) {
                iterator.reset();
                model.fit(iterator);
                // 每隔100此epoch更新一次进度
                if ((i + 1) % 100 == 0) {
                    trainingInfo.setProgress(i + 1);
                    redisService.set(trainingInfo.getId(), trainingInfo);
                    // System.out.println("trainingInfo = " + trainingInfo);
                }

            }
            // 参数反归一化 设置回模型
            Map<String, INDArray> params = model.paramTable();
            INDArray w = params.get("0_W").div(dataSetBox.maxTrainFeat).muli(dataSetBox.maxTrainTarget);
            INDArray b = params.get("0_b").muli(dataSetBox.maxTrainTarget);
            params.put("0_W", w);
            params.put("0_b", b);
            model.setParamTable(params);

            // 更新redis权重信息
            double[] newW = params.get("0_W").toDoubleVector();
            double[] newB = params.get("0_b").toDoubleVector();
            trainingInfo.setrW(newW[0]);
            trainingInfo.setgW(newW[1]);
            trainingInfo.setbW(newW[2]);
            trainingInfo.setBias(newB[0]);

            // 测试数据，获取准确率
            INDArray testFeats = dataSetBox.testFeats.div(dataSetBox.maxTestFeat);
            INDArray testTargets = dataSetBox.testTargets.div(dataSetBox.maxTestTarget);
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
            trainingInfo.setAccuracy(accuracy / predict.rows());

            // 更新redis训练信息
            trainingInfo.setDone(true);
            redisService.set(trainingInfo.getId(), trainingInfo);
            System.out.println(redisService.get(trainingInfo.getId()));
            // System.out.println("maxFeat = " + maxFeat + ", maxTaget = " + maxTaget);
            System.out.println("训练完成: rw = " + trainingInfo.getrW() + ", gw = " + trainingInfo.getgW() + ", bw = " + trainingInfo.getbW() + ", bias = " + trainingInfo.getBias());
        }

        // public double test(Double[][] feats, Double[] targets, Double allowError) {
        //     double[] preditX = new double[feats.length * 3];
        //     int len = 0;
        //     for (Double[] feat : feats) {
        //         for (Double f : feat) {
        //             preditX[len++] = f;
        //         }
        //     }
        //     INDArray input = Nd4j.create(preditX, new int[]{feats.length, 3});
        //     INDArray output = model.output(input);
        //     double[] preditY = output.toDoubleVector();
        //     int right = 0;
        //     for (int i = 0; i < preditY.length; i++) {
        //         if (Math.abs(preditY[i] - targets[i]) < allowError) {
        //             right++;
        //         }
        //     }
        //     double accuracy = right * 1.0 / preditY.length;
        //     trainingInfo.setAccuracy(accuracy);
        //     trainingInfo.setDone(true);
        //     redisService.set(trainingInfo.getId(), trainingInfo);
        //     return accuracy;
        // }
    }
}
