package com.example.quizy.controllers;

import com.example.quizy.models.LoginResponse;
import com.example.quizy.models.RichiesteLogin;
import com.example.quizy.models.RichiesteRegistrazione;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface QuizyApi {

    // CORRETTO: Nel backend c'è @Path("/auth") sulla classe e @Path("/register") sul metodo
    @POST("auth/register")
    Call<Void> registraUtente(@Body RichiesteRegistrazione body);

    // CORRETTO: Percorso "auth/login". Ritorna LoginResponse (il token)
    @POST("auth/login")
    Call<LoginResponse> login(@Body RichiesteLogin body);
}