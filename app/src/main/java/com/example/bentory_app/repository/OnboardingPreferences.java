package com.example.bentory_app.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingPreferences {

    // SharedPreferences file name.
    private static final String PREFS_NAME = "onboarding_prefs";

    // Key to check if onboarding is completed.
    private static final String KEY_COMPLETED = "onboarding_completed";


    // Check if onboarding has been completed.
    public static boolean isCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_COMPLETED, false); // Default is faulse if key not found.
    }

    // Mark onboarding as completed.
    public static void setCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_COMPLETED, true).apply(); // Save "completed" as true.
    }

}
