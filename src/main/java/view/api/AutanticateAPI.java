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
    public Response logout (Map<String,String> body){
        try {
            String token = body.get("token");
            auth.logout(token);

            return Response.ok().build();
        } catch (AppException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
