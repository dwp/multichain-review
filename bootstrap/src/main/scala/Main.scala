
import scalaj.http._
import com.dhpcs.jsonrpc._
import play.api.libs.json._
import scala.io.StdIn._

object Main extends App {
  val HmrcNode = "http://192.168.99.100:8081"
  val DwpNode = "http://192.168.99.100:8082"
  val AuthHeader = "Basic " + Base64.encodeString("multichainrpc:password")

  println("\n\nExperiment: Government interdepartmental MultiChain\n\n")

  val hmrcAddress = (Json.parse(Http(HmrcNode)
    .header("Authorization", AuthHeader)
    .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("listpermissions", Left(new JsArray(List(JsString("admin")))), None))))
    .asString
    .body) \\ "address").head.as[String]

  println(s"HMRC address: ${hmrcAddress}")

  val dwpAddress = (Json.parse(Http(DwpNode)
    .header("Authorization", AuthHeader)
    .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("getaddresses", Left(new JsArray(List())), None))))
    .asString
    .body) \ "result")(0).as[String]

  println(s"DWP address: ${dwpAddress}")


  def bootstrap(): Unit = {
    println("Running bootstrap...")

    // set send, receive & issue permissions on the DWP address
    Http(HmrcNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(
        JsonRpcRequestMessage("grant", Left(new JsArray(List(JsString(dwpAddress), JsString("receive,send,issue")))), None))))
      .asString

    // create a bunch of addresses representing people

    def newAddress(): String = {
      val getNewAddressJson = Json.stringify(Json.toJson(JsonRpcRequestMessage("getnewaddress", Left(new JsArray(List.empty)), None)))
      val response = Http(HmrcNode)
        .header("Authorization", AuthHeader)
        .postData(getNewAddressJson)
        .asString

      (Json.parse(response.body) \\ "result").head.as[String]
    }

    val addresses = for (i <- 1 to 100) yield newAddress()
    println(s"\nAddresses for individuals:\n${addresses.mkString("\n")}")

    // create a new asset type: NIC-STAMP
    Http(HmrcNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("issue", Left(new JsArray(List(
        JsString(hmrcAddress),
        JsString("NIC-STAMP"),
        JsNumber(1000000000000L)))), None))))
      .asString

    // create another new asset: TAX-LIABILITY-GBP
    Http(HmrcNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("issue", Left(new JsArray(List(
        JsString(hmrcAddress),
        JsString("TAX-LIABILITY-GBP"),
        JsNumber(1000000000000L),
        JsNumber(0.01)))), None))))
      .asString

    // create another new asset: BENEFIT-PAID-GBP
    Http(DwpNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("issue", Left(new JsArray(List(
        JsString(dwpAddress),
        JsString("BENEFIT-PAID-GBP"),
        JsNumber(1000000000000L),
        JsNumber(0.01)))), None))))
      .asString

    for (address <- addresses) {
      // Enable receive permissions for the newly created addresses
      Http(HmrcNode)
        .header("Authorization", AuthHeader)
        .postData(Json.stringify(Json.toJson(
          JsonRpcRequestMessage("grant", Left(new JsArray(List(JsString(address), JsString("receive")))), None))))
        .asString
    }
  }

  def printHelp: Unit = println("""Commands:
      |  bootstrap          Sets up MultiChain with the relevant assets and addresses
      |  nicstamp <address> <amount>
      |                     Assign a number of NIC stamps to an individual's address
      |  getnicstamps <address>
      |                     Find the number of stamps assigned to an individual's address
      |  jsa <address> <amount>
      |                     Make a Job seekers allowance benefit payment to an address
      |  help               Show available commands
      |  exit               Quit this application""".stripMargin)

  def payJsa(command: String) = {
    val part = command.replaceAll("\\s+", " ").split(' ')
    val address = part(1)
    val amount = part(2)
    Http(DwpNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("sendwithmetadatafrom", Left(new JsArray(List(
        JsString(dwpAddress),
        JsString(address),
        JsString(s"{'BENEFIT-PAID-GBP': ${amount}}"),
        JsString("THIS SHOULD BE HEX")))), None))))
      .asString
  }

  def addNicStamps(command: String) = {
    val part = command.replaceAll("\\s+", " ").split(' ')
    val address = part(1)
    val stamps = part(2).toInt
    Http(HmrcNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("sendassetfrom", Left(new JsArray(List(
        JsString(hmrcAddress),
        JsString(address),
        JsString("NIC-STAMP"),
        JsNumber(stamps)))), None))))
      .asString

    println(s"Added ${stamps} NIC stamps to address: ${address}")
  }

  def getNicStamps(command: String) = {
    val part = command.replaceAll("\\s+", " ").split(' ')
    val address = part(1)
    val response = Http(HmrcNode)
      .header("Authorization", AuthHeader)
      .postData(Json.stringify(Json.toJson(JsonRpcRequestMessage("getaddressbalances", Left(new JsArray(List(JsString(address)))), None))))
      .asString

    val stamps = for {
      obj <- (Json.parse(response.body) \ "result").as[List[JsObject]]
      qty = obj \ "qty" if (obj \ "name").as[String] == "NIC-STAMP"
    } yield {
      qty.as[Long]
    }

    println(s"There are ${stamps.sum} NIC stamps for address: ${address}")
  }

  def main = {
    print("exp> ")
    var input: String = readLine
    while (input != "quit" && input != "exit") {

      input match {
        case "bootstrap" => bootstrap()
        case s if s.startsWith("nicstamp ") => addNicStamps(s)
        case s if s.startsWith("getnicstamps ") => getNicStamps(s)
        case "help" => printHelp
        case _ => printHelp
      }

      print("exp> ")
      input = readLine
    }
  }

  main
}
