package jp.bitspace.salon.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 顧客向けJWT認証フィルタ.
 * <p>
 * Authorization: Bearer <jwt> を検証し、ROLE_CUSTOMER を付与して SecurityContext にセットします。
 */
@Component
public class JwtCustomerAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtCustomerAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // /api/customer/** のみ対象。認証エンドポイントは除外。
        if (path == null) {
            return true;
        }
        if (!path.startsWith("/api/customer/")) {
            return true;
        }
        if (path.startsWith("/api/customer/auth/")) {
            // /me はトークン確認用のためフィルタを通す
            return !"/api/customer/auth/me".equals(path);
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());

        if (!jwtUtils.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long customerId = jwtUtils.getCustomerIdFromToken(token);
        Long salonId = jwtUtils.extractSalonId(token);
        if (customerId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomerPrincipal principal = new CustomerPrincipal(customerId, salonId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
