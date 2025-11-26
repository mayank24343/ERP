package edu.univ.erp.api.auth;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;


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

    //Api to logout user
    public ApiResult<Void> logoutUser() {
        try {
            service.logout();
            return ApiResult.okMessage("Logged Out.");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    //Api to change password
    public ApiResult<Void> changePassword(String username, String oldPass, String newPass) {
        try {
            service.changePassword(username, oldPass, newPass);
            return ApiResult.okMessage("Password Changed.");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
