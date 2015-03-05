package ru.mail.teamcity.web.parameters.Authentication;

import java.net.*;


/**
 * Created by ramaish on 3/5/2015.
 */
public class WebServiceAuthentication extends Authenticator{
    public String UserName = "jhrpsadmin";
    public String Password = "jhrps@dmin";
    public PasswordAuthentication getPasswordAuthentication () {
        return new PasswordAuthentication (UserName, Password.toCharArray());
    }
}
