package main;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import constants.Constants;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        System.out.println("Runtime.getRuntime().maxMemory()="+Runtime.getRuntime().maxMemory());

        try {
            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(443);

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] password = SecretSomething.sslPassword.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("chitchatserver.com.jks");
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception e) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });

            HTTPSHelper helper = new HTTPSHelper();
            httpsServer.createContext(Constants.GET_CHAT_URI.toString(), helper);
            httpsServer.createContext(Constants.GET_REMAINING_URI.toString(), helper);
            httpsServer.createContext(Constants.REGISTER_USER_URI.toString(), helper);
            httpsServer.createContext(Constants.GET_IMPORTANT_CONSTANTS_URI.toString(), helper);
            httpsServer.createContext(Constants.FULL_VALIDATE_PREMIUM_URI.toString(), helper);
            httpsServer.createContext(Constants.QUICK_VALIDATE_PREMIUM_URI.toString(), helper);
            httpsServer.createContext(Constants.GET_IAP_STUFF_URI.toString(), helper);
            httpsServer.createContext(Constants.PRIVACY_POLICY_URI.toString(), helper);
            httpsServer.createContext(Constants.TERMS_AND_CONDITIONS_URI.toString(), helper);
            httpsServer.createContext(Constants.PRINT_ALL_ACTIVE_SUBSCRIPTIONS_URI.toString(), helper);

            // Legacy
            httpsServer.createContext(Constants.GET_DISPLAY_PRICE_URI.toString(), helper);
            httpsServer.createContext(Constants.GET_SHARE_URL_URI.toString(), helper);
            httpsServer.setExecutor(Executors.newFixedThreadPool(4)); // creates a default executor
            httpsServer.start();

//            System.out.println(AIHelper.generateImage("baby elephants", 512, 512, 0.8, 1, 10, 7.5, -1));


//            DatabaseHelper dh = new DatabaseHelper();
//            User user = dh.registerUser();
//
//            System.out.println(user);
//
//            dh.login(user.getUserID());
//
//            System.out.println(dh.getLogins(user.getUserID()));
//
//
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
//        catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (SomethingWeirdHappenedException e) {
//            throw new RuntimeException(e);
//        }


    }
}
