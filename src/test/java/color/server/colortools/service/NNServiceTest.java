package color.server.colortools.service;


import color.server.colortools.entity.TrainingMessage;
import color.server.colortools.service.impl.NNServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NNServiceTest {
    @Autowired
    private NNServiceImpl nnService;
    @Autowired
    private IRedisService redisService;


    @Test
    public void test() throws InterruptedException {
        // 生成测试数据
        Double[][] feats = new Double[100][3];
        Double[] targets = new Double[100];
        for(int i = 0; i < 100; i++){
            Double[] f = new Double[3];
            for(int j = 0; j < 3; j++){
                f[j] = (double) i;
            }
            feats[i] = f;
            targets[i] = (double) i;

        }
        String id = nnService.train_3_1(feats, targets, 0.5, 10,  1000, "test_model");
        Thread.sleep(1000);
        for(int i = 0; i < 100; i++){
            TrainingMessage msg = nnService.getTrainResult(id);
            System.out.println("+++>>> " + msg.toString());
            if(msg == null || msg.isDone()) break;
            Thread.sleep(500);
        }
    }
}
