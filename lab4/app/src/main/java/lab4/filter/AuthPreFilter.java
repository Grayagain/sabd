package lab4.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthPreFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthPreFilter.class);
    private static final String AUTH_HEADER = "X-Auth-Token";

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        return request.getRequestURI().startsWith("/api/");
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String token = request.getHeader(AUTH_HEADER);

        logger.info("gateway received {} {}", request.getMethod(), request.getRequestURI());

        if (token == null || token.trim().isEmpty()) {
            logger.warn("gateway rejected request without {}", AUTH_HEADER);
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(401);
            context.getResponse().setContentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8");
            context.setResponseBody("Missing X-Auth-Token");
            return null;
        }

        logger.info("gateway routed request to backend with {} present", AUTH_HEADER);
        return null;
    }
}
