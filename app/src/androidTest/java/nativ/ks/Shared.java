package nativ.ks;

import android.content.Context;
import android.content.res.AssetManager;
import android.security.KeyPairGeneratorSpec;
import android.support.test.InstrumentationRegistry;
import android.util.Base64;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

/**
 * Created by wenbin.liu on 2018/12/16
 *
 * @author wenbin.liu
 */
public class Shared {

    private Context mContext;

    private boolean saveSignToDisk = false;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getContext();
    }

    private void SignTest(KeyStore.Entry entry) throws Exception{
        final String dataToSign = "this is plainText.";
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(((KeyStore.PrivateKeyEntry)entry).getPrivateKey());
        signature.update(dataToSign.getBytes());
        byte[] sign = signature.sign();
        System.out.println("生成的数字签名是 = \n" + new String(Base64.encode(sign, Base64.DEFAULT)));
        if (saveSignToDisk){
            File file = new File("/sdcard/sign.asn");
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(new String(Base64.encode(sign, Base64.DEFAULT)).getBytes());
            fos.flush();
            fos.close();
        }

        Signature verify = Signature.getInstance("SHA1withRSA");
        verify.initVerify(((KeyStore.PrivateKeyEntry)entry).getCertificate());
        verify.update(dataToSign.getBytes());
        boolean succ = verify.verify(sign);
        System.out.println("数字签名验证结果 = " + succ);
    }

    private void exportPfxPrivateKey() throws Exception {
        System.out.println("\n常规的PFX密钥库材料导出测试：");
        final String pwd = "111111";
        final String pfxPath = "/sdcard/filecert.pfx";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(pfxPath), pwd.toCharArray());

        String defaultAlias = "";
        Enumeration<String> aliass = keyStore.aliases();
        while (aliass.hasMoreElements()) {
            defaultAlias = aliass.nextElement();
            System.out.println("find alias = " + defaultAlias);
        }

        Key key = keyStore.getKey(defaultAlias, "123456".toCharArray());
        System.out.println("key = " + key);
        if (key.getEncoded() != null && key.getEncoded().length > 0){
            System.out.println("导出的私钥材料 = \n" + new String(Base64.encode(key.getEncoded(), Base64.DEFAULT)));
        } else {
            System.out.println("导出的私钥材料为空！");
        }
        Certificate certificate = keyStore.getCertificate(defaultAlias);
        System.out.println("certificate = " + certificate);

        SignTest(keyStore.getEntry(defaultAlias, null));
//        mKeyStore.setKeyEntry(pfxAlias, key,null,new Certificate[]{certificate});
    }

    private void exportKeyStorePrivateKey() throws Exception{
        System.out.println("\nAndroidKeyStore 密钥库材料导出测试:");
        KeyStore mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        mKeyStore.load(null);

        final String alias = "MY_ALIAS";

        if (!mKeyStore.containsAlias(alias)){
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA","AndroidKeyStore");
            Calendar startDate = Calendar.getInstance();
            Calendar enDate = Calendar.getInstance();
            enDate.add(Calendar.YEAR, 20);
            KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(mContext)
                    .setAlias(alias)
                    .setSerialNumber(BigInteger.ONE)
                    .setSubject(new X500Principal("CN=Android CA Certificate"))
                    .setStartDate(startDate.getTime())
                    .setEndDate(enDate.getTime());

            keyPairGenerator.initialize(builder.build());
            keyPairGenerator.generateKeyPair();
        }else {
            System.out.println("已经找到密钥对，不再生成!");
        }

        Assert.assertTrue(mKeyStore.containsAlias(alias));
        Key key = mKeyStore.getKey(alias, null);
        if (key.getEncoded() != null && key.getEncoded().length >0){
            System.out.println("导出的私钥材料 = \n" + new String(Base64.encode(key.getEncoded(), Base64.DEFAULT)));
        } else {
            System.out.println("导出的私钥材料为空！");
        }
        System.out.println("key = " + key);
        Certificate certificate = mKeyStore.getCertificate(alias);
        System.out.println("certificate = " + certificate);

        SignTest(mKeyStore.getEntry(alias, null));
    }

    // 1. "密钥材料永不进入应用进程"
    @Test
    public void keyNotExportedToREE_Test() throws Exception{


        exportPfxPrivateKey();
        exportKeyStorePrivateKey();
    }

    // 2. "临时唯一性，不可前向破解"
    @Test
    public void decryptFailedOnSecondInstall_Test() throws Exception{
        saveSignToDisk = true;
        exportKeyStorePrivateKey();
    }

    @Test
    public void decrypt_Test() throws Exception{
        FileInputStream fis = new FileInputStream("/sdcard/sign.asn");
        final int available = fis.available();
        byte[] buffer = new byte[available];
        int len = fis.read(buffer);
        Assert.assertEquals(available, len);

        final String dataToSign = "this is plainText.";
        final String alias = "MY_ALIAS";
        Signature verify = Signature.getInstance("SHA1withRSA");
        KeyStore mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        mKeyStore.load(null);
        if (!mKeyStore.containsAlias(alias)){
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA","AndroidKeyStore");
            Calendar startDate = Calendar.getInstance();
            Calendar enDate = Calendar.getInstance();
            enDate.add(Calendar.YEAR, 20);
            KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(mContext)
                    .setAlias(alias)
                    .setSerialNumber(BigInteger.ONE)
                    .setSubject(new X500Principal("CN=Android CA Certificate"))
                    .setStartDate(startDate.getTime())
                    .setEndDate(enDate.getTime());

            keyPairGenerator.initialize(builder.build());
            keyPairGenerator.generateKeyPair();
        }else {
            System.out.println("已经找到密钥对，不再生成!");
        }
        Assert.assertTrue(mKeyStore.containsAlias(alias));

        verify.initVerify(mKeyStore.getCertificate(alias));
        verify.update(dataToSign.getBytes());
        boolean suc = verify.verify(Base64.decode(buffer, Base64.DEFAULT));
        System.out.println("解密结果 = " + suc);
    }

    // 3. "KeyStore中的私钥进程隔离"
    @Test
    public void appSeparated_Test() throws Exception {
        KeyStore mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        mKeyStore.load(null);
        Enumeration<String> enumeration = mKeyStore.aliases();
        while (enumeration.hasMoreElements()){
            String alias = enumeration.nextElement();
            System.out.println("alias = " + alias);
        }
    }
}
