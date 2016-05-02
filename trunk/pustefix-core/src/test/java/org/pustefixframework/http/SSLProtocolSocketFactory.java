package org.pustefixframework.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

public class SSLProtocolSocketFactory implements SecureProtocolSocketFactory {

    private SSLSocketFactory sslSocketFactory;
    
    private SSLSocketFactory getSSLSocketFactory() {
        if(sslSocketFactory == null) {
        try {
            //Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        } catch(Exception x) {
            throw new RuntimeException("Can't get SSLSocketFactory", x);
        }
        }
        return sslSocketFactory;
    }

    public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort) throws IOException, UnknownHostException {
        return getSSLSocketFactory().createSocket(host, port, localAddress, localPort);
    }

    public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort, HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        return getSSLSocketFactory().createSocket(host, port, localAddress, localPort);
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return getSSLSocketFactory().createSocket(host, port);
    }

    public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        return getSSLSocketFactory().createSocket(socket, host, port, autoClose);
    }

    
}
