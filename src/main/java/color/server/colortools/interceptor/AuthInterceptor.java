package color.server.colortools.interceptor;

import color.server.colortools.entity.ResInfo;
import color.server.colortools.exception.BusinessException;
import color.server.colortools.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 登录鉴权过滤器
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private IRedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // response.setCharacterEncoding("UTF-8");
        // response.setContentType("text/html;charset=utf-8");
        // String uri = request.getRequestURI();
        // List<String> allowList = new ArrayList<>();
        // allowList.add("/login");
        // allowList.add("/register");
        // allowList.add("/dl/?(.?)+");
        // // System.out.println("uri = " + uri);
        // if (allowList.stream().anyMatch(uri::matches)) {
        //     return true;
        // }
        //
        // String token = request.getHeader("token");
        // if (token == null || token.isEmpty()) {
        //     System.out.println("no login uri = " + uri);
        //     throw new BusinessException(ResInfo.STATUS_NO_LOGIN, "未登录");
        // }
        // Object loginStatus = redisService.get(token);
        // if (Objects.isNull(loginStatus)) {
        //     throw new BusinessException(ResInfo.STATUS_NO_LOGIN, "错误token");
        // }
        return true;

    }

}
