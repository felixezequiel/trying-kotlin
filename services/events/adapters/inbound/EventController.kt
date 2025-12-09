package events.adapters.inbound

import events.application.dto.CreateEventRequest
import events.application.dto.ErrorResponse
import events.application.dto.EventResponse
import events.application.dto.UpdateEventRequest
import events.application.useCases.CancelEventUseCase
import events.application.useCases.CreateEventUseCase
import events.application.useCases.FinishEventUseCase
import events.application.useCases.GetEventUseCase
import events.application.useCases.ListEventsUseCase
import events.application.useCases.PublishEventUseCase
import events.application.useCases.UpdateEventUseCase
import events.domain.EventStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class EventController(
        private val createEventUseCase: CreateEventUseCase,
        private val updateEventUseCase: UpdateEventUseCase,
        private val publishEventUseCase: PublishEventUseCase,
        private val cancelEventUseCase: CancelEventUseCase,
        private val finishEventUseCase: FinishEventUseCase,
        private val getEventUseCase: GetEventUseCase,
        private val listEventsUseCase: ListEventsUseCase
) {

    suspend fun createEvent(call: ApplicationCall) {
        try {
            // TODO: Obter partnerId do token de autenticação
            // Por enquanto, recebemos via header para testes
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            if (partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Partner não autenticado"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(partnerIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Partner ID inválido")
                        )
                        return
                    }

            val request = call.receive<CreateEventRequest>()
            val eventId = createEventUseCase.execute(partnerId, request)

            val event = getEventUseCase.execute(eventId)
            if (event != null) {
                call.respond(HttpStatusCode.Created, EventResponse.fromDomain(event))
            } else {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Evento criado mas não encontrado")
                )
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Dados inválidos"))
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun getEventById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val eventId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            // Verificar se é acesso público ou autenticado
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            val event =
                    if (isAdmin) {
                        getEventUseCase.execute(eventId)
                    } else if (partnerIdStr != null) {
                        val partnerId = UUID.fromString(partnerIdStr)
                        getEventUseCase.executeForPartner(eventId, partnerId)
                                ?: getEventUseCase.executePublic(eventId)
                    } else {
                        getEventUseCase.executePublic(eventId)
                    }

            if (event != null) {
                call.respond(HttpStatusCode.OK, EventResponse.fromDomain(event))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Evento não encontrado"))
            }
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun updateEvent(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            if (partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Partner não autenticado"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(partnerIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Partner ID inválido")
                        )
                        return
                    }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val eventId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val request = call.receive<UpdateEventRequest>()
            val updatedEvent = updateEventUseCase.execute(eventId, partnerId, request)
            call.respond(HttpStatusCode.OK, EventResponse.fromDomain(updatedEvent))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Evento não encontrado")
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

    suspend fun publishEvent(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            if (partnerIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Partner não autenticado"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(partnerIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Partner ID inválido")
                        )
                        return
                    }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val eventId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val publishedEvent = publishEventUseCase.execute(eventId, partnerId)
            call.respond(HttpStatusCode.OK, EventResponse.fromDomain(publishedEvent))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Evento não encontrado")
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

    suspend fun cancelEvent(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            val partnerId =
                    partnerIdStr?.let {
                        try {
                            UUID.fromString(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }

            if (!isAdmin && partnerId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Não autenticado"))
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val eventId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val cancelledEvent = cancelEventUseCase.execute(eventId, partnerId, isAdmin)
            call.respond(HttpStatusCode.OK, EventResponse.fromDomain(cancelledEvent))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Evento não encontrado")
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

    suspend fun finishEvent(call: ApplicationCall) {
        try {
            val partnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            val partnerId =
                    partnerIdStr?.let {
                        try {
                            UUID.fromString(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }

            if (!isAdmin && partnerId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Não autenticado"))
                return
            }

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID é obrigatório"))
                return
            }

            val eventId =
                    try {
                        UUID.fromString(id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID inválido"))
                        return
                    }

            val finishedEvent = finishEventUseCase.execute(eventId, partnerId, isAdmin)
            call.respond(HttpStatusCode.OK, EventResponse.fromDomain(finishedEvent))
        } catch (e: IllegalArgumentException) {
            call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(e.message ?: "Evento não encontrado")
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

    suspend fun listPublicEvents(call: ApplicationCall) {
        try {
            val events = listEventsUseCase.executePublic()
            call.respond(HttpStatusCode.OK, events.map { EventResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun listPartnerEvents(call: ApplicationCall) {
        try {
            val partnerIdStr = call.parameters["partnerId"]
            if (partnerIdStr == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Partner ID é obrigatório"))
                return
            }

            val partnerId =
                    try {
                        UUID.fromString(partnerIdStr)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Partner ID inválido")
                        )
                        return
                    }

            // Verificar se é o próprio partner ou admin
            val requestPartnerIdStr = call.request.headers["X-Partner-Id"]
            val isAdmin = call.request.headers["X-Is-Admin"]?.toBoolean() ?: false

            if (!isAdmin) {
                val requestPartnerId =
                        requestPartnerIdStr?.let {
                            try {
                                UUID.fromString(it)
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }

                if (requestPartnerId != partnerId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Acesso negado"))
                    return
                }
            }

            val events = listEventsUseCase.executeByPartner(partnerId)
            call.respond(HttpStatusCode.OK, events.map { EventResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }

    suspend fun listEventsByStatus(call: ApplicationCall) {
        try {
            val statusParam = call.request.queryParameters["status"]

            val events =
                    if (statusParam != null) {
                        val status =
                                try {
                                    EventStatus.valueOf(statusParam.uppercase())
                                } catch (e: IllegalArgumentException) {
                                    call.respond(
                                            HttpStatusCode.BadRequest,
                                            ErrorResponse("Status inválido: $statusParam")
                                    )
                                    return
                                }
                        listEventsUseCase.executeByStatus(status)
                    } else {
                        listEventsUseCase.executeAll()
                    }

            call.respond(HttpStatusCode.OK, events.map { EventResponse.fromDomain(it) })
        } catch (e: Exception) {
            call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Erro interno do servidor: ${e.message}")
            )
        }
    }
}
