package color.server.colortools.entity;

import java.util.UUID;

public class TrainingInfo {
    private String id;
    private double rW;
    private double gW;
    private double bW;
    private double bias;
    private double accuracy;

    private boolean done;

    private int epochs;

    private int progress;

    public TrainingInfo() {
        // 生成唯一id
        this.id = "train_" + UUID.randomUUID().toString().replaceAll("-", "");
        this.done = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getrW() {
        return rW;
    }

    public void setrW(double rW) {
        this.rW = rW;
    }

    public double getgW() {
        return gW;
    }

    public void setgW(double gW) {
        this.gW = gW;
    }

    public double getbW() {
        return bW;
    }

    public void setbW(double bW) {
        this.bW = bW;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "TrainingInfo{" +
                "id='" + id + '\'' +
                ", rW=" + rW +
                ", gW=" + gW +
                ", bW=" + bW +
                ", bias=" + bias +
                ", accuracy=" + accuracy +
                ", done=" + done +
                ", epochs=" + epochs +
                ", progress=" + progress +
                '}';
    }


}
