load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@contrib_rules_jvm//java:defs.bzl", "java_junit5_test")

# Library BARU: util_lib (dipakai oleh app_lib)
java_library(
    name = "util_lib",
    srcs = glob(["src/main/java/com/contoh/util/*.java"]),
    visibility = ["//visibility:public"],
)

java_library(
    name = "app_lib",
    srcs = glob(["src/main/java/com/contoh/app/*.java"]),
    deps = [
        ":util_lib",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "app",
    main_class = "com.contoh.app.App",
    runtime_deps = [
        ":app_lib",
        "@maven//:ch_qos_logback_logback_classic",
    ],
)

java_junit5_test(
    name = "app_test",
    srcs = ["src/test/java/com/contoh/app/AppTest.java"],
    test_class = "com.contoh.app.AppTest",
    deps = [
        ":app_lib",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
    ],
)
