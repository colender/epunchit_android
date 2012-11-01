package com.epunchit.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import android.util.Log;


public class CustomSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory
{
	private SSLSocketFactory FACTORY = HttpsURLConnection.getDefaultSSLSocketFactory ();
	
	private SSLContext sslContext = SSLContext.getInstance("TLS");

	public CustomSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			UnrecoverableKeyException {
		super(null);
		try {
			sslContext = SSLContext.getInstance("TLS");
			TrustManager[] tm = new TrustManager[] { new FullX509TrustManager() };
			sslContext.init(null, tm, new SecureRandom());

			FACTORY = sslContext.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new FullX509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		sslContext.init(null, new TrustManager[] { tm }, null);
		FACTORY = sslContext.getSocketFactory();
		
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	/**
	 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
	 *      java.lang.String, int, boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
			UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}
	private static SSLContext createEasySSLContext() throws IOException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new FullX509TrustManager() }, null);
			return context;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	private SSLContext getSSLContext() throws IOException {
		if (this.sslContext == null) {
			this.sslContext = createEasySSLContext();
		}
		return this.sslContext;
	}
}
