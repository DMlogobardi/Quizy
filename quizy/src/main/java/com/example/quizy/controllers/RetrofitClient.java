package com.example.quizy.controllers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    // INSERISCI QUI IL TUO IP DEL SERVER
    private static final String BASE_URL = "http://192.168.1.100:8080/api/auth/login";

    public static QuizyApi getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(QuizyApi.class);
    }
}