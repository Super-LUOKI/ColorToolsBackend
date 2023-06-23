package color.server.colortools.entity;

import java.util.UUID;

// 训练信息，用于取代TrainingInfo
public class TrainingMessage {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    private String id;
    private double accuracy;

    private boolean done;

    private int epochs;

    private int progress;

    public TrainingMessage() {
        // 生成唯一id
        this.id = "train_" + UUID.randomUUID().toString().replaceAll("-", "");
        this.done = false;
    }

    @Override
    public String toString() {
        return "TrainingMessage{" +
                "id='" + id + '\'' +
                ", accuracy=" + accuracy +
                ", done=" + done +
                ", epochs=" + epochs +
                ", progress=" + progress +
                '}';
    }
}
