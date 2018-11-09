package com.zoomtech.emm.view.activity;

import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/10.
 */

public class AboutActivity extends BaseActivity {

    WebView about_text;
    String about_str = null;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initData() {
        String version = null;
        version = AppUtils.getAppVersionName(this, Common.packageName);

        about_str = "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <title></title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <style>\n" +
                "      .content{\n" +
                "        margin-top: 40px;\n" +
                "        width: 100%;\n" +
                "      }\n" +
                "      .content>span{\n" +
                "        width: 100%;\n" +
                "        display: inline-block;\n" +
                "        font-size: 40px;\n" +
                "        text-align: center;\n" +
                "        color: blue;\n" +
                "        margin-bottom: 10px;\n" +
                "      }\n" +
                "      .content>h4{\n" +
                "        width: 100%;\n" +
                "        text-align: center;\n" +
                "        font-weight: 200;\n" +
                "      }\n" +
                "      .content>p{\n" +
                "        width: 100%;\n" +
                "        text-align: center;\n" +
                "        margin: 30px 0;\n" +
                "        font-size: 14px;\n" +
                "      }\n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "  <div class=\"content\">\n" +
                "    <span class=\"icon-mobile\"></span>\n" +
                "    <h4>" + getResources().getString(R.string.emm_name) + "</h4>\n" +
                "    <p>" + getResources().getString(R.string.device_name, Build.MODEL ) + "</p>\n" +
                "    <p>" + getResources().getString(R.string.softwares_model,  Common.packageName.split( "\\." )[1] ) + "</p>\n" +
                "    <p>" + getResources().getString(R.string.version_number, version ) +  " </p>\n" +
                "  </div>\n" +
                "  </body>\n" +
                "</html>";
        about_text.loadDataWithBaseURL( "",about_str, "text/html", "UTF-8",null );
    }

    @Override
    protected void initView() {

        Toolbar toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        about_text = mViewHolder.get(R.id.about_text);
    }
}
