package com.cs478.akshay.common;

interface TreasuryAPI {
    List getMonthlyCash(int year);
    List getDailyCash(int year, int month, int day, int workingDays);
    int getYearlyAvg(int year);
}
