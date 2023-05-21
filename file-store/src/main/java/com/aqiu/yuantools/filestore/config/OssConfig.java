package com.aqiu.yuantools.filestore.config;

import com.aliyun.oss.*;
import com.aliyun.oss.crypto.SimpleRSAEncryptionMaterials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author: yuanyang
 * @date: 2022-12-12 19:45
 * @desc:
 */
@Configuration
public class OssConfig {

    @Value("${alibaba.cloud.access-key}")
    private String accessKey;
    @Value("${alibaba.cloud.secret-key}")
    private String accessSecret;
    @Value("${alibaba.cloud.oss.endpoint}")
    private String endpoint;
    private static final String PRIVATE_KEY_STR = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEowIBAAKCAQEAvOE0hZ7M/u6o5EYa6mZSxk/tTb6XRd6s1GRyXNdxpD7ypCGh\n" +
            "uF+5Qf9hG047Kbzzyk5e5kS76NVFX8pon0E89RDPQzs5hZ1izYV03u3xkI+B0Csb\n" +
            "ctYqAKTece8Wc5DHXxIsYOUTB4u7IiBcXstFsNXWNUOl1wKHEpqc2ONZv6t4m/Z1\n" +
            "uB6FGeevsEVHGVR+yFrrgWV/enXH8KSTlcyvkSPJ/FLyPNqVSxcse1PmAR5jhzi5\n" +
            "FEfcLanNZGWwl0S82mIbYoczNzq76UTANtVElm625qhdpYkgwsjGVwIfRAPV1n4B\n" +
            "7Jxs7IRWd77sIz6SVgHbLlmrXA4TpNP7Nd1feQIDAQABAoIBAEOkKxkHK8bIVXea\n" +
            "m50CE/atPQCwlqARBLfzWPlitnHpkR+yY6YRsdiuymnq9EkYP/5dXqL8ToctiXCq\n" +
            "nhkaVeg1ouQi25C8MlwDxTzo3a+1lPml1mijxdsBMCCBTPsKCDxF19rDbrMvGU85\n" +
            "Y5sXFNglXFcq6MM9Rn4lHLKXiNaYxS95U1WThzmVW3XnZN7GTgrJ06Uyj1O4DQrm\n" +
            "OfCcTwFnSiAFV0NxdgWZ1FtD36Qs01iiokknhad+5AcTczAejhzEWqtKoKp+ByzG\n" +
            "cU26KT/nWGzglz9I+SjlNprBWoi6/w0mM7pzYv26eieLxYPwIfitbGyKlFGyU+D7\n" +
            "c/OddXkCgYEA9/qMJJqcVvS6rkrCXKXbc7P4vGt1g5DcGTLFu5lwUjbJZoDNt5oB\n" +
            "VXTj8FM5H4CFEQI4NtuTUew9WUhH8N4DhmKQHY9+gYx3PVdNmr6HPRH46pVBXgf4\n" +
            "lSVi9vTYt+Gio5B/P1yAmtc9ClFB2o3jqGLbtzwaWGvX5ScExjo/AkMCgYEAwv1F\n" +
            "5g8OueFpa/46vjrkvEWIyrmitZPfs2C/91jES8BV2zB7fEp9WScG0ee/YdNATNp+\n" +
            "2gH5KVeEMY1oWoc2t5lSunwsiWJaENTgXt0TEwj69Cv04eeaRSG5Ut0Av49YfppR\n" +
            "j3yYBR5FqaCyjDWMEHHQX5E5iJm1MZMsdM1a8ZMCgYAdcRTUf7rSJCpJ6TxcTaDZ\n" +
            "guOkU0nXfgpzv3B0jookaLbOwboq81D3OXKapPbiQ5sI4u+Tq8w47Mh34joQYfuf\n" +
            "J/KeX9wQO4IouUXQepCJ8qlQCLqDFIUyCAdQ9M8KPfLWYqCJAE2QlL99ixo7fZ8q\n" +
            "7dnSToN+PgebAF/zvRSgCwKBgCn3Ssj16qx06DSW9dulXz9qE8PV6j/8QIQhyY8Z\n" +
            "qr3G+nN/XsRkX9AFR0F8CEOsrMUdrXmMPSz5qfjkP1ZkjZE8TYLXYNp8sXe36UZh\n" +
            "fE53e1N79mt8ZkaEuJct+5A+8MPmwLEKFsnbNxEIBgWJLMkJRdibr+9HbajqzQGi\n" +
            "hPVDAoGBALRn8kvzHrPq+mn/AmdA+fvMnx5iVvLtRNpXIKCSR5q8Sk62zwr91Ki7\n" +
            "OxJoyRNXrtSffsqwLz4OTHafBcf3n1CP7k1ufm0F05rdREjPiTeUDoMUM5I07gB+\n" +
            "4XdguKG5HrlAN3DD43tF2TmIbYj9FmtFYcCKabdq5YqpFe/Q7kEw\n" +
            "-----END RSA PRIVATE KEY-----";
    private static final String PUBLIC_KEY_STR = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvOE0hZ7M/u6o5EYa6mZS\n" +
            "xk/tTb6XRd6s1GRyXNdxpD7ypCGhuF+5Qf9hG047Kbzzyk5e5kS76NVFX8pon0E8\n" +
            "9RDPQzs5hZ1izYV03u3xkI+B0CsbctYqAKTece8Wc5DHXxIsYOUTB4u7IiBcXstF\n" +
            "sNXWNUOl1wKHEpqc2ONZv6t4m/Z1uB6FGeevsEVHGVR+yFrrgWV/enXH8KSTlcyv\n" +
            "kSPJ/FLyPNqVSxcse1PmAR5jhzi5FEfcLanNZGWwl0S82mIbYoczNzq76UTANtVE\n" +
            "lm625qhdpYkgwsjGVwIfRAPV1n4B7Jxs7IRWd77sIz6SVgHbLlmrXA4TpNP7Nd1f\n" +
            "eQIDAQAB\n" +
            "-----END PUBLIC KEY-----";

    /**
     * 获取加解密客户端
     * @return
     */
    @Bean("ossEncryptionClient")
    public OSSEncryptionClient ossEncryptionClient() {
        // 创建一个RSA密钥对
        RSAPrivateKey privateKey = SimpleRSAEncryptionMaterials.getPrivateKeyFromPemPKCS1(PRIVATE_KEY_STR);
        RSAPublicKey publicKey = SimpleRSAEncryptionMaterials.getPublicKeyFromPemX509(PUBLIC_KEY_STR);
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        /*
         * 创建主密钥RSA的描述信息，创建后不允许修改，主密钥描述信息和主密钥一一对应
         * 如果所有的Object都使用相同的主密钥，主密钥描述信息可以为空，但后续不支持更换主密钥
         * 如果主密钥描述信息为空，解密时无法判断文件使用的是哪个主密钥进行加密
         * 强烈建议为每个主密钥都配置描述信息，由客户端保存主密钥和描述信息之间的对应关系（服务端不保存两者之间的对应关系）
         */
//        Map<String, String> matDesc = new HashMap<>();
//        matDesc.put("desc-key", "desc-value");
//        SimpleRSAEncryptionMaterials encryptionMaterials = new SimpleRSAEncryptionMaterials(keyPair, matDesc);

        // 创建RSA加密材料
        SimpleRSAEncryptionMaterials encryptionMaterials = new SimpleRSAEncryptionMaterials(keyPair, null);
        // 如果要下载并解密其他RSA密钥加密的文件，请将其他主密钥及其描述信息添加到加密材料中
//         encryptionMaterials.addKeyPairDescMaterial(<otherKeyPair>, <otherKeyPairMatDesc>);

        // 创建RSA加密客户端
        return new OSSEncryptionClientBuilder().build(endpoint, accessKey, accessSecret, encryptionMaterials);
    }

    /**
     * 获取加解密客户端
     * @return
     */
    @Bean("ossNotEncryptionClient")
    public OSS ossNotEncryptionClient() {
        // 创建非加密客户端
        return new OSSClientBuilder().build(endpoint, accessKey, accessSecret);
    }
}
