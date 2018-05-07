package nativ.ks;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.security.KeyPairGeneratorSpec;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Enumeration;
import javax.security.auth.x500.X500Principal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static Context mContext;

    private static KeyStore mKeyStore;

    private static final String TAG = "test";

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
        mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        mKeyStore.load(null);
    }

    private void genKeyPairInKsTest(String alias) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA","AndroidKeyStore");

        initGeneratorWithKeyPairGeneratorSpec(keyPairGenerator,alias);

        keyPairGenerator.generateKeyPair();

        Log.v(TAG,"genKeyPairInKsTest done.");
    }

    private void importPfx2KsTest(String pfxAlias)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {

        final String pfxPath = "/sdcard/KOAL_CERT/lwb2.pfx";
        final String pwd = "123456";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File(pfxPath)), "123456".toCharArray());

        String defaultAlias = "";
        Enumeration<String>aliass = keyStore.aliases();
        while (aliass.hasMoreElements()) {
            defaultAlias = aliass.nextElement();
            Log.v(TAG,"find alias = " + defaultAlias);
        }

        Key key = keyStore.getKey(defaultAlias, "123456".toCharArray());
//        Log.v(TAG,"key:"+key);
        Certificate certificate = keyStore.getCertificate(defaultAlias);
//        Log.v(TAG,"certificate:" + certificate);

        mKeyStore.setKeyEntry(pfxAlias, key,null,new Certificate[]{certificate});
    }

    @Test
    public void useAppContext() throws Exception {

        Enumeration<String> aliasInKs = mKeyStore.aliases();
        Log.v(TAG,"Searching key in KS:");
        while (aliasInKs.hasMoreElements()){
            Log.v(TAG,aliasInKs.nextElement());
        }

        String[] alias = {"import_key_alias","orig_key_alias"};

        Log.v(TAG,"\n\n");
        for (String one:alias){

            if (!mKeyStore.containsAlias(one)){

                Log.v(TAG,String.format("starting to generate/import: [%s] in KS", one));

                if (one.equals("orig_key_alias"))
                    // 1. 在ks中产生RSA密钥对
                    genKeyPairInKsTest(one);
                else
                    // 2. 外部pfx导入到ks中
                    importPfx2KsTest(one);

            }else {
                Log.v(TAG,String.format("already find: [%s] in \"KS\"",one));
            }

            Assert.assertTrue(mKeyStore.containsAlias(one));

            /*
            *
            * 引用自“android.security.Credentials”（该类外部无法访问）中的“USER_PRIVATE_KEY”
            *
            *  对 privateKey 的操作，实际在Keychain或者KeyStore中操作传入native之前时，实际会添加相关前缀。
            *
            * */
            final String PREFIX = "USRPKEY_";

            int succ = MainActivity.getKeyStoreInNative(PREFIX + one);
            Assert.assertEquals(0,succ);

            Log.v(TAG,"done:" + one+"   \n\n");
        }
    }

    /**
     * 兼容API18以上在KeyStore中创建对应的密钥对的具体实现
     *
     * @param generator 该对象主要用于创建对应的KeyPair {@link KeyPairGenerator}
     * @param alias 在KeyStore中存储密钥对所使用的别名
     */
    @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
    private static void initGeneratorWithKeyPairGeneratorSpec(KeyPairGenerator generator, String alias) {
        Calendar startDate = Calendar.getInstance();
        Calendar enDate = Calendar.getInstance();
        enDate.add(Calendar.YEAR, 20);

        KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(mContext)
                .setAlias(alias)
                .setSerialNumber(BigInteger.ONE)
                .setSubject(new X500Principal("CN=Android CA Certificate"))
                .setStartDate(startDate.getTime())
                .setEndDate(enDate.getTime());

        try {
            generator.initialize(builder.build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

}
