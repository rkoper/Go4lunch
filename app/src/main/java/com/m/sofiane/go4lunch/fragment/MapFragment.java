package com.m.sofiane.go4lunch.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.m.sofiane.go4lunch.BuildConfig;
import com.m.sofiane.go4lunch.R;
import com.m.sofiane.go4lunch.activity.SubActivity;
import com.m.sofiane.go4lunch.adapter.SearchMapAdapter;
import com.m.sofiane.go4lunch.models.NameOfResto;
import com.m.sofiane.go4lunch.models.pojoAutoComplete.AutoComplete;
import com.m.sofiane.go4lunch.models.pojoAutoComplete.Prediction;
import com.m.sofiane.go4lunch.models.pojoMaps.Result;
import com.m.sofiane.go4lunch.services.LatAndLngSingleton;
import com.m.sofiane.go4lunch.services.Singleton;
import com.m.sofiane.go4lunch.utils.MyChoiceHelper;
import com.m.sofiane.go4lunch.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Marker mRestoMarker;
    LatLng mLatLng;
    LatLng mLatLngForAll;
    String placeIdToCompare;
    String apiKey = BuildConfig.APIKEY;
    @BindView(R.id.recyclerview_for_maps)
    RecyclerView mRecyclerView;
    SearchMapAdapter mAdapter;
    ArrayList<Prediction> listdataForSearch;
    FragmentManager mFragmentManager;
    ArrayList<String> ld;
    ArrayList<Result> mResults;
    String placeId;
    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        uploadToolbar();
        ButterKnife.bind(this, view);
        configureRecyclerView();
        ArrayList<Prediction> mT = new ArrayList<>();
        setHasOptionsMenu(true);
        loadMap();
        setRetainInstance(true);

        return view;
    }

    private void configureRecyclerView() {
        this.listdataForSearch = new ArrayList<>();
        this.mAdapter = new SearchMapAdapter(listdataForSearch, mFragmentManager, getContext());
        mRecyclerView.setVisibility(View.INVISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
    }

    private void uploadToolbar() {
        BottomNavigationView mBottomNavigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.activity_main_bottom_navigation);
        mBottomNavigationView.setVisibility(View.VISIBLE);
        TextView mTitleText = getActivity().findViewById(R.id.toolbar_title);
        mTitleText.setText(R.string.i_m_hungry);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Toolbar mToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.activity_main_toolbar);

        inflater.inflate(R.menu.activity_main_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        Utils.colorSearch(searchView, mToolbar);

        searchView.setOnSearchClickListener(v -> DoAfterClickOnSearch(mToolbar, searchView));
        searchView.setOnCloseListener(() -> {
            mToolbar.setNavigationIcon(R.drawable.ic_dehaze_black_24dp);
            initRestaurantPosition();
            return false;
        });
    }

    public void DoAfterClickOnSearch(Toolbar mToolbar, SearchView searchView) {
        initQuery(searchView);
    }

    public void initQuery(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String item) {
                Toast.makeText(getContext(), "QUERY", Toast.LENGTH_SHORT).show();
                if (item.length() != 0) {
                    System.out.println("1-------->");
                    build_retrofit_and_get_responseForSearch(item);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    Toolbar mToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.activity_main_toolbar);
                    mToolbar.setNavigationIcon(R.drawable.ic_dehaze_black_24dp);
                    initRestaurantPosition();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void build_retrofit_and_get_responseForSearch(String input) {
        Call<AutoComplete> call = Utils.retrofitforMaps(input);

        call.enqueue(new Callback<AutoComplete>() {
            @SuppressLint({"RestrictedApi", "LongLogTag"})
            @Override
            public void onResponse(Call<AutoComplete> call, Response<AutoComplete> place) {
                listdataForSearch = new ArrayList<>();
                for (int i = 0; i < Objects.requireNonNull(place.body()).getPredictions().size(); i++) {
                    listdataForSearch.add(place.body().getPredictions().get(i));
                    placeIdToCompare = place.body().getPredictions().get(i).getPlaceId();
                    mAdapter = new SearchMapAdapter(listdataForSearch, mFragmentManager, getContext());
                    mRecyclerView.setAdapter(mAdapter);
                    compareToUpdateMarkers(placeIdToCompare);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<AutoComplete> call, Throwable t) {
            }
        });
    }

    private void loadMap() {
        SupportMapFragment mMFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.containermap);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
            FragmentManager mFragmentManager = getFragmentManager();
            FragmentTransaction mFragmentTransaction = Objects.requireNonNull(mFragmentManager).beginTransaction();
            mMFragment = SupportMapFragment.newInstance();
            mFragmentTransaction.replace(R.id.containermap, mMFragment).commit();
        }
        Objects.requireNonNull(mMFragment).getMapAsync(this);
        CheckGooglePlayServices();
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        } else {
            Log.e("error", "with permission");
        }
    }

    private void CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getContext());
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        0).show();
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.getUiSettings().isTiltGesturesEnabled();
            mMap.setMyLocationEnabled(true);
            mMap.setMinZoomPreference(5.0f);
            mMap.setMaxZoomPreference(20.0f);
        }
        loadDatawithSingleTon();
    }

    private void loadDatawithSingleTon() {
        initMyPosition();
        initRestaurantPosition();
    }

    private void initRestaurantPosition() {
        for (int i = 0; i < Singleton.getInstance().getArrayList().size(); i++) {
            mResults = Singleton.getInstance().getArrayList();
            Double mLatForAll = mResults.get(i).getGeometry().getLocation().getLat();
            Double mLngForAll = mResults.get(i).getGeometry().getLocation().getLng();
            mLatLngForAll = new LatLng(mLatForAll, mLngForAll);
            placeId = mResults.get(i).getPlaceId();
            readDataFromFirebaseForGreen(mLatLngForAll, placeId);
        }
    }

    private void initMyPosition() {
        double mLat = LatAndLngSingleton.getInstance().getmLatitude();
        double mLng = LatAndLngSingleton.getInstance().getmLongitude();
        mLatLng = new LatLng(mLat, mLng);
        loadMapCamera(mLatLng);
    }

    protected synchronized void buildGoogleApiClient() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(getContext()))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void loadMapCamera(LatLng mLatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 12));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    public void readDataFromFirebaseForGreen(LatLng mLatLngForAll, String placeId) {
        ld = new ArrayList<>();
        MyChoiceHelper.getMyChoice()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            NameOfResto l = document.toObject(NameOfResto.class);
                            ld.add(l.getPlaceID());
                        }
                    }
                    if (ld.contains(placeId)) {
                        System.out.println("Account-----******* found" + placeId);
                        markerAllRestaurantGreen(mLatLngForAll, placeId);
                    } else {
                        System.out.println("Account -----******* not found");
                        markerAllRestaurantOrange(mLatLngForAll, placeId);
                    }
                });
    }

    private void compareToUpdateMarkers(String placeIdToCompare) {
        for (int i = 0; i < Singleton.getInstance().getArrayList().size(); i++) {
            mResults = Singleton.getInstance().getArrayList();
            if (placeIdToCompare.equals(mResults.get(i).getReference())) {
                mMap.clear();
                Double mLatForAll = mResults.get(i).getGeometry().getLocation().getLat();
                Double mLngForAll = mResults.get(i).getGeometry().getLocation().getLng();
                mLatLngForAll = new LatLng(mLatForAll, mLngForAll);
            }
            readDataFromFirebaseForGreen(mLatLngForAll, placeIdToCompare);
        }
        Onclick();
    }

    private void markerAllRestaurantOrange(LatLng mLatLngForAll, String placeId) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLngForAll);
        markerOptions.snippet(placeId);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icorange32));
        mRestoMarker = mMap.addMarker(markerOptions);
        Onclick();
    }

    private void markerAllRestaurantGreen(LatLng mLatLngForAll, String placeId) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLngForAll);
        markerOptions.snippet(placeId);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icgreen32));
        mRestoMarker = mMap.addMarker(markerOptions);
        Onclick();
    }

    public void Onclick() {
        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(getContext(), SubActivity.class);
            String mId = marker.getSnippet();
            intent.putExtra("I", mId);
            Objects.requireNonNull(getContext()).startActivity(intent);
            return false;
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


}
