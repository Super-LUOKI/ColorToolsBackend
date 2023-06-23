package color.server.colortools.bean;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

public class DataSetBox {
    public INDArray trainFeats;
    public INDArray trainTargets;
    public INDArray testFeats;
    public INDArray testTargets;

    public double maxTrainFeat;
    public double maxTrainTarget;
    public double maxTestFeat;
    public double maxTestTarget;

}
