rootProject.name = "myapp"

// Modulos do monorepo
include("shared")
include("bff")
include("services:users")
include("services:partners")

// Configuracao de paths dos modulos
project(":shared").projectDir = file("shared")
project(":bff").projectDir = file("bff")
project(":services:users").projectDir = file("services/users")
project(":services:partners").projectDir = file("services/partners")
