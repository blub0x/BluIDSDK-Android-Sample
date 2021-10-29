package com.blub0x.bluidsdk_sample_app.Database

data class UserCreds(
    var username: String = "",
    var password: String = "",
    var rememberMe: Boolean = false
)

data class Environment(
    var env: String = "QA"
)


