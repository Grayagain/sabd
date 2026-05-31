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
import java.security.*;
import java.security.cert.*;
import java.util.Collection;
import java.util.Collections;

public class App {

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

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        System.out.println("Max Key Size for AES : " + maxKeySize);

        X509Certificate cert = (X509Certificate)
                CertificateFactory.getInstance("X.509")
                        .generateCertificate(new FileInputStream("public.cer"));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream("private.p12"), "password".toCharArray());

        PrivateKey key = (PrivateKey)
                ks.getKey("baeldung", "password".toCharArray());

        String secretMessage = "My password is 123456Seven";

        System.out.println("Original Message : " + secretMessage);

        byte[] encryptedData = encryptData(secretMessage.getBytes(), cert);

        System.out.println("Encrypted Message : " + new String(encryptedData));

        byte[] decryptedData = decryptData(encryptedData, key);

        String decryptedMessage = new String(decryptedData);

        System.out.println("Decrypted Message : " + decryptedMessage);

        byte[] signedData = signData(decryptedData, cert, key);

        boolean verified = verify(signedData);

        System.out.println("Verified : " + verified);
    }
}