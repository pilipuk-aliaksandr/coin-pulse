plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.open.api.generator)
    checkstyle
}

extra["httpclient5.version"] = "5.6.5"
extra["httpcore5.version"] = "5.4.4"
extra["jetty.version"] = "12.1.14"

group = "by.pilipuk"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.projectlombok:lombok:${libs.versions.lombok.get()}")
    annotationProcessor("org.projectlombok:lombok:${libs.versions.lombok.get()}")

    implementation("org.mapstruct:mapstruct:${libs.versions.mapstruct.get()}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${libs.versions.mapstruct.get()}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${libs.versions.lombokMapstructBinding.get()}")

    implementation("org.postgresql:postgresql:${libs.versions.postgresql.get()}")
    implementation("org.liquibase:liquibase-core")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${libs.versions.springdoc.get()}")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:${libs.versions.archunit.get()}")
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:${libs.versions.wiremock.get()}")
}

tasks.test {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/specification/spec.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)

    packageName.set("by.pilipuk")
    apiPackage.set("by.pilipuk.api")
    modelPackage.set("by.pilipuk.dto")

    skipValidateSpec.set(true)

    configOptions.set(
        mapOf(
            "openApiNullable" to "false",
            "interfaceOnly" to "true",
            "useResponseEntity" to "false",
            "useJakartaEe" to "true",
            "useTags" to "true",
            "additionalModelTypeAnnotations" to "@lombok.Builder(toBuilder = true)\n@lombok.NoArgsConstructor"
        )
    )
}

sourceSets {
    named("main") {
        java.srcDir(layout.buildDirectory.dir("generated/src/main/java"))
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}

tasks.named("test") {
    dependsOn("checkstyleMain", "checkstyleTest")
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}
