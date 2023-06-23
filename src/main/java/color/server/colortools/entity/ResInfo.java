package color.server.colortools.entity;

public class ResInfo {
    public static final int STATUS_NO_LOGIN = 1001;
    public static final int STATUS_INNER_ERR = 1002;
    public static final int STATUS_OPT_ERR = 1003;
    public static final int STATUS_SUCCESS = 1004;


    public boolean err;
    public int status;
    public Object msg;

    public ResInfo(){}

    public ResInfo(boolean err, int status, Object msg) {
        this.err = err;
        this.status = status;
        this.msg = msg;
    }

    public boolean isErr() {
        return err;
    }

    public void setErr(boolean err) {
        this.err = err;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResInfo{" +
                "err=" + err +
                ", status=" + status +
                ", msg=" + msg +
                '}';
    }
}
