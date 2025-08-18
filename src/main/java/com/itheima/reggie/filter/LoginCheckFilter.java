package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();

        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        // 1) 静态/公开资源直接放行
        if (match(urls, requestURI)) {
            log.info("本次请求 {} 不需要登录校验", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 2) 已登录员工放行
        Object empAttr = request.getSession().getAttribute("employee");
        if (empAttr != null) {
            Long empId = (Long) empAttr;
            BaseContext.setCurrentId(empId);
            log.info("员工已登录, id={}", empId);
            filterChain.doFilter(request, response);
            return;
        }

        // 3) 已登录普通用户放行
        Object userAttr = request.getSession().getAttribute("user");
        if (userAttr != null) {
            Long userId = (Long) userAttr;
            BaseContext.setCurrentId(userId);
            log.info("用户已登录, id={}", userId);
            filterChain.doFilter(request, response);
            return;
        }

        // 4) 未登录：只写一次响应并结束
        log.info("未登录，拦截 {}", requestURI);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        // 关键：写完就结束，不再进入后续链路，避免再次写响应
    }

    private boolean match(String[] urls, String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
