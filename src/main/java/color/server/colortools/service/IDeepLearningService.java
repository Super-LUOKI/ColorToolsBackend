package color.server.colortools.service;


import color.server.colortools.entity.TrainingInfo;

// 兼容旧版本的类
public interface IDeepLearningService {

    /**
     * 开始模型训练，并返回训练id
     * @param feats 特征值
     * @param targets 目标值
     * @param trainRatio 训练集比例
     * @param allowError 容许误差
     * @return 训练id
     */
    String train(Double[][] feats, Double[] targets, double trainRatio, double allowError);

    /**
     * 获取训练结果
     * @param trainingId 训练id
     * @return 训练结果
     */
    TrainingInfo getTrainResult(String trainingId);
}
