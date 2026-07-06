package com.community.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT（JSON Web Token）生成与验证配置类
 *
 * <p>基于 JJWT 库（io.jsonwebtoken）实现 JWT 的创建、解析和验证。
 * Token 中携带的自定义声明（Claims）包括：userId、username、role，
 * 用作前后端无状态身份认证的凭证。</p>
 *
 * <p>配置项（在 application.yml 中定义）：</p>
 * <ul>
 *   <li>{@code jwt.secret} — HMAC-SHA 签名密钥（至少 256 位）</li>
 *   <li>{@code jwt.expiration} — Token 有效期（单位：毫秒）</li>
 * </ul>
 *
 * <p>JWT 结构速览：Header.Payload.Signature</p>
 * <ul>
 *   <li><b>Header</b>：算法类型（HS256）和 Token 类型（JWT）</li>
 *   <li><b>Payload</b>：自定义数据（userId, username, role）+ 标准字段（sub, iat, exp）</li>
 *   <li><b>Signature</b>：用密钥对前两部分进行 HMAC-SHA256 签名，保证数据完整性</li>
 * </ul>
 *
 * @see <a href="https://github.com/jwtk/jjwt">JJWT 官方文档</a>
 */
@Component
public class JwtConfig {

    /** JWT 签名密钥，从配置文件注入（jwt.secret） */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT 有效期（毫秒），从配置文件注入（jwt.expiration），例如 86400000 = 24 小时 */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 根据配置的 secret 字符串生成 HMAC-SHA 签名密钥
     *
     * <p>使用 JJWT 提供的 {@link Keys#hmacShaKeyFor(byte[])} 方法，
     * 将字符串密钥转换为标准的 Java 加密密钥对象。</p>
     *
     * @return HMAC-SHA 密钥对象
     */
    private SecretKey getSigningKey() {
        // 将 UTF-8 编码的密钥字符串转换为字节数组，再生成 HMAC 密钥
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户信息生成 JWT Token
     *
     * <p>生成的 Token 包含以下信息：</p>
     * <ul>
     *   <li><b>自定义 Claims</b>：userId（用户ID）、username（用户名）、role（角色码）</li>
     *   <li><b>sub（Subject）</b>：用户名，标识 Token 的拥有者</li>
     *   <li><b>iat（Issued At）</b>：签发时间，当前时间</li>
     *   <li><b>exp（Expiration）</b>：过期时间 = 当前时间 + 配置的有效期</li>
     * </ul>
     *
     * <p>Token 使用 HMAC-SHA256 签名，签名密钥为配置的 secret。</p>
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色码（0=普通用户, 1=管理员, 2=超级管理员, 3=专职人员）
     * @return 签发生成的 JWT Token 字符串（三段 Base64，以 . 分隔）
     */
    public String generateToken(Long userId, String username, Integer role) {
        // 准备自定义 Claims 数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 构建 JWT：设置 Claims → Subject → 签发时间 → 过期时间 → 签名 → 生成字符串
        return Jwts.builder()
                .claims(claims)                                                     // 负载数据
                .subject(username)                                                  // Token 主体
                .issuedAt(new Date())                                               // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))      // 过期时间
                .signWith(getSigningKey())                                          // HMAC-SHA256 签名
                .compact();                                                         // 序列化为字符串
    }

    /**
     * 解析 JWT Token，提取其中的 Claims 数据
     *
     * <p>解析过程会进行签名验证和过期时间检查（过期 Token 会抛出 ExpiredJwtException）。
     * 解析成功返回 Claims 对象，包含 Token 中的所有自定义数据。</p>
     *
     * @param token JWT Token 字符串
     * @return 解析后的 Claims 对象，可从中提取 userId、username、role 等字段
     * @throws JwtException                 Token 格式不正确或签名无效
     * @throws IllegalArgumentException Token 为空
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // 指定验证签名所用的密钥
                .build()                       // 构建解析器
                .parseSignedClaims(token)      // 解析并验证签名
                .getPayload();                 // 获取负载 Claims
    }

    /**
     * 验证 Token 是否合法且未过期
     *
     * <p>验证步骤：</p>
     * <ol>
     *   <li>解析 Token，验证签名完整性</li>
     *   <li>检查 Token 是否已过期（当前时间 >= 过期时间）</li>
     * </ol>
     *
     * @param token JWT Token 字符串
     * @return true = 有效 Token，false = 无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查过期时间：当前时间必须在过期时间之前才是有效的
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // 签名无效、格式错误、Token 为空等情况均视为无效
            return false;
        }
    }

    /**
     * 从 Token 中提取用户名（即 JWT Subject 字段）
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中提取用户 ID（自定义 Claim: userId）
     *
     * @param token JWT Token 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从 Token 中提取角色码（自定义 Claim: role）
     * <p>角色码含义：0=普通用户，1=管理员，2=超级管理员，3=专职人员</p>
     *
     * @param token JWT Token 字符串
     * @return 角色码
     */
    public Integer getRoleFromToken(String token) {
        return parseToken(token).get("role", Integer.class);
    }
}
