package color.server.colortools.conf;

import color.server.colortools.interceptor.AuthInterceptor;
import color.server.colortools.interceptor.CorsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * 拦截器配置
 */
@Configuration
public class ProjInterceptionConfig extends WebMvcConfigurationSupport {
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 跨域处理拦截器
        registry.addInterceptor(new CorsInterceptor()).addPathPatterns("/**");
        // 登录鉴权拦截器
        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");

    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 设置静态资源目录，允许静态资源访问
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
