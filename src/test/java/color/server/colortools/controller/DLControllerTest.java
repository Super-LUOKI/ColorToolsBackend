package color.server.colortools.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DLControllerTest {

    @Autowired
    private DLController controller;

    @Test
    public void test(){
        System.out.println("hello");
    }
}
