plugins {
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
}

group = "com.myapp"
version = "1.0-SNAPSHOT"

// Configurações compartilhadas para todos os subprojetos
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    group = "com.myapp"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

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
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }
}

// Task para iniciar todos os serviços (sem Docker)
tasks.register("dev") {
    group = "application"
    description = "Inicia todos os serviços em modo desenvolvimento"
    
    doLast {
        println("")
        println("========================================")
        println("  MyApp - Iniciando serviços...")
        println("========================================")
        println("")
        
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        
        if (isWindows) {
            println("Abrindo 2 terminais...")
            println("  - Users Service: http://localhost:8081")
            println("  - BFF:           http://localhost:8080")
            println("")
            
            // Inicia Users Service em novo terminal
            ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "title Users Service && cd /d ${projectDir} && gradle :services:users:run --no-rebuild")
                .directory(projectDir)
                .inheritIO()
                .start()
            
            Thread.sleep(2000)
            
            // Inicia BFF em novo terminal
            ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "title BFF && cd /d ${projectDir} && gradle :bff:run --no-rebuild")
                .directory(projectDir)
                .inheritIO()
                .start()
                
            println("Terminais abertos! Aguarde os serviços iniciarem.")
            println("")
            println("Para parar: feche os terminais ou use 'gradle dev-stop'")
        } else {
            println("Em Linux/Mac, execute em terminais separados:")
            println("  Terminal 1: gradle :services:users:run")
            println("  Terminal 2: gradle :bff:run")
        }
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
            ProcessBuilder("cmd", "/c", "taskkill /F /FI \"WINDOWTITLE eq Users Service*\" /T 2>nul & taskkill /F /FI \"WINDOWTITLE eq BFF*\" /T 2>nul")
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

