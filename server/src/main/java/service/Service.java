package service;

import dataAccess.DataAccess;
import exception.ResponseException;
import model.UserData;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData register(UserData userData) throws ResponseException {
        dataAccess.register(userData);
        return userData;
    }

    public UserData login(UserData userData) throws ResponseException {
        dataAccess.login(userData);
        return userData;
    }

    public void logout(String authToken) throws ResponseException {
        dataAccess.logout(authToken);
    }
}
