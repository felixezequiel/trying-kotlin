plugins {
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
    jacoco
}

group = "com.myapp"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

// Configurações compartilhadas para todos os subprojetos
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "jacoco")

    group = "com.myapp"
    version = "1.0-SNAPSHOT"

    repositories { mavenCentral() }

    dependencies {
        // Kotlin Standard Library
        "implementation"(kotlin("stdlib"))

        // Coroutines
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

        // Kotlinx Serialization
        "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

        // JUnit 5 para testes
        "testImplementation"("org.jetbrains.kotlin:kotlin-test")
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.1")
        "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

        // Runtime para JUnit 5
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> { jvmToolchain(17) }
}

// Task para gerar relatório de coverage consolidado
tasks.register<JacocoReport>("jacocoFullReport") {
    group = "verification"
    description = "Gera relatório de coverage consolidado de todos os módulos"

    // Depende dos testes e relatórios de todos os subprojetos
    dependsOn(subprojects.map { it.tasks.named("test") })
    dependsOn(subprojects.map { it.tasks.named("jacocoTestReport") })

    // Coleta sources e classes de todos os subprojetos
    additionalSourceDirs.setFrom(
            subprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    sourceDirectories.setFrom(
            subprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    classDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].output })
    executionData.setFrom(subprojects.map { it.layout.buildDirectory.file("jacoco/test.exec") })

    reports {
        xml.required.set(true)
        xml.outputLocation.set(
                layout.buildDirectory.file("reports/jacoco/full/jacocoFullReport.xml")
        )
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/full/html"))
    }
}


// Task para parar os serviços
tasks.register("dev-stop") {
    group = "application"
    description = "Para todos os serviços em execução"

    doLast {
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")

        if (isWindows) {
            // Para processos Gradle e fecha terminais
            ProcessBuilder(
                            "cmd",
                            "/c",
                            "taskkill /F /FI \"WINDOWTITLE eq Users Service*\" /T 2>nul & taskkill /F /FI \"WINDOWTITLE eq BFF*\" /T 2>nul"
                    )
                    .inheritIO()
                    .start()
                    .waitFor()

            println("Serviços parados.")
        }
    }
}

// Task para Docker Compose (se Docker estiver disponível)
tasks.register<Exec>("dev-docker") {
    group = "application"
    description = "Inicia todos os serviços via Docker Compose"

    commandLine("docker", "compose", "up", "--build")
}

// Task para parar Docker Compose
tasks.register<Exec>("dev-docker-stop") {
    group = "application"
    description = "Para todos os serviços do Docker Compose"

    commandLine("docker", "compose", "down")
}
