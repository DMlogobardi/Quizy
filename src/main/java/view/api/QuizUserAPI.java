package view.api;

import controller.menager.QuizUserMenager;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.dto.CompletaQuizDTO;
import model.dto.QuizDTO;
import model.entity.Domanda;
import model.entity.Quiz;
import model.entity.Risposta;
import model.exception.AppException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/quiz-use")
@RequestScoped
public class QuizUserAPI {

    @Inject
    private QuizUserMenager useMenager;

    public QuizUserAPI() {
    }

    @POST
    @Path("/downRole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response downRole(@HeaderParam("Authorization") String authHeader) {
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String token = authHeader.replace("Bearer ", "");
            String newToken = useMenager.downUserRole(token);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);

            return Response.ok(response).build();
        } catch (MalformedJwtException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }  catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/getQuiz")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuiz(@HeaderParam("Authorization") String authHeader, Map<String,String> body) {
        try{
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String token = authHeader.replace("Bearer ", "");

            if (body == null || body.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            int page = Integer.parseInt(body.get("page"));
            int offset = Integer.parseInt(body.get("offset"));
            List<Quiz> quizList = useMenager.getQuizzes(page, offset, token);
            List<QuizDTO> quizSummaries = quizList.stream()
                    .map(QuizDTO::new)
                    .toList();


            return Response.ok(quizSummaries).build();
        } catch (MalformedJwtException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }  catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/startQuiz")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startQuiz(@HeaderParam("Authorization") String authHeader, Quiz quiz){
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (quiz == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            String token = authHeader.replace("Bearer ", "");

            List<Domanda> domande = useMenager.startQuiz(quiz, token);
            if(domande == null) {
                return Response.status(Response.Status.UNAVAILABLE_FOR_LEGAL_REASONS).build();
            }

            return Response.ok(domande).build();

        } catch (MalformedJwtException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }  catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/startQuiz_password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startQuizByPass(@HeaderParam("Authorization") String authHeader, Quiz quiz){
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (quiz == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            String token = authHeader.replace("Bearer ", "");

            List<Domanda> domande = useMenager.startQuiz(quiz, quiz.getPasswordQuiz(), token);

            if(domande == null) {
                return Response.status(Response.Status.UNAVAILABLE_FOR_LEGAL_REASONS).build();
            }

            return Response.ok(domande).build();

        } catch (MalformedJwtException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }  catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/completa")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response completaQuiz(@HeaderParam("Authorization") String authHeader, CompletaQuizDTO request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            List<Risposta> risposteClient = request.getRisposteClient();
            Quiz quiz = request.getQuiz();

            if (risposteClient == null || risposteClient.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if(quiz == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            String token = authHeader.replace("Bearer ", "");

            int punteggio = useMenager.completaQuiz(quiz, risposteClient, token);
            Map<String, Integer> respons = new HashMap<>();
            respons.put("punteggio", punteggio);

            return Response.ok(respons).build();
        } catch (MalformedJwtException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }  catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
