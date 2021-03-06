package com.m.sofiane.go4lunch.services;

import com.m.sofiane.go4lunch.models.pojoMaps.Result;

import java.util.ArrayList;

public final class Singleton {
    private static Singleton uniqInstance;
    public ArrayList<Result> results = new ArrayList<>();

    public Singleton() {
    }

    public static Singleton getInstance() {
        if (uniqInstance == null)
            uniqInstance = new Singleton();
        return uniqInstance;
    }

    public ArrayList<Result> getArrayList() {
        return this.results;

    }

    public void setArrayList(ArrayList<Result> results) {
        this.results = results;
    }


}