package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

import java.util.UUID

class CreateContactsTest extends Simulation {

  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Función para generar emails únicos
  def randomEmail() = s"contact_${UUID.randomUUID.toString.take(8)}@test.com"
  def randomName() = s"Test_${UUID.randomUUID.toString.take(8)}"

  val feeder = Iterator.continually(Map(
    "firstName" -> randomName(),
    "lastName" -> "User",
    "email" -> randomEmail(),
    "birthdate" -> "1990-01-01",
    "phone" -> "1234567890"
  ))

  val scn = scenario("Crear Contactos")
    .exec(
      http("Login para crear contacto")
        .post("users/login")
        .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    )
    .pause(1)
    .repeat(1) {
      feed(feeder)
        .exec(
          http("Crear Contacto")
            .post("contacts")
            .header("Authorization", "Bearer ${authToken}")
            .body(StringBody(
              """{
                "firstName": "${firstName}",
                "lastName": "${lastName}",
                "birthdate": "${birthdate}",
                "email": "${email}",
                "phone": "${phone}",
                "street1": "Carrera 123",
                "street2": "#45-67",
                "city": "Bogotá",
                "stateProvince": "CO",
                "postalCode": "111111",
                "country": "Colombia"
              }"""
            )).asJson
            .check(status.is(201))
        )
    }

  setUp(
    scn.inject(rampUsers(20).during(60)) // Simula creación masiva
  ).protocols(httpConf)
}
