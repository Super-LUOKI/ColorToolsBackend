package color.server.colortools.service;

public interface IRedisService {
    void set(String key, Object value);

    Object get(String key);

    boolean del(String key);
}
