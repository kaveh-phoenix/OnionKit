package info.guardianproject.onionkit.trust;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.scheme.LayeredSchemeSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpParams;

import android.content.Context;

public class StrongSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory implements LayeredSchemeSocketFactory
{
	
	private SSLSocketFactory mFactory = null;

    private Proxy mProxy = null;
    
    public static final String TLS   = "TLS";
    public static final String SSL   = "SSL";
    public static final String SSLV2 = "SSLv2";
    
    private X509HostnameVerifier mHostnameVerifier = new StrictHostnameVerifier();
    //private final HostNameResolver mNameResolver = new StrongHostNameResolver();

    
	public StrongSSLSocketFactory (Context context) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
    	super(null);
 
        SSLContext sslContext = SSLContext.getInstance ("TLS");
        StrongTrustManager tmStrong = new StrongTrustManager (context);
        TrustManager[] tm = new TrustManager[] { tmStrong };
        KeyManager[] km = createKeyManagers(tmStrong.getTrustStore(),tmStrong.getTrustStorePassword());
        sslContext.init (km, tm, new SecureRandom ());

        mFactory = sslContext.getSocketFactory ();
   
    }

	private KeyManager[] createKeyManagers(final KeyStore keystore, final String password)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (keystore == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keystore, password != null ? password.toCharArray(): null);
        return kmfactory.getKeyManagers(); 
    }

	@Override
	public Socket createSocket() throws IOException
	{
	    return mFactory.createSocket();
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
	
		return mFactory.createSocket(socket, host, port, autoClose);
	}

	

	@Override
	public boolean isSecure(Socket sock) throws IllegalArgumentException {
		return (sock instanceof SSLSocket);
	}
	

	public void setProxy (Proxy proxy) {
		mProxy = proxy;
	}
	
	public Proxy getProxy ()
	{
		return mProxy;
	}
	
	class StrongHostNameResolver implements HostNameResolver
	{

		@Override
		public InetAddress resolve(String host) throws IOException {
			
			//can we do proxied name look ups here?
			
			//what can we implement to make name resolution strong
			
			return InetAddress.getByName(host);
		}
		
	}

	@Override
	public Socket connectSocket(Socket sock, InetSocketAddress arg1,
			InetSocketAddress arg2, HttpParams arg3) throws IOException,
			UnknownHostException, ConnectTimeoutException {
	
		return connectSocket(sock, arg1.getHostName(), arg1.getPort(), InetAddress.getByName(arg2.getHostName()), arg2.getPort(),arg3);
	}

	@Override
	public Socket createSocket(HttpParams arg0) throws IOException {
		
		return createSocket();
		
	}

	@Override
	public Socket createLayeredSocket(Socket arg0, String arg1, int arg2,
			boolean arg3) throws IOException, UnknownHostException {
		return ((LayeredSchemeSocketFactory)mFactory).createLayeredSocket(arg0, arg1, arg2, arg3);
	}
	
}