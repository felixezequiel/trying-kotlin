rootProject.name = "myapp"

// Modulos do monorepo
include("shared")
include("bff")
include("services:users")
include("services:partners")
include("services:events")
include("services:tickets")
include("services:reservations")
include("services:orders")

// Configuracao de paths dos modulos
project(":shared").projectDir = file("shared")
project(":bff").projectDir = file("bff")
project(":services:users").projectDir = file("services/users")
project(":services:partners").projectDir = file("services/partners")
project(":services:events").projectDir = file("services/events")
project(":services:tickets").projectDir = file("services/tickets")
project(":services:reservations").projectDir = file("services/reservations")
project(":services:orders").projectDir = file("services/orders")
