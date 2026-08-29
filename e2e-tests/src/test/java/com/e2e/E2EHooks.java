package com.e2e;

import com.e2e.auth.AuthContext;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class E2EHooks {

    @Autowired
    private AuthContext authContext;

    @Before
    public void resetIdentityToAdmin() {
        authContext.useAdmin();
    }
}
