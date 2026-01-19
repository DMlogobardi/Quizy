package view.api;

import controller.menager.AutanticateMenager;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entity.Utente;
import model.exception.AppException;
import model.exception.LoginFailed;
import model.exception.RegisterFailed;


import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@RequestScoped
public class AutanticateAPI {

    @Inject
    private AutanticateMenager auth;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login (Utente loginData){
        try{
            if(loginData == null){
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            String token = auth.autenticate(loginData.getPasswordHash(), loginData.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return Response.ok(response).build();
        } catch (LoginFailed e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register (Utente registerData){
        try{
            if(registerData == null){
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            auth.registra(registerData);

            return Response.ok().build();
        } catch (RegisterFailed e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout (@HeaderParam("Authorization") String authHeader){
        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            auth.logout(token);

            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/newPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiapassword (Map<String,String> body, @HeaderParam("Authorization") String authHeader){
        try {
            if(body == null || body.isEmpty()){
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            String token = authHeader.replace("Bearer ", "");
            String password = body.get("password");
            String oldPassword = body.get("oldPassword");

            auth.newPassword(password, oldPassword, token);

            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
