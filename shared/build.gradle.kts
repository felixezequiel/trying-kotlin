// Módulo shared - DTOs e utilitários compartilhados entre serviços

sourceSets {
    main {
        kotlin.srcDirs(".")
        kotlin.exclude("bin/**")
    }
    test {
        kotlin.srcDirs("../tests/shared")
    }
}

// Excluir arquivos de configuração do source set
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    exclude("build.gradle.kts")
}
