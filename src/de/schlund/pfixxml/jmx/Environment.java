package de.schlund.pfixxml.jmx;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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
    public static HashMap create(File keystore) {
        HashMap env = new HashMap();

		env.put("jmx.remote.server.address.wildcard", "false");
		env.put("jmx.remote.profiles", "TLS");
        env.put("jmx.remote.tls.socket.factory", createSSLFactory(keystore));
        env.put("jmx.remote.tls.enabled.protocols", "TLSv1");
        env.put("jmx.remote.tls.need.client.authentication", "true");
		// the original jmx example used "SSL_RSA_WITH_NULL_MD5", but I get "no cipher suites 
        // in common" if I use that. SSL + TLS define valid values
		env.put("jmx.remote.tls.enabled.cipher.suites", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA");
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
