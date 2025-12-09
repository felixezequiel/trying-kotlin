// Módulo Users Service

plugins {
    application
}

dependencies {
    // Dependência do módulo shared
    implementation(project(":shared"))
    
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-server-status-pages:2.3.5")
    
    // KGraphQL para GraphQL
    implementation("com.apurebase:kgraphql:0.18.0")
    implementation("com.apurebase:kgraphql-ktor:0.18.0")
    
    // Ktor Client (para testes)
    testImplementation("io.ktor:ktor-client-core:2.3.5")
    testImplementation("io.ktor:ktor-client-cio:2.3.5")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    testImplementation("io.ktor:ktor-server-test-host:2.3.5")
}

application {
    mainClass.set("users.infrastructure.web.MainKt")
}

sourceSets {
    main {
        kotlin.srcDirs(".")
    }
    test {
        kotlin.srcDirs("../../tests/services/users")
    }
}

// Excluir arquivos de configuração do source set
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    exclude("build.gradle.kts", "Dockerfile")
}
