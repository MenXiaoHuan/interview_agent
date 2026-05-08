package com.multimodal.interview.common.security;

import com.multimodal.interview.common.exception.BusinessException;
import com.multimodal.interview.dto.ForgotResetRequest;
import com.multimodal.interview.dto.LoginRequest;
import com.multimodal.interview.dto.PasswordUpdateRequest;
import com.multimodal.interview.dto.RegisterRequest;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * 密码字段 RSA 加解密服务。
 *
 * <p>该服务仅处理带有 RSA 前缀的密文字段，未加密的旧请求将按原样透传，
 * 以兼容尚未升级的调用方。</p>
 */
@Service
public class RsaPasswordCryptoService {

    private static final String ENCRYPTED_PREFIX = "RSA:";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int KEY_SIZE = 2048;

    private final KeyPair keyPair;

    public RsaPasswordCryptoService() {
        this.keyPair = generateKeyPair();
    }

    /**
     * 获取当前服务实例的公钥 PEM。
     *
     * @return 公钥 PEM 文本
     */
    public String getPublicKeyPem() {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(keyPair.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n"
                + encoded
                + "\n-----END PUBLIC KEY-----";
    }

    /**
     * 解密登录请求中的密码字段。
     *
     * @param request 登录请求
     * @return 已处理的请求对象
     */
    public LoginRequest decrypt(LoginRequest request) {
        request.setPassword(decryptIfNeeded(request.getPassword()));
        return request;
    }

    /**
     * 解密注册请求中的密码字段。
     *
     * @param request 注册请求
     * @return 已处理的请求对象
     */
    public RegisterRequest decrypt(RegisterRequest request) {
        request.setPassword(decryptIfNeeded(request.getPassword()));
        request.setConfirmPassword(decryptIfNeeded(request.getConfirmPassword()));
        return request;
    }

    /**
     * 解密忘记密码重置请求中的密码字段。
     *
     * @param request 重置密码请求
     * @return 已处理的请求对象
     */
    public ForgotResetRequest decrypt(ForgotResetRequest request) {
        request.setNewPassword(decryptIfNeeded(request.getNewPassword()));
        return request;
    }

    /**
     * 解密用户修改密码请求中的密码字段。
     *
     * @param request 修改密码请求
     * @return 已处理的请求对象
     */
    public PasswordUpdateRequest decrypt(PasswordUpdateRequest request) {
        request.setOldPassword(decryptIfNeeded(request.getOldPassword()));
        request.setNewPassword(decryptIfNeeded(request.getNewPassword()));
        return request;
    }

    /**
     * 如字段带有 RSA 前缀，则执行解密；否则按原值返回。
     *
     * @param value 原始字段值
     * @return 明文字段值
     */
    public String decryptIfNeeded(String value) {
        if (value == null || value.isBlank() || !value.startsWith(ENCRYPTED_PREFIX)) {
            return value;
        }

        String cipherText = value.substring(ENCRYPTED_PREFIX.length());
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT
            ));
            byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw BusinessException.badRequest("密码解密失败，请刷新页面后重试");
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("RSA 密钥对初始化失败", exception);
        }
    }
}
