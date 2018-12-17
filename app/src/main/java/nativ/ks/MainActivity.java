package nativ.ks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(this.getAssets().open("filecert.pfx"), "111111".toCharArray());
            Enumeration<String> enumeration = keyStore.aliases();
            while (enumeration.hasMoreElements()){
                String alias = enumeration.nextElement();
                System.out.println("alias = " + alias);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("default.Error:" + e.getMessage());
        }
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native String stringFromJNI();

    public static native int getKeyStoreInNative(String alias);
}
