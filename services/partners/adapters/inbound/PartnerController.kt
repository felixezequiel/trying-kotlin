package partners.adapters.inbound

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import partners.application.dto.CreatePartnerRequest
import partners.application.dto.ErrorResponse
import partners.application.dto.PartnerResponse
import partners.application.dto.RejectPartnerRequest
import partners.application.dto.UpdatePartnerRequest
import partners.application.useCases.ApprovePartnerUseCase
import partners.application.useCases.CreatePartnerUseCase
import partners.application.useCases.GetPartnerUseCase
import partners.application.useCases.ListPartnersUseCase
import partners.application.useCases.ReactivatePartnerUseCase
import partners.application.useCases.RejectPartnerUseCase
import partners.application.useCases.SuspendPartnerUseCase
import partners.application.useCases.UpdatePartnerUseCase
import partners.domain.PartnerStatus

class PartnerController(
        private val createPartnerUseCase: CreatePartnerUseCase,
        private val updatePartnerUseCase: UpdatePartnerUseCase,
        private val approvePartnerUseCase: ApprovePartnerUseCase,
        private val rejectPartnerUseCase: RejectPartnerUseCase,
        private val suspendPartnerUseCase: SuspendPartnerUseCase,
        private val reactivatePartnerUseCase: ReactivatePartnerUseCase,
        private val getPartnerUseCase: GetPartnerUseCase,
        private val listPartnersUseCase: ListPartnersUseCase
) {

    suspend fun createPartner(call: ApplicationCall) {
        try {
            val request = call.receive<CreatePartnerRequest>()

            // ADR-011: Vinculação inteligente de usuário
            // O UseCase busca/cria usuário automaticamente pelo email
            val partnerId = createPartnerUseCase.execute(request)

            val partner = getPartnerUseCase.execute(partnerId)
            if (partner != null) {
                call.respond(HttpStatusCode.Created, PartnerResponse.fromDomain(partner))
            } else {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Parceiro criado mas não encontrado")
                )
            }
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(e.message ?: "Conflito ao criar parceiro")
            )
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun getPartnerById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val partner = getPartnerUseCase.execute(partnerId)
            if (partner != null) {
                call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(partner))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Parceiro não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun getMyPartner(call: ApplicationCall) {
        try {
            val userId = call.request.headers["X-User-Id"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuário não autenticado"))
                return
            }

            val partner = getPartnerUseCase.executeByUserId(userId)
            if (partner != null) {
                call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(partner))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Parceiro não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun updatePartner(call: ApplicationCall) {
        try {
            val userId = call.request.headers["X-User-Id"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Usuário não autenticado"))
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request = call.receive<UpdatePartnerRequest>()
            val updatedPartner = updatePartnerUseCase.execute(partnerId, userId, request)
            call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(updatedPartner))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Parceiro não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun approvePartner(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val approvedPartner = approvePartnerUseCase.execute(partnerId)
            call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(approvedPartner))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Parceiro não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun rejectPartner(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request = call.receive<RejectPartnerRequest>()
            val rejectedPartner = rejectPartnerUseCase.execute(partnerId, request.reason)
            call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(rejectedPartner))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun suspendPartner(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val suspendedPartner = suspendPartnerUseCase.execute(partnerId)
            call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(suspendedPartner))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Parceiro não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun reactivatePartner(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val reactivatedPartner = reactivatePartnerUseCase.execute(partnerId)
            call.respond(HttpStatusCode.OK, PartnerResponse.fromDomain(reactivatedPartner))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Parceiro não encontrado")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Operação não permitida")
            )
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun listPartners(call: ApplicationCall) {
        try {
            val statusParam = call.request.queryParameters["status"]

            val partners =
                    if (statusParam != null) {
                        val status =
                                try {
                                    PartnerStatus.valueOf(statusParam.uppercase())
                                } catch (e: IllegalArgumentException) {
                                    call.respond(
                                            HttpStatusCode.BadRequest,
                                            ErrorResponse("Status inválido: $statusParam")
                                    )
                                    return
                                }
                        listPartnersUseCase.executeByStatus(status)
                    } else {
                        listPartnersUseCase.execute()
                    }

            call.respond(HttpStatusCode.OK, partners.map { PartnerResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }
}
