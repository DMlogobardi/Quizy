package view.api;

import controller.menager.QuizUserMenager;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.dto.QuizDTO;
import model.entity.Quiz;
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
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            String newToken = useMenager.downUserRole(token);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);

            return Response.ok(response).build();
        } catch (AppException e) {
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
                return Response.status(Response.Status.UNAUTHORIZED).build();
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
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
