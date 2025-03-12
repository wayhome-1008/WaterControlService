package com.zjtc.helper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @Author: way @CreateTime: 2025-03-07 15:22 @Description: TODO
 */
public class DecryptAndEncryptionHelper implements AutoCloseable {
  private static final String key = "www.ixiaobai.net";
  private static final String iv = "www.ixiaobai.net";
  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
  private final Cipher cipher;
  private final SecretKeySpec secretKeySpec;
  private final IvParameterSpec ivParameterSpec;

  public DecryptAndEncryptionHelper() throws Exception {
    cipher = Cipher.getInstance(TRANSFORMATION);
    secretKeySpec = new SecretKeySpec(getLegalKey(key), ALGORITHM);
    ivParameterSpec = new IvParameterSpec(getLegalIV(iv));
  }

  private byte[] getLegalKey(String key) {
    // 假设使用 256 位密钥，即 32 字节
    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
    byte[] legalKey = new byte[32];
    System.arraycopy(keyBytes, 0, legalKey, 0, Math.min(keyBytes.length, legalKey.length));
    return legalKey;
  }

  private byte[] getLegalIV(String iv) {
    // 假设使用 128 位 IV，即 16 字节
    byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);
    byte[] legalIV = new byte[16];
    System.arraycopy(ivBytes, 0, legalIV, 0, Math.min(ivBytes.length, legalIV.length));
    return legalIV;
  }

  // 加密方法
  public String encrypt(String source) throws Exception {
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
    byte[] inputBytes = source.getBytes(StandardCharsets.UTF_8);
    byte[] outputBytes = cipher.doFinal(inputBytes);
    return Base64.getEncoder().encodeToString(outputBytes);
  }

  // 解密方法
  public String decrypt(String source) throws Exception {
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
    byte[] inputBytes = Base64.getDecoder().decode(source);
    byte[] outputBytes = cipher.doFinal(inputBytes);
    return new String(outputBytes, StandardCharsets.UTF_8);
  }

  @Override
  public void close() throws Exception {}
}
