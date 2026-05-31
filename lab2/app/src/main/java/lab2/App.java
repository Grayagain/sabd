package lab2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.operator.jcajce.*;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;

public class App {

    private static final String DEFAULT_CERT_PATH = "public.cer";
    private static final String DEFAULT_KEYSTORE_PATH = "private.p12";
    private static final String DEFAULT_KEY_ALIAS = "baeldung";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "password";
    private static final String DEFAULT_MESSAGE = "My password is 123456Seven";

    static void ensureCryptoSetup() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        Security.setProperty("crypto.policy", "unlimited");
    }

    static X509Certificate loadCertificate(String certificatePath) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(certificatePath)) {
            return (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(inputStream);
        }
    }

    static PrivateKey loadPrivateKey(String keystorePath, String alias, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (FileInputStream inputStream = new FileInputStream(keystorePath)) {
            keyStore.load(inputStream, password.toCharArray());
        }

        return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    }

    public static byte[] encryptData(byte[] data, X509Certificate cert) throws Exception {
        CMSEnvelopedDataGenerator generator = new CMSEnvelopedDataGenerator();

        generator.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(cert)
        );

        CMSTypedData msg = new CMSProcessableByteArray(data);

        OutputEncryptor encryptor =
                new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                        .setProvider("BC")
                        .build();

        return generator.generate(msg, encryptor).getEncoded();
    }

    public static byte[] decryptData(byte[] encrypted, PrivateKey key) throws Exception {
        CMSEnvelopedData data = new CMSEnvelopedData(encrypted);

        RecipientInformation recipient =
                data.getRecipientInfos().getRecipients().iterator().next();

        return recipient.getContent(
                new JceKeyTransEnvelopedRecipient(key)
        );
    }

    public static byte[] signData(byte[] data, X509Certificate cert, PrivateKey key) throws Exception {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

        ContentSigner signer =
                new JcaContentSignerBuilder("SHA256withRSA").build(key);

        generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder()
                                .setProvider("BC")
                                .build()
                ).build(signer, cert)
        );

        generator.addCertificates(
                new JcaCertStore(Collections.singletonList(cert))
        );

        return generator.generate(
                new CMSProcessableByteArray(data),
                true
        ).getEncoded();
    }

    public static boolean verify(byte[] signedData) throws Exception {
        CMSSignedData cms = new CMSSignedData(signedData);

        SignerInformation signer =
                cms.getSignerInfos().getSigners().iterator().next();

        Collection<X509CertificateHolder> certs =
                cms.getCertificates().getMatches(signer.getSID());

        X509CertificateHolder certHolder =
                certs.iterator().next();

        return signer.verify(
                new JcaSimpleSignerInfoVerifierBuilder()
                        .build(certHolder)
        );
    }

    public static byte[] extractSignedContent(byte[] signedData) throws Exception {
        CMSSignedData cms = new CMSSignedData(signedData);
        CMSTypedData signedContent = cms.getSignedContent();

        if (signedContent == null) {
            return new byte[0];
        }

        return (byte[]) signedContent.getContent();
    }

    private static String decodeText(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] encodeText(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] decodeBase64(String data) {
        return Base64.getDecoder().decode(data);
    }

    private static void runDemo(X509Certificate cert, PrivateKey key, String message) throws Exception {
        System.out.println("Original Message : " + message);

        byte[] encryptedData = encryptData(encodeText(message), cert);
        System.out.println("Encrypted Message (Base64) : " + encodeBase64(encryptedData));

        byte[] decryptedData = decryptData(encryptedData, key);
        System.out.println("Decrypted Message : " + decodeText(decryptedData));

        byte[] signedData = signData(decryptedData, cert, key);
        System.out.println("Signed Message (Base64) : " + encodeBase64(signedData));
        System.out.println("Verified : " + verify(signedData));
    }

    private static void handleCommand(String[] args, X509Certificate cert, PrivateKey key) throws Exception {
        String command = args[0].toLowerCase();

        if ("demo".equals(command)) {
            String message = args.length >= 2 ? args[1] : DEFAULT_MESSAGE;
            runDemo(cert, key, message);
            return;
        }

        if (args.length != 2) {
            printUsage();
            return;
        }

        if ("encrypt".equals(command)) {
            byte[] encryptedData = encryptData(encodeText(args[1]), cert);
            System.out.println(encodeBase64(encryptedData));
            return;
        }

        if ("decrypt".equals(command)) {
            byte[] decryptedData = decryptData(decodeBase64(args[1]), key);
            System.out.println(decodeText(decryptedData));
            return;
        }

        if ("sign".equals(command)) {
            byte[] signedData = signData(encodeText(args[1]), cert, key);
            System.out.println(encodeBase64(signedData));
            return;
        }

        if ("verify".equals(command)) {
            byte[] signedData = decodeBase64(args[1]);
            System.out.println("Verified : " + verify(signedData));

            byte[] content = extractSignedContent(signedData);
            if (content.length > 0) {
                System.out.println("Signed Content : " + decodeText(content));
            }
            return;
        }

        if ("roundtrip".equals(command)) {
            byte[] encryptedData = encryptData(encodeText(args[1]), cert);
            byte[] decryptedData = decryptData(encryptedData, key);
            byte[] signedData = signData(decryptedData, cert, key);

            System.out.println("Encrypted (Base64) : " + encodeBase64(encryptedData));
            System.out.println("Decrypted : " + decodeText(decryptedData));
            System.out.println("Signed (Base64) : " + encodeBase64(signedData));
            System.out.println("Verified : " + verify(signedData));
            return;
        }

        printUsage();
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  demo [message]");
        System.out.println("  encrypt <message>");
        System.out.println("  decrypt <base64-cms>");
        System.out.println("  sign <message>");
        System.out.println("  verify <base64-cms>");
        System.out.println("  roundtrip <message>");
        System.out.println("Run from lab2/app so public.cer and private.p12 resolve correctly.");
    }

    public static void main(String[] args) throws Exception {

        ensureCryptoSetup();

        int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        System.out.println("Max Key Size for AES : " + maxKeySize);

        X509Certificate cert = loadCertificate(DEFAULT_CERT_PATH);
        PrivateKey key = loadPrivateKey(DEFAULT_KEYSTORE_PATH, DEFAULT_KEY_ALIAS, DEFAULT_KEYSTORE_PASSWORD);

        if (args.length == 0) {
            runDemo(cert, key, DEFAULT_MESSAGE);
            return;
        }

        handleCommand(args, cert, key);
    }
}
