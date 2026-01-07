package view.api;

import controller.menager.QuizCreatorMenager;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entity.Quiz;
import model.exception.AppException;

@Path("/quiz-manage")
@RequestScoped
public class QuizCreatorAPI {

    @Inject
    private QuizCreatorMenager menager;

    public QuizCreatorAPI() {
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQuiz(Quiz quiz, @HeaderParam("Authorization") String authHeader ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            menager.createQuiz(quiz, token);
            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


}
