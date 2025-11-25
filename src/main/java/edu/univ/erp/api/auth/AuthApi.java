package edu.univ.erp.api.auth;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.ServiceException;

import java.sql.SQLException;

public class AuthApi {
    private final AuthService service;

    //constructor to add service to Api
    public AuthApi(AuthService auth) {
        this.service = auth;
    }

    //Api to authenticate user
    public ApiResult<User> authenticateUser(String username, String password) {
        try {
            return ApiResult.ok(service.login(username, password));
        }
        catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //logout
    public ApiResult<Void> logoutUser() {
        try {
            service.logout();
            return ApiResult.okMessage("Logged Out.");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
