package color.server.colortools.exception;

/**
 * 自定义业务异常
 * 集成RuntimeException以保证事务可以回滚
 */
public class BusinessException extends RuntimeException {
    private int status = -1;

    public BusinessException(int status, String msg) {
        super(msg);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

