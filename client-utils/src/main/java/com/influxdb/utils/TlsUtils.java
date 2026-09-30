/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.influxdb.utils;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class TlsUtils {
    private static final String TLS = "TLS";
    private static final char[] DEFAULT_PASSWORD_CHAR_ARRAY = "".toCharArray();
    private static final String X509 = "X.509";
    private static final String PKCS12 = "PKCS12";

    private TlsUtils() {
    }

    /**
     * Builds an {@link SSLContext} using the provided {@link KeyManagerFactory} and/or
     * {@link TrustManagerFactory}. If both factories are null, this method returns null.
     *
     * @param kmf the {@link KeyManagerFactory} to use for key management, or null if no key management is required.
     * @param tmf the {@link TrustManagerFactory} to use for trust management, or null if no trust management is
     *            required.
     * @return an initialized {@link SSLContext}, or null if both input parameters are null.
     * @throws Exception if an error occurs during the SSLContext initialization.
     */
    @Nullable
    public static SSLContext buildSslContext(@Nullable final KeyManagerFactory kmf,
                                             @Nullable final TrustManagerFactory tmf) throws Exception {
        if (kmf == null && tmf == null) {
            return null;
        }

        SSLContext sslContext = SSLContext.getInstance(TLS);
        sslContext.init(kmf != null ? kmf.getKeyManagers() : null, tmf != null ? tmf.getTrustManagers() : null, null);
        return sslContext;
    }

    /**
     * Retrieves an instance of {@link X509TrustManager} from the provided {@link TrustManagerFactory}.
     * If the input TrustManagerFactory is null, a default TrustManagerFactory is created and initialized.
     *
     * @param tmf the {@link TrustManagerFactory} to retrieve the {@link X509TrustManager} from,
     *            or null to use a default {@link TrustManagerFactory}.
     * @return an instance of {@link X509TrustManager} initialized from the given or
     * default {@link TrustManagerFactory}.
     * @throws Exception if an error occurs during the initialization or retrieval of the {@link X509TrustManager}.
     */
    @Nonnull
    public static X509TrustManager getX509TrustManager(@Nullable final TrustManagerFactory tmf) throws Exception {
        TrustManagerFactory factory = tmf;
        if (factory == null) {
            factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
        }
        return (X509TrustManager) factory.getTrustManagers()[0];
    }

    /**
     * Loads a private key from a file specified by the provided path. The key must be in PKCS#8 format and unencrypted.
     * Encrypted private keys are not supported.
     *
     * @param path the file system path to the private key file; must not be null.
     * @return the {@link PrivateKey} object loaded from the provided file path.
     * @throws IllegalArgumentException if the private key is encrypted or in an unsupported format.
     * @throws Exception if an error occurs during file reading, Base64 decoding, or key generation.
     */
    public static PrivateKey loadPrivateKey(@Nonnull final String path) throws Exception {
        String keyPem = Files.readString(Paths.get(path));
        if (keyPem.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {
            throw new IllegalArgumentException("Encrypted PKCS#8 private keys are not supported. Use an unencrypted "
                    + "PKCS#8 key or a PKCS#12 file.");
        }

        String privateKeyPEM = keyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        GeneralSecurityException lastException = null;
        for (String algorithm : new String[]{"RSA", "EC", "DSA"}) {
            try {
                return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
            } catch (GeneralSecurityException e) {
                lastException = e;
            }
        }

        throw new GeneralSecurityException("Unsupported private key algorithm", lastException);
    }

    /**
     * Creates and initializes a {@link KeyManagerFactory} using a PKCS#12 keystore file
     * located at the specified path. This method loads the keystore using the provided
     * password (or a default password if none is provided) and initializes a
     * {@link KeyManagerFactory} with it.
     *
     * @param path the file path to the PKCS#12 keystore; must not be null.
     * @param password the password for the keystore, or null/empty if the default password
     *                 should be used.
     * @return a {@link KeyManagerFactory} instance initialized with the provided keystore.
     * @throws Exception if an error occurs while reading the file, loading the keystore,
     *                   or initializing the {@link KeyManagerFactory}.
     */
    public static KeyManagerFactory createKmfP12(@Nonnull final String path,
                                                 @Nullable final char[] password) throws Exception {
        char[] pass = password != null && password.length > 0 ? password : DEFAULT_PASSWORD_CHAR_ARRAY;
        KeyStore keyStore = KeyStore.getInstance(PKCS12);
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, pass);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, pass);
        return kmf;
    }

    /**
     * Creates and initializes a {@link KeyManagerFactory} using the specified certificate and private key files.
     * The method loads the certificate chain from the provided `certPath` and the private key from the provided
     * `keyPath`.
     * These are stored in a {@link KeyStore}, which is then used to initialize the {@link KeyManagerFactory}.
     *
     * @param certPath the file path to the certificate chain in X.509 format; must not be null.
     * @param keyPath the file path to the private key in PKCS#8 format; must not be null.
     * @return a {@link KeyManagerFactory} instance initialized with the provided certificate and private key.
     * @throws Exception if an error occurs while reading the files, loading the credentials, or initializing
     * the {@link KeyManagerFactory}.
     */
    public static KeyManagerFactory createKmf(@Nonnull final String certPath,
                                              @Nonnull final String keyPath) throws Exception {
        java.util.Collection<? extends Certificate> certificateChain;
        try (FileInputStream fis = new FileInputStream(certPath)) {
            certificateChain = CertificateFactory.getInstance(X509).generateCertificates(fis);
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setKeyEntry("alias",
                TlsUtils.loadPrivateKey(keyPath),
                null,
                certificateChain.toArray(new Certificate[0]));

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, null);

        return kmf;
    }

    /**
     * Creates and initializes a TrustManagerFactory from a PKCS#12 keystore located at the specified path.
     * This method loads the keystore using the provided password and initializes a TrustManagerFactory with it.
     *
     * @param path the file path to the PKCS#12 keystore; must not be null.
     * @param password the password for the keystore, or null/empty if the default password should be used.
     * @return a TrustManagerFactory instance initialized with the provided keystore.
     * @throws Exception if an error occurs while reading the file, loading the keystore,
     *                   or initializing the TrustManagerFactory.
     */
    public static TrustManagerFactory createTmfP12(@Nonnull final String path,
                                                   @Nullable final char[] password) throws Exception {
        char[] pass = password != null && password.length > 0 ? password : DEFAULT_PASSWORD_CHAR_ARRAY;

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(path)) {
            trustStore.load(fis, pass);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        return tmf;
    }

    /**
     * Creates and initializes a TrustManagerFactory from the provided certificate file.
     * The file format is determined based on its extension. Supported formats include:
     * - .p12 or .pfx: Loaded as a PKCS#12 keystore.
     * - .crt, .cert, or .pem: Loaded as individual X.509 certificates.
     *
     * @param path the file path to the certificate or keystore; must not be null.
     * @param password the password for the keystore, or null if not required.
     * @return a TrustManagerFactory instance initialized with the provided certificates or keystore.
     * @throws Exception if an error occurs while reading the file, processing the certificates,
     *                   or initializing the TrustManagerFactory.
     * @throws IllegalArgumentException if the provided file format is unsupported.
     */
    public static TrustManagerFactory createTmf(@Nonnull final String path,
                                                @Nullable final char[] password) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        String extension = path.toLowerCase(Locale.ROOT);

        if (extension.endsWith(".p12") || extension.endsWith(".pfx")) {
            char[] pass = password != null && password.length > 0 ? password : DEFAULT_PASSWORD_CHAR_ARRAY;
            tmf = createTmfP12(path, pass);
        } else if (extension.endsWith(".crt") || extension.endsWith(".cert") || extension.endsWith(".pem")) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            CertificateFactory certFactory = CertificateFactory.getInstance(X509);
            try (FileInputStream fis = new FileInputStream(path)) {
                int alias = 0;
                for (Certificate certificate : certFactory.generateCertificates(fis)) {
                    trustStore.setCertificateEntry("alias-" + alias++, certificate);
                }
            }
            tmf.init(trustStore);
        } else {
            throw new IllegalArgumentException("Unsupported certificate format");
        }

        return tmf;
    }
}
