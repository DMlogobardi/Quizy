package controller.menager;

import controller.utility.AccessControlService;
import controller.utility.JWT_Provider;
import controller.utility.PassCrypt;
import controller.utility.SessionLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuizUserMenager {

    @Inject
    PassCrypt crypt;

    @Inject
    SessionLog logBible;

    @Inject
    JWT_Provider jwtProvider;

    @Inject
    AccessControlService accessControl;

    public QuizUserMenager() {
    }
}
