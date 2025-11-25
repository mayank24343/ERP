package edu.univ.erp.api.types;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.UserService;

public class UserApi {
    private final UserService userService;

    //constructor
    public UserApi(UserService userService) {
        this.userService = userService;
    }

    //Api to return the complete profile of user
    public ApiResult<User> loadUserProfile(User user) {
        try {
            return ApiResult.ok(userService.loadUserProfile(user));
        }
        catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
