package com.lovely.bear.laboratory.https

import android.util.Base64
import android.util.Log
import com.lovely.bear.laboratory.MyApplication
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.*

var okHttpClient: OkHttpClient? = null

fun testOkSSL(ok: OkHttpClient) {
    val req = Request.Builder().url("https://api2.dev.ringle.com").build()
    ok.newCall(req).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, "请求失败：$e")
        }

        override fun onResponse(call: Call, response: Response) {
            Log.d(TAG, "请求成功")
        }
    })
}

fun initOk() {
    val ssl = getSSLConfig()
    if (ssl != null) {

        okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(ssl.first, ssl.second)
            .apply {
                if (SERVER_CA_SIGNATURE.isNotBlank() && SERVER_CA_SIGNATURE_ALGORITHM == SHA256) {
                    CertificatePinner.Builder().add("**.$BASE_HOST", "SHA256/$SERVER_CA_SIGNATURE")
                        .build()
                }
            }
            .hostnameVerifier { hostname, session ->
                if ((session.peerCertificates[0] as X509Certificate).subjectDN.name.endsWith(
                        RINGLE_DN
                    )
                ) {
                    true
                } else {
                    val hv = HttpsURLConnection.getDefaultHostnameVerifier()
                    hv.verify(hostname, session)
                }
            }
            .build()
    }
}

private fun getSSLConfig(): Pair<SSLSocketFactory, X509TrustManager>? {
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm()
    )
    val serverKS = getServerKeyStore()
    if (serverKS == null) {
        Log.e(TAG, "服务端KeyStore初始化异常")
        return null
    }
    trustManagerFactory.init(getServerKeyStore())
    val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
    if (trustManagers.size != 1
//              || trustManagers[0] !is javax.net.ssl.X509TrustManager
    ) {
        Log.e(
            TAG, ("Unexpected default trust managers:"
                    + trustManagers.contentToString())
        )
        return null
    }

    val trustManager: X509TrustManager = trustManagers[0] as X509TrustManager

    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    val clientKS = getClientKeyStore()
    if (clientKS == null) {
        Log.e(TAG, "客户端KeyStore初始化异常")
        return null
    }
    kmf.init(clientKS, PASSWROD.toCharArray())

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(kmf.keyManagers, arrayOf<TrustManager>(trustManager), null)
    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
    return Pair(sslSocketFactory, trustManager)
}

private var SERVER_CA_SIGNATURE: String = ""
private var SERVER_CA_SIGNATURE_ALGORITHM: String = ""
private var SHA256 = "SHA256"
private val BASE_HOST = "ringle.com"

/**
 * 准备服务端证书
 */
private fun getServerKeyStore(): KeyStore? {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null)
    arrayOf(SERVER_CA_CRT_FILE_PATH, SERVER_CRT_FILE_PATH).map {
        val io = getAssetsFio(it) ?: return null
        val cert = certificateFactory.generateCertificate(io)
        if (it == SERVER_CA_CRT_FILE_PATH) {
            if (cert is X509Certificate) {
                SERVER_CA_SIGNATURE = cert.signature.toString()
                if (cert.sigAlgName.contains("sha256With"))
                    SERVER_CA_SIGNATURE_ALGORITHM = SHA256
            }
        }
        Log.d(TAG, "证书$it \n${cert.toString()}")
        keyStore.setCertificateEntry(it, cert)
    }

    return keyStore
}

/**
 * 准备客户端证书
 */
private fun getClientKeyStore(): KeyStore? {
    val certificateFactory = CertificateFactory.getInstance("X.509")

    // 创建一个JKS类型的KeyStore，存储我们信任的证书
    //todo 测试一下android 是否支持jks格式，不可以再改成BKS
    val clientKeyStore = KeyStore.getInstance("BKS")
    clientKeyStore.load(getAssetsFio("tls/client1.bks"), "ringle".toCharArray())

    return clientKeyStore

    val protParam: KeyStore.ProtectionParameter =
        KeyStore.PasswordProtection(PASSWROD.toCharArray())

    /*添加客户端证书*/
    // 证书工厂根据证书文件的流生成证书 cert
    val cert =
        certificateFactory.generateCertificate(getAssetsFio(CLIENT_CRT_FILE_PATH))
    clientKeyStore.setCertificateEntry("client", cert)

    //添加客户端私钥
    /**
     * PEM files consist of a header, body and footer as ASCII characters with the body being the Base64 encoded content of the DER file. You can convert PEM to DER in two obvious ways -

    1) Use openssl to convert the PEM to DER using something like
    openssl rsa -inform PEM -in rsapriv.pem -outform DER -pubout -out rsapub.der
    openssl pkcs8 -topk8 -inform PEM -in rsapriv.pem -outform DER -nocrypt -out rsapriv.der
    Check the openssl 'man page' for further details.
    or
    2) Within your Java, strip the header and footer and then Base64 decode the body before using the body to create the keys.
     */

    val keyData: ByteArray = getAssetsFio(CLIENT_CRT_KEY_FILE_PATH)?.readBytes() ?: return null
    val keyString = String(keyData).replace("-----BEGIN PRIVATE KEY-----\n", "")
        .replace("\n-----END PRIVATE KEY-----\n", "")
    //Log.d(TAG, "私钥解码前文案：$keyString")

    val actKeyString = Base64.decode(keyString, Base64.DEFAULT)
    val privateKey = getPrivateKey(actKeyString)
    if (privateKey == null) {
        Log.e(TAG, "私钥读取失败")
        return null
    }
    val crtPrivateKey = KeyStore.PrivateKeyEntry(privateKey, arrayOf(cert))
    clientKeyStore.setEntry("client", crtPrivateKey, protParam)

    return clientKeyStore
}

private const val TAG = "SSL_CONFIG"
private const val PASSWROD = "PASSWROD"
private const val CLIENT_CRT_FILE_PATH = "tls/client1.crt"
private const val CLIENT_CRT_KEY_FILE_PATH = "tls/client1.key"
private const val SERVER_CRT_FILE_PATH = "tls/server.crt"
private const val SERVER_CA_CRT_FILE_PATH = "tls/ca.crt"
private const val RINGLE_DN = "api2.dev.ringle.com"

private fun getPrivateKey(data: ByteArray): PrivateKey? {
    val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
    val keySpec = PKCS8EncodedKeySpec(data)
    val privateKey = keyFactory.generatePrivate(keySpec)

    if (privateKey == null) {
        Log.e(TAG, "私钥读取失败")
    }

    return privateKey
}

private fun getAssetsFio(path: String): InputStream? {
    return try {
        MyApplication.APP.assets.open(path)
    } catch (e: IOException) {
        Log.e(TAG, "读取 assets 流失败，path:$path")
        null
    }
}
