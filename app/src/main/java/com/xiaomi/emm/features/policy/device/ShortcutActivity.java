package com.xiaomi.emm.features.policy.device;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.xiaomi.emm.R;
import com.xiaomi.emm.utils.ActivityCollector;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import java.util.regex.Pattern;

/**
 * 通过指定多个Launcher入口实现
 *
 *
 *@author
 * @date
 */
public class ShortcutActivity extends AppCompatActivity {

    private static final String TAG = "ShortcutActivity";
    private static Pattern  pattern = Pattern.compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
    private static final String[] ext = {
            "top", "com.cn", "com", "net", "org", "edu", "gov", "int", "mil", "cn", "tel", "biz", "cc", "tv", "info",
            "name", "hk", "mobi", "asia", "cd", "travel", "pro", "museum", "coop", "aero", "ad", "ae", "af",
            "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "as", "at", "au", "aw", "az", "ba", "bb", "bd",
            "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz",
            "ca", "cc", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cq", "cr", "cu", "cv", "cx",
            "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "eh", "es", "et", "ev", "fi",
            "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gh", "gi", "gl", "gm", "gn", "gp",
            "gr", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "in", "io",
            "iq", "ir", "is", "it", "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw",
            "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md",
            "mg", "mh", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mv", "mw", "mx", "my", "mz",
            "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", "np", "nr", "nt", "nu", "nz", "om", "qa", "pa",
            "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pt", "pw", "py", "re", "ro", "ru", "rw",
            "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st",
            "su", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tm", "tn", "to", "tp", "tr", "tt",
            "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "va", "vc", "ve", "vg", "vn", "vu", "wf", "ws",
            "ye", "yu", "za", "zm", "zr", "zw"
    };
    private static  Pattern WEB_URL;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ext.length; i++) {
            sb.append(ext[i]);
            sb.append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        // final pattern str
        String pattern = "((https?|s?ftp|irc[6s]?|git|afp|telnet|smb)://)?((\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|((www\\.|[a-zA-Z\\.\\-]+\\.)?[a-zA-Z0-9\\-]+\\." + sb.toString() + "(:[0-9]{1,5})?))((/[a-zA-Z0-9\\./,;\\?'\\+&%\\$#=~_\\-]*)|([^\\u4e00-\\u9fa5\\s0-9a-zA-Z\\./,;\\?'\\+&%\\$#=~_\\-]*))";
        WEB_URL = Pattern.compile(pattern);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcut_main);
        WebView webview = (WebView)findViewById(R.id.webview);
        WebSettings webSettings=webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        Intent intent = getIntent();
        if (intent != null) {
            String url = intent.getStringExtra("url");
            if (pattern != null) {
                LogUtil.writeToFile(TAG,"===="+url);

                Log.w(TAG,"pattern=="+pattern.matcher(url).matches());
                Log.w(TAG,"WEB_URL=="+WEB_URL.matcher(url).matches());
            /*(url.contains("http://") || url.contains("https://") || url.contains("www."))*/
                if (! TextUtils.isEmpty( url ) && pattern.matcher(url).matches()) {
                    Log.w(TAG,""+url);
                    Uri content_url = Uri.parse(url);
                    //startActivity( new Intent(Intent.ACTION_VIEW, content_url));
                    webview.loadUrl(url);
                }else {
                    Log.w(TAG,"----"+url+"不是网址");
                    Toast.makeText(this,"不是正确的网址，格式是类似这种 http://www.baidu.com",Toast.LENGTH_SHORT).show();
                }
            }else {
                if (! TextUtils.isEmpty( url )){
                    Uri content_url = Uri.parse(url);
                    //startActivity( new Intent(Intent.ACTION_VIEW, content_url));
                    webview.loadUrl(url);
                }else {
                    Toast.makeText(this,"url 为空 ",Toast.LENGTH_SHORT).show();
                }
            }

        }

        boolean intentToMainState = PreferencesManager.getSingleInstance().getLockFlag("intentMainAciticity");
        if (intentToMainState) {
            PreferencesManager.getSingleInstance().setLockFlag("unLockScreen",true);
            PreferencesManager.getSingleInstance().setLockFlag("intentMainAciticity",false);
        }else {
            ActivityCollector.removeAllActivity();
        }
       // finish();
    }

}
