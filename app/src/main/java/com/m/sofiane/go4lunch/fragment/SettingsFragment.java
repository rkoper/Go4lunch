package com.m.sofiane.go4lunch.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.work.WorkManager;

import com.m.sofiane.go4lunch.R;
import com.m.sofiane.go4lunch.activity.mainactivity;
import com.m.sofiane.go4lunch.services.notificationService;

import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.*;

/**
 * created by Sofiane M. 23/04/2020
 */

public class SettingsFragment extends DialogFragment {

    @BindView(R.id.check_notif)
    CheckBox mCheckNotif;
    @BindView(R.id.check_lang)
    CheckBox mCheckLang;
    @BindView(R.id.lfrench)
    LinearLayout mLenglish;
    @BindView(R.id.lenglish)
    LinearLayout mLfrench;
    @BindView(R.id.lnotif)
    LinearLayout mLactiveNotif;
    @BindView(R.id.ltime_p)
    LinearLayout mLTimePicker;
    @BindView(R.id.switch_notif)
    Switch mSwNotif;
    @BindView(R.id.widget_time_picker)
    TimePicker mWidgetTimePicker;
    @BindView(R.id.button_ok)
    ImageButton mButtonOK;
    @BindView(R.id.button_close)
    LinearLayout mButton_Close;
    @BindView(R.id.switch_lang_fr)
    Switch mSwitchFrench;
    @BindView(R.id.switch_lang_en)
    Switch mSwitchEnglish;

    Boolean mStatNotif;

    Animation mASD;
    Animation mASU;
    Animation mAup;
    Animation mASD2;
    Animation mASU2;
    Animation mASD3;
    Animation mASU3;

    SharedPreferences mSharedPreferences;
    public static final String PREFS = "666";
    public static final String TIMETONOTIF = "TimeToNotif";
    public static final String STATNOTIF = "StateNotif";
    public static final String LANG = "language";

    public SettingsFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_test, null);
        initAnimation();
        ButterKnife.bind(this, view);

        Window window = Objects.requireNonNull(getDialog()).getWindow();
        Objects.requireNonNull(window).setBackgroundDrawableResource(android.R.color.transparent);

        initLang();
        initNotif();
        initCloseButton();

        return view;
    }

    private void initAnimation() {
        mASD = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
        mASU = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        mASD2 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down2);
        mASU2 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up2);
        mASD3 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down3);
        mASU3 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up3);
        mAup = AnimationUtils.loadAnimation(getContext(), R.anim.inbottom);
    }

    private void initCloseButton() {
        mButton_Close.setOnClickListener(v ->
                onDestroyView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void initLang() {
        mCheckLang.setOnClickListener(v -> {
            if (mCheckLang.isChecked()) {
                mLenglish.startAnimation(mASU);
                mLfrench.startAnimation(mASU);
                chooseYourLanguage();
                Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
            } else {
                mLenglish.startAnimation(mASD);
                mLfrench.startAnimation(mASD);
                Toast.makeText(getContext(), "2", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseYourLanguage() {
        mSharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(LANG, Context.MODE_PRIVATE);
        String mLang = mSharedPreferences.getString(LANG, "en");
        if (mLang.equals("en")) {
            mSwitchEnglish.setChecked(true);
        } else {
            mSwitchFrench.setChecked(true);
        }

        mSwitchFrench.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mSharedPreferences.edit().putString(LANG, "fr").apply();
                mSwitchEnglish.setChecked(false);
                Intent refresh = new Intent(getContext(), mainactivity.class);
                startActivity(refresh);
            }
        });
        mSwitchEnglish.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                mSharedPreferences.edit().putString(LANG, "en").apply();
                mSwitchFrench.setChecked(false);
                Intent refresh = new Intent(getContext(), mainactivity.class);
                startActivity(refresh);

            }
        }));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initNotif() {
        mCheckNotif.setOnClickListener(v -> {
            if (mCheckNotif.isChecked()) {

                mLactiveNotif.startAnimation(mASU2);
                LoadNotifSwitch();
              //  initSwitchO();
            }
            else
                { mLactiveNotif.startAnimation(mASD2);
            }
        });
    }

    private void LoadNotifSwitch() {
        mSharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        mStatNotif = mSharedPreferences.getBoolean(STATNOTIF, false);
        if (mStatNotif.equals(true)) {
            mSwNotif.setChecked(true);
            mLTimePicker.startAnimation(mASU3);
        } else {mSwNotif.setChecked(false);}
        mSwNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (mSwNotif.isChecked()) {
            initPicker();
            mLTimePicker.startAnimation(mASU3);
            mCheckNotif.setEnabled(false); }
        else {
            WorkManager.getInstance().cancelAllWork();
            mSharedPreferences
                    .edit()
                    .putBoolean(STATNOTIF, false)
                    .apply();
            mLTimePicker.startAnimation(mASD3);
            mButtonOK.setVisibility(GONE);
            mCheckNotif.setEnabled(true); }});

        Log.e("Shared P State ------>", mStatNotif.toString());
    }

        private void initPicker() {
                mWidgetTimePicker.is24HourView();
                mWidgetTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);
                    mButtonOK.setVisibility(VISIBLE);
                    mButtonOK.startAnimation(mAup);
                    initButtonOK(cal);
                });
            }

    private void initButtonOK(Calendar cal) {
        mButtonOK.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setAlarm(cal);
            }
            onDestroyView();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setAlarm( Calendar cal) {
        Intent notificationIntent = new Intent(getActivity(), notificationService.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(getActivity(), 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getActivity()).getSystemService(Context.ALARM_SERVICE);

        Objects.requireNonNull(alarmManager).setRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, broadcast);

        mSharedPreferences
                .edit()
                .putBoolean(STATNOTIF, true)
                .putLong(TIMETONOTIF, (cal.getTimeInMillis()))
                .apply();
    }

}





/*

    @BindView(R.id.e1_english)
    LinearLayout mLayoutSwitchEnglish;
    @BindView(R.id.fourL_switchEN)
    Switch mSwitchEnglish;
    @BindView(R.id.e1_french)
    LinearLayout mLayoutSwitchFrench;
    @BindView(R.id.fourL_switchFR)
    Switch mSwitchFrench;
    @BindView(R.id.e2_language)
    LinearLayout mLayoutLanguage;
    @BindView(R.id.e3_notification)
    LinearLayout mLayoutNotif;
    @BindView(R.id.e4_switch_notif)
    LinearLayout mLayoutSwitchNot;
    @BindView(R.id.three_switchNotif)
    Switch mSwitchNotif;
    @BindView(R.id.e5_time_picker)
    LinearLayout mLayoutTimePicker;
    @BindView(R.id.e5_widget_time_picker)
    TimePicker mWidgetTimePicker;
    @BindView(R.id.e6_ok)
    LinearLayout mLayoutOk;
    @BindView(R.id.e6_button_ok)
    ImageButton mButtonSwitchOk;
    @BindView(R.id.button_close)
    ImageButton mButtonClose;
    @BindView(R.id.Layout)
    RelativeLayout mLayoutG;

    public SettingsFragment() {}


    private int h;
    private int m;

    Resources mTest;
    Boolean mStatNotif;
    SharedPreferences mSharedPreferences;
    public static final String PREFS ="666";
    public static final String TIMETONOTIF= "TimeToNotif";
    public static final String STATNOTIF= "StateNotif";
    public static final String LANG = "language";

    Animation mAnimLeftRight,mAnimFadeOut,mAnimZoomOut,mAnimSimple,mAnimBottom;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, null);
        initAnim();
        ButterKnife.bind(this, view);
        initCloseButton();
        initWindowsTransprent();
        clickonNotif();
        clickonLangauge();
        return view;
    }

    private void initAnim() {
        mAnimLeftRight  = AnimationUtils.loadAnimation(getContext(),R.anim.lefttoright);
        mAnimFadeOut  = AnimationUtils.loadAnimation(getContext(),R.anim.fadeout);
        mAnimZoomOut  = AnimationUtils.loadAnimation(getContext(),R.anim.zoomout);
        mAnimSimple = AnimationUtils.loadAnimation(getContext(),R.anim.sample_anim);
        mAnimBottom = AnimationUtils.loadAnimation(getContext(),R.anim.inbottom);
    }

    private void clickonLangauge() {
        mLayoutLanguage.setOnClickListener(v ->
                displayLang());
    }

    private void displayLang(){
        invisibleNotif();
        chooseYourLanguage();
    }

    @SuppressLint("ResourceAsColor")
    public void invisibleNotif(){
        mLayoutSwitchFrench.startAnimation(mAnimLeftRight);
        mLayoutSwitchFrench.setVisibility(View.VISIBLE);
        mLayoutSwitchEnglish.startAnimation(mAnimLeftRight);
        mLayoutSwitchEnglish.setVisibility(View.VISIBLE);

        mLayoutOk.setVisibility(View.INVISIBLE);
        mLayoutTimePicker.setVisibility(View.INVISIBLE);
        mLayoutSwitchNot.setVisibility(View.INVISIBLE);
    }

    private void chooseYourLanguage() {
        mSharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(LANG ,Context.MODE_PRIVATE);
        String mLang  = mSharedPreferences.getString(LANG,"en");
        if (mLang.equals("en"))
        {mSwitchEnglish.setChecked(true);}
        else {mSwitchFrench.setChecked(true);}

        mSwitchFrench.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mSharedPreferences.edit().putString(LANG, "fr").apply();
                mSwitchEnglish.setChecked(false);
                Intent refresh = new Intent(getContext(), mainactivity.class);
                startActivity(refresh);
            }
        });
        mSwitchEnglish.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                mSharedPreferences.edit().putString(LANG, "en").apply();
                mSwitchFrench.setChecked(false);
                Intent refresh = new Intent(getContext(), mainactivity.class);
                startActivity(refresh);

            }
        }));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void clickonNotif() {
        mLayoutNotif.setOnClickListener(v ->
                displayNotif());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayNotif(){
        invisibleLang();
        checkStateSwitch();
        initSwitch();
    }

    public void invisibleLang(){
        mLayoutSwitchNot.setVisibility(View.VISIBLE);
        mLayoutSwitchEnglish.setVisibility(View.INVISIBLE);
        mLayoutSwitchFrench.setVisibility(View.INVISIBLE);
    }

    private void initWindowsTransprent() {
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        Objects.requireNonNull(window).setBackgroundDrawableResource(android.R.color.transparent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void mASD4StateSwitch() {
        mSharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS ,Context.MODE_PRIVATE);
        Boolean mStatNotif  = mSharedPreferences.getBoolean(STATNOTIF,false);
        if (mStatNotif.equals(true))
        {mSwitchNotif.setChecked(true);
            mLayoutTimePicker.setVisibility(View.VISIBLE);
            initPicker();}
        Log.e("Shared P State ------>", mStatNotif.toString());
    }

    @SuppressLint("ResourceAsColor")
    public void initSwitch() {
        mSwitchNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mLayoutTimePicker.setVisibility(View.VISIBLE);
                    initPicker();
                }
                else {    mLayoutTimePicker.startAnimation(mAnimSimple);

                    mLayoutTimePicker.setVisibility(View.VISIBLE);
                }
            } else {
                WorkManager.getInstance().cancelAllWork();
                Toast.makeText(getActivity(), "OFF", Toast.LENGTH_SHORT).show();
                mSharedPreferences
                        .edit()
                        .putBoolean(STATNOTIF, false)
                        .remove(TIMETONOTIF)
                        .apply();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ResourceAsColor")
    private void initPicker() {
        mWidgetTimePicker.is24HourView();
        mWidgetTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            initButton(cal);
        });
    }

    private void initButton(Calendar calendar) {
        mLayoutOk.startAnimation(mAnimBottom);
        mLayoutOk.setVisibility(View.VISIBLE);
        mButtonSwitchOk.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setAlarm(calendar);
            }
            onDestroyView();
        });
    }

    private void initCloseButton() {
        mButtonClose.setOnClickListener(v ->
                onDestroyView());
    }

    @Override
    public void onDestroyView() {
        mLayoutG.startAnimation(mAnimZoomOut);
        super.onDestroyView();
    }



}



*/
