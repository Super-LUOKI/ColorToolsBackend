package color.server.colortools.controller;

import color.server.colortools.entity.ResInfo;
import color.server.colortools.entity.TrainingInfo;
import color.server.colortools.service.IDeepLearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dl")
public class DLController extends BaseController {

    @Autowired
    private IDeepLearningService deepLearningService;
    /**
     * 开始模型训练，并返回训练id
     * @return
     */
    @PostMapping("/train")
    public ResInfo train(@RequestBody Map<String, Object> body) {
        System.out.println("请求训练");

        List<List<Double>> feats = (List<List<Double>>) body.get("feats");
        List<Double> targets = (List<Double>) body.get("targets");
        Double trainRatio = (Double) body.get("trainRatio");
        Double allowError = (Double) body.get("allowError");

        System.out.println("feats: " + feats);
        System.out.println("targets: " + targets);
        System.out.println("trainRatio: " + trainRatio);
        System.out.println("allowError: " + allowError);

        Double[][] featsArr = new Double[feats.size()][feats.get(0).size()];
        Double[] targetsArr = new Double[targets.size()];
        // 参数转成需要的数组形式
        for (int i = 0; i < feats.size(); i++) {
            for (int j = 0; j < feats.get(i).size(); j++) {
                featsArr[i][j] = feats.get(i).get(j);
            }
        }
        for (int i = 0; i < targets.size(); i++) {
            targetsArr[i] = targets.get(i);
        }
        String trainingId = deepLearningService.train(featsArr, targetsArr, trainRatio, allowError);
        return resSuccess(trainingId);
    }

    /**
     * 获取模型训练信息
     * @return
     */
    @PostMapping("/check")
    public ResInfo check(@RequestBody Map<String, Object> body){
        String trainingId = (String) body.get("trainingId");
        TrainingInfo result = deepLearningService.getTrainResult(trainingId);
        return resSuccess(result);
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
