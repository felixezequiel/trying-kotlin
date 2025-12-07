plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin Standard Library
    implementation(kotlin("stdlib"))
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-server-status-pages:2.3.5")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // KGraphQL para GraphQL
    implementation("com.apurebase:kgraphql:0.18.0")
    implementation("com.apurebase:kgraphql-ktor:0.18.0")
    
    // Ktor Client (para testes)
    testImplementation("io.ktor:ktor-client-core:2.3.5")
    testImplementation("io.ktor:ktor-client-cio:2.3.5")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    testImplementation("io.ktor:ktor-server-test-host:2.3.5")
    
    // JUnit 5 para testes
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Runtime para JUnit 5
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("users.infrastructure.web.MainKt")
}

// Configuração dos diretórios de código fonte e testes
sourceSets {
    main {
        kotlin.srcDirs("users")
    }
    test {
        kotlin.srcDirs("tests")
    }
}

