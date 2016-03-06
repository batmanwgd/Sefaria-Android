package org.sefaria.sefaria.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.sefaria.sefaria.BuildConfig;
import org.sefaria.sefaria.MenuElements.MenuNode;
import org.sefaria.sefaria.MyApp;
import org.sefaria.sefaria.R;
import org.sefaria.sefaria.Settings;
import org.sefaria.sefaria.Util;
import org.sefaria.sefaria.database.Database;
import org.sefaria.sefaria.database.Downloader;
import org.sefaria.sefaria.layouts.CustomActionbar;

import java.util.Set;

public class SettingsActivity extends Activity {

    private final int TOT_NUM_DEBUG_DB_CLICKS = 7;
    private int numDebugDBUnlockClicks;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        numDebugDBUnlockClicks = 0;

        CustomActionbar customActionbar = new CustomActionbar(this, new MenuNode("Settings","Settings (he)", null), Settings.getSystemLang(),homeClick,homeLongClick,null,null,null,null,backClick,-1);
        LinearLayout abRoot = (LinearLayout) findViewById(R.id.actionbarRoot);
        abRoot.addView(customActionbar);

        RadioButton defaultTextButton;
        Util.Lang defaultTextLang = Settings.getDefaultTextLang();
        switch (defaultTextLang){
            case BI:
                defaultTextButton = (RadioButton) findViewById(R.id.text_bilingual);
                break;
            case HE:
                defaultTextButton = (RadioButton) findViewById(R.id.text_hebrew);
                break;
            default://case EN:
                defaultTextButton = (RadioButton) findViewById(R.id.text_english);
                break;
        }
        defaultTextButton.setChecked(true);

        RadioButton menuLangButton;
        Util.Lang menuLang = Settings.getMenuLang();
        switch (menuLang){
            case HE:
                menuLangButton = (RadioButton) findViewById(R.id.menu_hebrew);
                break;
            default: //case EN:
                menuLangButton = (RadioButton) findViewById(R.id.menu_english);
                break;
        }
        menuLangButton.setChecked(true);



        //LinearLayout gridRoot = (LinearLayout) findViewById(R.id.gridRoot);
        String debugVer = "";
        if (Settings.getIsDebug()) debugVer = "D";

        TextView appInfo = (TextView) findViewById(R.id.appInfo);
        appInfo.setText("App Version: " + BuildConfig.VERSION_NAME + debugVer);
        TextView databaseInfo = (TextView) findViewById(R.id.databaseInfo);
        databaseInfo.setText("Library Version: " + Util.convertDBnum(Database.getDBDownloadVersion()));


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    View.OnLongClickListener homeLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            MyApp.homeClick(SettingsActivity.this, true);
            return true;
        }
    };

    View.OnClickListener homeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("homeClicked",true);
            setResult(0, returnIntent);
            finish();
        }
    };

    View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    public void updateLibrary(View v){
        Toast.makeText(this, "Checking for updates", Toast.LENGTH_SHORT).show();
        Downloader.updateLibrary(this);
    }

    public void clearAllBookSettings(View v){
        Settings.BookSettings.clearAllBookSettings();
    }

    public void done(View v){
        finish();
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.text_hebrew:
                if (checked)
                    Settings.setDefaultTextLang(Util.Lang.HE);
                    break;
            case R.id.text_english:
                if (checked)
                    Settings.setDefaultTextLang(Util.Lang.EN);
                    break;
            case R.id.text_bilingual:
                if (checked)
                    Settings.setDefaultTextLang(Util.Lang.BI);
                    break;
        }
    }

    public void onMenuLangRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.menu_hebrew:
                if (checked)
                    Settings.setMenuLang(Util.Lang.HE);
                break;
            case R.id.menu_english:
                if (checked)
                    Settings.setMenuLang(Util.Lang.EN);
                break;
        }
    }

    public void debubDBUnlockClick(View view) {
        if (numDebugDBUnlockClicks >= TOT_NUM_DEBUG_DB_CLICKS) {
            numDebugDBUnlockClicks = 0;
            Settings.setIsDebug(!Settings.getIsDebug()); //toggle
            Toast.makeText(this,"DB isDebug == " + Settings.getIsDebug(),Toast.LENGTH_SHORT).show();

        } else {
            numDebugDBUnlockClicks++;
        }

    }



}
