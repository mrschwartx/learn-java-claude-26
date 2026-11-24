// Terjemahan pom.xml latihan-2 (TIDAK dijalankan di sandbox: butuh Maven Central)
plugins {
    java
    application
}

repositories { mavenCentral() }

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
}

application { mainClass = "com.contoh.app.App" }
