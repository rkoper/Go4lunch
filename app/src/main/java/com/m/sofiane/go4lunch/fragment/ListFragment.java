package com.m.sofiane.go4lunch.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.m.sofiane.go4lunch.R;
import com.m.sofiane.go4lunch.adapter.ListAdapter;
import com.m.sofiane.go4lunch.models.MyChoice;
import com.m.sofiane.go4lunch.models.pojoMaps.Result;
import com.m.sofiane.go4lunch.services.Singleton;
import com.m.sofiane.go4lunch.utils.MyChoiceHelper;
import com.m.sofiane.go4lunch.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ListFragment extends Fragment {

    final String mKeyName = "1";
    FragmentManager mFragmentManager;
    ArrayList<MyChoice> listData;
    ArrayList<String> mTest;
    @BindView(R.id.List_recyclerView)
    RecyclerView mRecyclerView;
    private ListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, null);
        boolean isBackFromB = false;
        setHasOptionsMenu(true);
        uploadToolbar();
        ButterKnife.bind(this, view);
        return view;
    }


    private void uploadToolbar() {
        TextView mTitleText = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_title);
        mTitleText.setText(R.string.i_m_hungry);
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Toolbar mToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.activity_main_toolbar);

        inflater.inflate(R.menu.activity_main_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        Utils.colorSearch(searchView, mToolbar);

        searchView.setOnCloseListener(() -> {
            mToolbar.setNavigationIcon(R.drawable.ic_dehaze_black_24dp);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("TAG", "onQueryTextSubmitList: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() != 0) {
                    mAdapter.getFilter().filter(newText);

                } else {
                    Toolbar mToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.activity_main_toolbar);
                    mToolbar.setNavigationIcon(R.drawable.ic_dehaze_black_24dp);
                    readFireBase();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void configureRecyclerView() {
        this.listData = new ArrayList<>();
        ArrayList<Result> mData = new ArrayList<>();
        this.mAdapter = new ListAdapter(mData, getContext(), mFragmentManager, mKeyName, mTest);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

    }

    private void readFireBase() {
        ArrayList<Result> mData = Singleton.getInstance().getArrayList();
        MyChoiceHelper.getMyChoice()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            document.getData();
                            MyChoice l = document.toObject(MyChoice.class);
                            listData.add(l);
                        }
                        uploadRecyclerView(mData, listData);
                    }
                });
    }


    private void uploadRecyclerView(ArrayList<Result> mData, ArrayList<MyChoice> listData) {
        ArrayList<String> mTest = new ArrayList<>();
        for (int i = 0; i < listData.size(); i++) {
            mTest.add(listData.get(i).getNameOfResto());
            mAdapter = new ListAdapter(mData, getContext(), mFragmentManager, mKeyName, mTest);
            mRecyclerView.setAdapter(mAdapter);

        }
    }

    @Override
    public void onResume() {
        configureRecyclerView();
        readFireBase();
        super.onResume();
    }
}
