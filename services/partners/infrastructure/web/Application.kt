package partners.infrastructure.web

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import partners.adapters.inbound.PartnerController
import partners.adapters.outbound.PartnerRepositoryAdapter
import partners.application.dto.ErrorResponse
import partners.application.useCases.ApprovePartnerUseCase
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.GetPartnerUseCase
import partners.application.useCases.ListPartnersUseCase
import partners.application.useCases.ReactivatePartnerUseCase
import partners.application.useCases.RejectPartnerUseCase
import partners.application.useCases.SuspendPartnerUseCase
import partners.application.useCases.UpdatePartnerUseCase
import partners.infrastructure.persistence.DatabaseContext

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
        )
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno: ${cause.message}")
            )
        }
    }

    // Inicialização das dependências
    val dbContext = DatabaseContext()
    val partnerRepository = PartnerRepositoryAdapter(dbContext)

    // Use Cases
    val createPartnerUseCase = CreatePartnerUseCase(partnerRepository)
    val updatePartnerUseCase = UpdatePartnerUseCase(partnerRepository)
    val approvePartnerUseCase = ApprovePartnerUseCase(partnerRepository)
    val rejectPartnerUseCase = RejectPartnerUseCase(partnerRepository)
    val suspendPartnerUseCase = SuspendPartnerUseCase(partnerRepository)
    val reactivatePartnerUseCase = ReactivatePartnerUseCase(partnerRepository)
    val getPartnerUseCase = GetPartnerUseCase(partnerRepository)
    val listPartnersUseCase = ListPartnersUseCase(partnerRepository)

    // Controller
    val partnerController =
            PartnerController(
                    createPartnerUseCase,
                    updatePartnerUseCase,
                    approvePartnerUseCase,
                    rejectPartnerUseCase,
                    suspendPartnerUseCase,
                    reactivatePartnerUseCase,
                    getPartnerUseCase,
                    listPartnersUseCase
            )

    // Configuração de rotas REST
    routing {
        route("/partners") {
            // POST /partners - Cria parceiro (PARTNER)
            post { partnerController.createPartner(call) }

            // GET /partners - Lista parceiros com filtros (ADMIN)
            get { partnerController.listPartners(call) }

            // GET /partners/me - Parceiro do usuário logado (PARTNER)
            get("/me") { partnerController.getMyPartner(call) }

            // Rotas com ID
            route("/{id}") {
                // GET /partners/{id} - Busca por ID (Autenticado)
                get { partnerController.getPartnerById(call) }

                // PUT /partners/{id} - Atualiza parceiro (PARTNER próprio)
                put { partnerController.updatePartner(call) }

                // POST /partners/{id}/approve - Aprova (ADMIN)
                post("/approve") { partnerController.approvePartner(call) }

                // POST /partners/{id}/reject - Rejeita (ADMIN)
                post("/reject") { partnerController.rejectPartner(call) }

                // POST /partners/{id}/suspend - Suspende (ADMIN)
                post("/suspend") { partnerController.suspendPartner(call) }

                // POST /partners/{id}/reactivate - Reativa (ADMIN)
                post("/reactivate") { partnerController.reactivatePartner(call) }
            }
        }
    }
}
