package unit.views.revocation

import java.util.UUID

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import unit.views.CommonViewSpec

import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apirevocationfrontend.models.{AppAuthorisation, Scope, ThirdPartyApplication}
import uk.gov.hmrc.apirevocationfrontend.views.html.revocation.AuthorizedApplicationsView

class AuthorizedApplicationsSpec extends CommonViewSpec with FixedClock {

  trait Setup {
    val authorizedApplicationsPage = app.injector.instanceOf[AuthorizedApplicationsView]
    private val scopes             = Set(Scope("read:api-1", "scope name", "Access personal information"), Scope("read:api-3", "scope name", "Access tax information"))

    val auth1 = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "app1"), scopes, instant)
    val auth2 = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "app2"), scopes, instant)
    val auth3 = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "app3"), scopes, instant)

    val apps = List(auth1, auth2, auth3)

  }

  "authorized Applications page" should {

    "render page correctly" in new Setup {

      val page: Html         = authorizedApplicationsPage.render(apps, FakeRequest(), messagesProvider.messages, appConfig, footerConfig)
      val document: Document = Jsoup.parse(page.body)
      document.getElementById("page-heading").text() shouldBe "Authorised software applications"
      document.getElementById("app-summary-0").text() shouldBe "app1"
      document.getElementById("app-summary-1").text() shouldBe "app2"
      document.getElementById("app-summary-2").text() shouldBe "app3"
    }
  }

}
