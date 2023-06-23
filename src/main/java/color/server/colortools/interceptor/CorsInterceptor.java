package color.server.colortools.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 跨域相关拦截器
 */
public class CorsInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler) throws Exception {
        // 允许跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");
        // 设置能够允许客户端暴露的响应头信息
        response.setHeader("Access-Control-Expose-Headers", "custom-token");
        response.addHeader("Access-Control-Allow-Credentials","true"); // 允许携带验证信息(Cookie)
        //浏览器会先发送一个试探请求OPTIONS,然后才会发送真正的请求，为了避免拦截器拦截两次请求，所以不能让OPTIONS请求通过
        if ("OPTIONS".equals(request.getRequestURI())){
            return false;
        }
        return true;
    }

}
