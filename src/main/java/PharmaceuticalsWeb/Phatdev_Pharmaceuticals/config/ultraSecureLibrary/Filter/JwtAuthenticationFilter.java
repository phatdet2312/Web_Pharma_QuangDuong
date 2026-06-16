//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/JwtAuthenticationFilter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenConstants;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.CookieUtils;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.JwtService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final SecurityLibraryProperties props;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String phuongThuc = request.getMethod();

        boolean laUrlTinh = props.kiemTraLaUrlTinh(requestUri);
        boolean laLenhGet = false;
        if ("GET".equalsIgnoreCase(phuongThuc) == true) {
            laLenhGet = true;
        }
        boolean choPhepBoQuaTaiNguyenTinh = false;
        if (laUrlTinh == true && laLenhGet == true) {
            choPhepBoQuaTaiNguyenTinh = true;
        }

        request.setAttribute("LA_URL_TINH_METHOD_GET", choPhepBoQuaTaiNguyenTinh);

        if (choPhepBoQuaTaiNguyenTinh == true) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean laUrlCongKhai = props.kiemTraLaUrlCongKhai(requestUri);
        request.setAttribute("LA_URL_CONG_KHAI", laUrlCongKhai);

        if (laUrlCongKhai == false) {
            System.out.println("[JwtAuthenticationFilter] Request bao mat: " + requestUri);
        }

        final String jwt = cookieUtils.getJwtFromRequest(request);

        if (jwt == null) {
            if (laUrlCongKhai == false) {
                System.out.println("[JwtAuthenticationFilter] Khong co token -> tiep tuc");
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = (Claims) request.getAttribute("JWT_CLAIMS");
            if (claims == null) {
                claims = jwtService.extractAllClaims(jwt);
                request.setAttribute("JWT_CLAIMS", claims);
            } else {
                System.out.println("[JwtAuthenticationFilter] Tai su dung JWT_CLAIMS trong request.");
            }
            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<String> authorities = layAuthoritiesTuClaims(claims);
                List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

                Object[] mangAuthority = authorities.toArray();
                for (int i = 0; i < mangAuthority.length; i = i + 1) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(mangAuthority[i].toString()));
                }

                apDungRequestAttributes(request, claims);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, grantedAuthorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                request.setAttribute("RAW_JWT_TOKEN", jwt);
                System.out.println("[JwtAuthenticationFilter] Cap authentication tu token thanh cong [Buoc 1/2]");
            }
        } catch (Exception e) {
            cookieUtils.clearJwtCookie(response);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    private List<String> layAuthoritiesTuClaims(Claims claims) {
        Object rawAuthorities = claims.get(SecurityTokenConstants.CLAIM_AUTHORITIES);
        List<String> authorities = chuyenThanhDanhSachChuoi(rawAuthorities);
        if (authorities.isEmpty() == false) {
            return authorities;
        }

        // Tuong thich token doi cu: claim roles cua phien ban chuan duoc convert thanh ROLE_*.
        Object legacyRoles = claims.get("roles");
        List<String> roles = chuyenThanhDanhSachChuoi(legacyRoles);
        List<String> legacyAuthorities = new ArrayList<>();
        Object[] mangRole = roles.toArray();
        for (int i = 0; i < mangRole.length; i = i + 1) {
            String rawRole = mangRole[i].toString();
            String cleanRole = rawRole.replace("ROLE_", "");
            themNeuChuaCo(legacyAuthorities, "ROLE_" + cleanRole);
        }
        return legacyAuthorities;
    }

    private void apDungRequestAttributes(HttpServletRequest request, Claims claims) {
        Object rawExposed = claims.get(SecurityTokenConstants.CLAIM_EXPOSED_ATTRIBUTES);
        List<String> exposedAttributes = chuyenThanhDanhSachChuoi(rawExposed);
        Object[] mangAttr = exposedAttributes.toArray();
        for (int i = 0; i < mangAttr.length; i = i + 1) {
            String attrName = mangAttr[i].toString();
            Object attrValue = claims.get(attrName);
            if (attrValue != null) {
                request.setAttribute(attrName, attrValue);
            }
        }
    }

    private List<String> chuyenThanhDanhSachChuoi(Object rawValue) {
        List<String> ketQua = new ArrayList<>();
        if (rawValue == null) {
            return ketQua;
        }
        if (rawValue instanceof List) {
            List<?> danhSachRaw = (List<?>) rawValue;
            Object[] mangRaw = danhSachRaw.toArray();
            for (int i = 0; i < mangRaw.length; i = i + 1) {
                Object item = mangRaw[i];
                if (item != null) {
                    themNeuChuaCo(ketQua, item.toString());
                }
            }
            return ketQua;
        }
        themNeuChuaCo(ketQua, rawValue.toString());
        return ketQua;
    }

    private void themNeuChuaCo(List<String> danhSach, String giaTriRaw) {
        if (giaTriRaw == null) {
            return;
        }
        String giaTri = giaTriRaw.trim();
        if (giaTri.isEmpty()) {
            return;
        }
        Object[] mangGiaTri = danhSach.toArray();
        for (int i = 0; i < mangGiaTri.length; i = i + 1) {
            if (giaTri.equals(mangGiaTri[i].toString())) {
                return;
            }
        }
        danhSach.add(giaTri);
    }
}
