rootProject.name = "myapp"

// Módulos do monorepo
include("shared")
include("bff")
include("services:users")

// Configuração de paths dos módulos
project(":shared").projectDir = file("shared")
project(":bff").projectDir = file("bff")
project(":services:users").projectDir = file("services/users")


