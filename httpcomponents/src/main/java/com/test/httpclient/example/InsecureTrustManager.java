package com.test.httpclient.example;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

class InsecureTrustManager extends X509ExtendedTrustManager {
    private final Set<X509Certificate> acceptedIssuers_ = new HashSet<>();

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        acceptedIssuers_.addAll(Arrays.asList(chain));
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        acceptedIssuers_.addAll(Arrays.asList(chain));
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        acceptedIssuers_.addAll(Arrays.asList(chain));
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        acceptedIssuers_.addAll(Arrays.asList(chain));
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine sslEngine) throws CertificateException {
        acceptedIssuers_.addAll(Arrays.asList(chain));
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine sslEngine) throws CertificateException {
        acceptedIssuers_.addAll(Arrays.asList(chain));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (acceptedIssuers_.isEmpty()) {
            return new X509Certificate[0];
        }
        return acceptedIssuers_.toArray(new X509Certificate[0]);
    }
}
