package bff.routes

import bff.clients.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.exceptions.NotFoundException

/** Rotas REST para o domínio de Partners */
fun Route.partnersRoutes(partnersClient: IPartnersClient) {
        route("/api/partners") {

                // POST /api/partners - Criar parceiro
                // ADR-011: Vinculação inteligente de usuário pelo email
                // O serviço de Partners busca/cria usuário automaticamente
                post {
                        val request = call.receive<CreatePartnerRequest>()
                        val partner = partnersClient.createPartner(request)
                        call.respond(HttpStatusCode.Created, partner)
                }

                // GET /api/partners - Listar parceiros
                get {
                        val status = call.request.queryParameters["status"]
                        val partners = partnersClient.listPartners(status)
                        call.respond(partners)
                }

                // GET /api/partners/{id} - Buscar parceiro por ID
                get("/{id}") {
                        val id =
                                call.parameters["id"]
                                        ?: return@get call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val partner =
                                partnersClient.getPartnerById(id)
                                        ?: throw NotFoundException("Partner not found with id: $id")
                        call.respond(partner)
                }

                // PUT /api/partners/{id} - Atualizar parceiro
                put("/{id}") {
                        val id =
                                call.parameters["id"]
                                        ?: return@put call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val userId = call.request.headers["X-User-Id"]?.toLongOrNull() ?: 1L
                        val request = call.receive<UpdatePartnerRequest>()
                        val partner = partnersClient.updatePartner(userId, id, request)
                        call.respond(partner)
                }

                // POST /api/partners/{id}/approve - Aprovar parceiro
                post("/{id}/approve") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val partner = partnersClient.approvePartner(id)
                        call.respond(partner)
                }

                // POST /api/partners/{id}/reject - Rejeitar parceiro
                post("/{id}/reject") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val partner = partnersClient.rejectPartner(id)
                        call.respond(partner)
                }

                // POST /api/partners/{id}/suspend - Suspender parceiro
                post("/{id}/suspend") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val partner = partnersClient.suspendPartner(id)
                        call.respond(partner)
                }

                // POST /api/partners/{id}/reactivate - Reativar parceiro
                post("/{id}/reactivate") {
                        val id =
                                call.parameters["id"]
                                        ?: return@post call.respond(
                                                HttpStatusCode.BadRequest,
                                                mapOf("error" to "Partner ID is required")
                                        )
                        val partner = partnersClient.reactivatePartner(id)
                        call.respond(partner)
                }
        }
}
