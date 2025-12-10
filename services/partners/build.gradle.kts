// Modulo Partners Service

plugins {
    application
}

dependencies {
    // Dependencia do modulo shared
    implementation(project(":shared"))

    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-server-status-pages:2.3.5")

    // Ktor Client (ADR-011: comunicacao com BFF)
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-cio:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")

    // Testes
    testImplementation("io.ktor:ktor-server-test-host:2.3.5")
}

application {
    mainClass.set("partners.infrastructure.web.MainKt")
}

sourceSets {
    main {
        kotlin.srcDirs("adapters", "application", "domain", "infrastructure")
    }
    test {
        kotlin.srcDirs("../../tests/services/partners")
    }
}
