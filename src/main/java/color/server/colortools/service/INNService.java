package color.server.colortools.service;

import color.server.colortools.entity.TrainingInfo;
import color.server.colortools.entity.TrainingMessage;

/**
 * 后端只做模型训练、保存、查询模型和模型预测功能
 */
public interface INNService {

    /**
     * 开始模型训练，并返回训练id（3输入1输出）
     * @param feats 特征值
     * @param targets 目标值
     * @param trainRatio 训练集比例
     * @param allowError 容许误差
     * @param epoches 最大训练次数
     * @param model_name 模型名称
     * @return 训练id
     */
    String train_3_1(Double[][] feats, Double[] targets, double trainRatio, double allowError, int epoches, String model_name);


    /**
     *预测结果（3输出1输出）
     * @param feats
     * @param model_id
     * @return
     */
    String predict_3_1(Double[][] feats, int model_id);

    /**
     * 开始模型训练，并返回训练id（6输入1输出）
     * @param feats
     * @param targets
     * @param trainRatio
     * @param allowError
     * @param epoches
     * @param model_name
     * @return
     */
    String train_6_1(Double[][] feats, Double[] targets, double trainRatio, double allowError, int epoches, int model_name);

    /**
     * 预测结果（6输入1输出）
     * @param feats
     * @param model_id
     * @return
     */
    String predict_6_1(Double[][] feats, int model_id);

    /**
     * 获取训练结果信息
     * @param trainingId 训练id
     * @return 训练结果
     */
    TrainingMessage getTrainResult(String trainingId);

}
