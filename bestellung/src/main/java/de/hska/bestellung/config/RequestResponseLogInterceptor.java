/*
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.bestellung.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Interceptor um eingehende Requests und ausgehende Responses zu protokollieren
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Log4j2
class RequestResponseLogInterceptor extends HandlerInterceptorAdapter {
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
                   throws Exception {
        if (log.isDebugEnabled()) {
            // Der Request-Body ist ein Stream und kann nur einmal gelesen werden
            // http://stackoverflow.com/questions/10457963/...
            //   ...spring-rest-service-retrieving-json-from-request#answer-10458119
            log.debug("<Request> URI: {}", request::getRequestURL);
            log.debug("<Request> Method: {}", request::getMethod);
            log.debug("<Request> Accept: {}",
                      () -> request.getHeader("Accept"));
            log.debug("<Request> Content Type: {}", request::getContentType);
            log.debug("<Request> Authorization: {}",
                      () -> request.getHeader("Authorization"));
        }
        return true;
    }
    
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView)
                throws Exception {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("<Response> Status: {}", response::getStatus);
        log.debug("<Response> Location: {}",
                  () -> response.getHeader("Location"));
    }
} 
