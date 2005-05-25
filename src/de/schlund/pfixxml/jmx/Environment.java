package de.schlund.pfixxml.jmx;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Configures client-server communication:
 * o  ssl
 * o  client authentication
 */
public class Environment {
    public static void main(String[] args) {
        String[] suites;
        int i;
        
        suites = getCipherSuites();
        for (i = 0; i < suites.length; i++) {
            System.out.println(i + " " + suites[i]);
        }

     /* environment on horst
          0 SSL_RSA_WITH_RC4_128_MD5
          1 SSL_RSA_WITH_RC4_128_SHA
          2 TLS_RSA_WITH_AES_128_CBC_SHA
          3 TLS_DHE_RSA_WITH_AES_128_CBC_SHA
          4 TLS_DHE_DSS_WITH_AES_128_CBC_SHA
          5 SSL_RSA_WITH_3DES_EDE_CBC_SHA
          6 SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA
          7 SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA
          8 SSL_RSA_WITH_DES_CBC_SHA
          9 SSL_DHE_RSA_WITH_DES_CBC_SHA
          10 SSL_DHE_DSS_WITH_DES_CBC_SHA
          11 SSL_RSA_EXPORT_WITH_RC4_40_MD5
          12 SSL_RSA_EXPORT_WITH_DES40_CBC_SHA
          13 SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA
          14 SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA
          15 SSL_RSA_WITH_NULL_MD5
          16 SSL_RSA_WITH_NULL_SHA
          17 SSL_DH_anon_WITH_RC4_128_MD5
          18 TLS_DH_anon_WITH_AES_128_CBC_SHA
          19 SSL_DH_anon_WITH_3DES_EDE_CBC_SHA
          20 SSL_DH_anon_WITH_DES_CBC_SHA
          21 SSL_DH_anon_EXPORT_WITH_RC4_40_MD5
          22 SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA

     cipher suites on pustefix51
         0 SSL_RSA_WITH_RC4_128_MD5
         1 SSL_RSA_WITH_RC4_128_SHA
         2 SSL_RSA_WITH_3DES_EDE_CBC_SHA
         3 SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA
         4 SSL_RSA_WITH_DES_CBC_SHA
         5 SSL_DHE_DSS_WITH_DES_CBC_SHA
         6 SSL_RSA_EXPORT_WITH_RC4_40_MD5
         7 SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA
         8 SSL_RSA_WITH_NULL_MD5
         9 SSL_RSA_WITH_NULL_SHA
         10 SSL_DH_anon_WITH_RC4_128_MD5
         11 SSL_DH_anon_WITH_3DES_EDE_CBC_SHA
         12 SSL_DH_anon_WITH_DES_CBC_SHA
         13 SSL_DH_anon_EXPORT_WITH_RC4_40_MD5
         14 SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA
     */ 
    }

    private static String[] getCipherSuites() {
        return ((SSLSocketFactory) SSLSocketFactory.getDefault()).getSupportedCipherSuites();
    }

    // Note 1: Sun's jmx example used "SSL_RSA_WITH_NULL_MD5", but I got "no cipher suites 
    // in common" if I use that.
    // Note 2: My second try was with "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", but this caused
    //    Unsupported ciphersuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA
    // exceptions when used in pustefixNN.
    private static final String CIPHER_SUITE = "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA";
    
    public static void assertCipher() {
        if (!Arrays.asList(getCipherSuites()).contains(CIPHER_SUITE)) {
            throw new RuntimeException("cipher suite not supported: " + CIPHER_SUITE);
        }
    }
    
    /** you might want to switch off security to access with jmx consoles */
    public static HashMap create(File keystore, boolean secure) {
        HashMap env = new HashMap();

		env.put("jmx.remote.server.address.wildcard", "false");
        if (secure) {
            env.put("jmx.remote.profiles", "TLS");
            env.put("jmx.remote.tls.socket.factory", createSSLFactory(keystore));
            env.put("jmx.remote.tls.enabled.protocols", "TLSv1");
            env.put("jmx.remote.tls.need.client.authentication", "true");
            env.put("jmx.remote.tls.enabled.cipher.suites", CIPHER_SUITE);
        }
        return env;
    }

    //--
    
    private static final String PASSWORD = "password";
    private static final String SUN = "SunX509";
    
    private static SSLSocketFactory createSSLFactory(File keystore) {
        SSLContext ctx;
        
        try {
			ctx = SSLContext.getInstance("TLSv1");
	        ctx.init(getKeyManagers(keystore), getTrustManagers(keystore), null);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return ctx.getSocketFactory();
    }

    private static KeyManager[] getKeyManagers(File keystore)  throws GeneralSecurityException, IOException {
        KeyStore ks = loadKeystore(keystore);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(SUN);
        kmf.init(ks, PASSWORD.toCharArray());
        return kmf.getKeyManagers();
    }
    
    private static TrustManager[] getTrustManagers(File keystore) throws GeneralSecurityException, IOException {
        KeyStore ks = loadKeystore(keystore);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(SUN);
        tmf.init(ks);
        return tmf.getTrustManagers();
    }

    private static KeyStore loadKeystore(File file) throws GeneralSecurityException, IOException {
        char keystorepass[] = PASSWORD.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(file), keystorepass);
        return ks;
    }
}
