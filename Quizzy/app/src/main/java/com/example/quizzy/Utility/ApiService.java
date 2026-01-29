package com.example.quizzy.Utility;

import com.example.quizzy.DTO.CompletaQuizRequest;
import com.example.quizzy.DTO.CompletaQuizResponse;
import com.example.quizzy.DTO.DeleteQuizRequest;
import com.example.quizzy.DTO.GetQuizUserRequest;
import com.example.quizzy.DTO.ListQuizDTO;
import com.example.quizzy.DTO.LoginRequest;
import com.example.quizzy.DTO.LoginResponse;
import com.example.quizzy.DTO.QuizDTO;
import com.example.quizzy.DTO.RegistrazioneRequest;
import com.example.quizzy.DTO.StartQuizNoPassRequest;
import com.example.quizzy.DTO.UpRoleResponse;
import com.example.quizzy.DTO.UpdateQuizRequest;
import com.example.quizzy.DTO.domandaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    // login (@Hader("Authorization") String token, @Body (DTO che mi creo))
    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String token);
    @POST("auth/register")
    Call<Void> registrazione(@Body RegistrazioneRequest registrazioneRequest);

    @POST("quiz-manage/upRole")
    Call<UpRoleResponse> upRuolo(@Header("Authorization") String header);
    // metto responseBody per conservarmi la stringa che mi cambia il ruolo
    // se usassi il void il nuovo cambio ruolo verrebbe ignorato

    @POST("quiz-manage/create")
    Call<Void> createQuiz(@Header("Authorization") String token, @Body QuizDTO quiz);

    @POST("quiz-manage/getQuiz")
    Call<List<ListQuizDTO>> getQuizListCreator(@Header("Authorization") String token, @Body GetQuizUserRequest request);



    @POST("quiz-use/getQuiz")
    Call<List<ListQuizDTO>> getQuizListUser(@Header("Authorization") String token, @Body GetQuizUserRequest getQuizUserRequest);

    @POST("quiz-use/startQuiz")
    Call<List<domandaDTO>> startQuiz(@Header("Authorization") String token, @Body StartQuizNoPassRequest request);

    @POST("quiz-use/completa")
    Call<CompletaQuizResponse> completaQuiz(@Header("Authorization") String token, @Body CompletaQuizRequest request);

    @POST("quiz-manage/delete")
    Call<Void> deleteQuiz(@Header("Authorization") String token, @Body DeleteQuizRequest request);

    @POST("quiz-manage/update")
    Call<Void> updateQuiz(@Header("Authorization") String token, @Body UpdateQuizRequest request);

}

