package com.m.sofiane.go4lunch.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.m.sofiane.go4lunch.R;
import com.m.sofiane.go4lunch.fragment.FavoriteFragment;
import com.m.sofiane.go4lunch.fragment.ListFragment;
import com.m.sofiane.go4lunch.fragment.MapFragment;
import com.m.sofiane.go4lunch.fragment.MyChoiceFragment;
import com.m.sofiane.go4lunch.fragment.SettingsFragment;
import com.m.sofiane.go4lunch.fragment.WorkFragment;
import com.m.sofiane.go4lunch.models.pojoMaps.Result;
import com.m.sofiane.go4lunch.services.GoogleInterface;
import com.m.sofiane.go4lunch.services.LatAndLngSingleton;
import com.m.sofiane.go4lunch.services.Singleton;
import com.m.sofiane.go4lunch.utils.MyChoiceHelper;
import com.m.sofiane.go4lunch.utils.MyFavoriteHelper;
import com.m.sofiane.go4lunch.utils.MyUserHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.m.sofiane.go4lunch.R.string.navigation_drawer_close;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    public static final String LANG = "language";
    final Fragment mapFragment = new MapFragment();
    final Fragment listFragment = new ListFragment();
    final Fragment workFragment = new WorkFragment();
    final Fragment mFavFrag = new FavoriteFragment();
    final FragmentManager fm = getSupportFragmentManager();
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.activity_main_bottom_navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.activity_main_drawer_isOpen)
    NavigationView mNavigationView;
    @BindView(R.id.constraitForCont)
    View mdeco;
    Location gps_loc, network_loc, final_loc;
    double longitude, latitude;
    HeaderViewHolder mHeaderViewHolder;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findLocation();
        testLanguage();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        InitToolBar(false);
        InitBottomNav(false);
        InitDrawerLayout();
        createFireStoreUser();
        loadOpenFragement();

    }

    private void loadOpenFragement() {
        loadFragment(mapFragment);
        mdeco.setBackgroundResource(R.drawable.gradientmap);
    }

    private void findLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            gps_loc = Objects.requireNonNull(locationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gps_loc != null || network_loc != null) {
            final_loc = gps_loc;
            latitude = Objects.requireNonNull(final_loc).getLatitude();
            longitude = final_loc.getLongitude();
        } else {
            latitude = 0.0;
            longitude = 0.0;
        }

        LatAndLngSingleton.getInstance().setLatitude(latitude);
        LatAndLngSingleton.getInstance().setLongitude(longitude);

        build_retrofit_and_get_response(latitude, longitude);

    }

    private void InitDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        mDrawerLayout.onCheckIsTextEditor();
        toggle.syncState();
        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            if (mToolbar.getChildAt(i) instanceof ImageButton) {
                mToolbar.getChildAt(i).setScaleX(1f);
                mToolbar.getChildAt(i).setScaleY(1f);
            }
        }
        loadUserProfil();
        initLogout();
        initFavDrawer();
        initSettingsDrawer();
        initMyChoiceDrawer();
    }

    public void InitBottomNav(boolean isHidden) {
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mBottomNavigationView.setVisibility(isHidden ? View.GONE : View.VISIBLE);
    }

    public void InitToolBar(boolean isHidden) {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.map:
                loadFragment(mapFragment);
                mdeco.setBackgroundResource(R.drawable.gradientmap);
                break;

            case R.id.list:
                loadFragment(listFragment);
                mdeco.setBackgroundResource(R.drawable.gradientlist);
                break;

            case R.id.group:
                loadFragment(workFragment);
                mdeco.setBackgroundResource(R.drawable.gradientfull);
                break;

        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public void loadUserProfil() {
        View header = mNavigationView.getHeaderView(0);
        mHeaderViewHolder = new HeaderViewHolder(header);
        mHeaderViewHolder.navUsername.setText(MyUserHelper.getProfilName());
        mHeaderViewHolder.navUserMail.setText(MyUserHelper.getProfilEmail());
        Glide.with(this)
                .load((MyUserHelper.getProfilPhoto()))
                .apply(RequestOptions.circleCropTransform())
                .into(mHeaderViewHolder.navProfilPic);
    }

    private void initSettingsDrawer() {
        mNavigationView.getMenu().findItem(R.id.drawer_settings).setOnMenuItemClickListener(menuItem -> {
            SettingsFragment editNameDialog = new SettingsFragment();
            editNameDialog.show(fm, "fragment_settings");
            mDrawerLayout.closeDrawers();
            return false;
        });
    }

    private void initMyChoiceDrawer() {
        mNavigationView.getMenu().findItem(R.id.drawer_lunch).setOnMenuItemClickListener(menuItem -> {
            MyChoiceFragment editNameDialog = new MyChoiceFragment();
            editNameDialog.show(fm, "fragment_settings");
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initFavDrawer() {
        mNavigationView.getMenu().findItem(R.id.drawer_fav).setOnMenuItemClickListener(menuItem -> {
            fm.beginTransaction().replace(R.id.fragment_container, mFavFrag).commit();
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initLogout() {
        mNavigationView.getMenu().findItem(R.id.drawer_logout).setOnMenuItemClickListener(menuItem -> {
            AuthUI.getInstance()
                    .signOut(MainActivity.this)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Test3 ", Toast.LENGTH_LONG).show());

            return true;
        });
    }

    public void createFireStoreUser() {
        Map<String, Object> mDataMap = new HashMap<>();
        mDataMap.put("NameUser", MyUserHelper.getProfilName());
        mDataMap.put("PhotoUser", MyUserHelper.getProfilPhoto());

        MyUserHelper.createMyUser().set(mDataMap);

        MyChoiceHelper.readMyChoice()
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (Objects.requireNonNull(document).exists()) {
                } else {
                    MyChoiceHelper.initMyChoice();
                    MyFavoriteHelper.getMyFavoriteCollection();
                }
            }
        });
    }

    private void build_retrofit_and_get_response(double latitude, double longitude) {
        String url = "https://maps.googleapis.com/maps/";
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        GoogleInterface service = retrofit.create(GoogleInterface.class);

        Call<Result> call = service.getNearbyPlaces((latitude + "," + longitude), 200, "restaurant");

        call.enqueue(new Callback<Result>() {
            @SuppressLint({"RestrictedApi", "LongLogTag"})
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                ArrayList<Result> mArrayList;
                mArrayList = Objects.requireNonNull(response.body()).getList();
                Singleton.getInstance().setArrayList(mArrayList);
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
            }
        });
    }

    public void testLanguage() {
        mSharedPreferences = Objects.requireNonNull(getSharedPreferences(LANG, Context.MODE_PRIVATE));
        String mLang = mSharedPreferences.getString(LANG, "en");

        if (mLang.equals("en")) {
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = new Locale("en");
            res.updateConfiguration(conf, dm);
        } else {
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = new Locale("fr");

            res.updateConfiguration(conf, dm);
        }
    }

    protected static class HeaderViewHolder {
        @BindView(R.id.profiltextnameBis)
        TextView navUsername;
        @BindView(R.id.profiltextmailBis)
        TextView navUserMail;
        @BindView(R.id.profilimage)
        ImageView navProfilPic;

        HeaderViewHolder(View headerView) {
            ButterKnife.bind(this, headerView);
        }
    }
}