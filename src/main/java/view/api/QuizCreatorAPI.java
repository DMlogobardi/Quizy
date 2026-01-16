package view.api;

import controller.menager.QuizCreatorMenager;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entity.Quiz;
import model.exception.AppException;

import java.util.List;
import java.util.Map;

@Path("/quiz-manage")
@RequestScoped
public class QuizCreatorAPI {

    @Inject
    private QuizCreatorMenager menager;

    public QuizCreatorAPI() {
    }

    @POST
    @Path("/upRole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upRole(@HeaderParam("Authorization") String authHeader){
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            String newToken = menager.upUserRole(token);
            return Response.ok(newToken).build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQuiz(Quiz quiz, @HeaderParam("Authorization") String authHeader ) {
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            menager.createQuiz(quiz, token);
            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuiz(Quiz quiz, @HeaderParam("Authorization") String authHeader) {
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            menager.deleteQuiz(quiz, token);
            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateQuiz(Quiz quiz, @HeaderParam("Authorization") String authHeader) {
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            menager.aggiornaQuiz(quiz, token);
            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/getQuiz")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuiz(@HeaderParam("Authorization") String authHeader, Map<String,String> body) {
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            int page = Integer.parseInt(body.get("page"));
            int offset = Integer.parseInt(body.get("offset"));
            List<Quiz> quizList = menager.getQuizzes(page, offset, token);
            return Response.ok(quizList).build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
