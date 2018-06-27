package com.bys.larpc.service;

import java.util.ArrayList;

public class CalculatorImpl implements Calculator {
    public int add(String[] d,int c,int[] a, int b) {
        return b;
    }

    @Override
    public int add(ArrayList<Integer> l) {
        return 0;
    }

    @Override
    public int add(int a, int b) {
        return a+b;
    }
}
