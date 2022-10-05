package unit.views.revocation

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.twirl.api.Html
import unit.views.CommonViewSpec

import uk.gov.hmrc.apirevocationfrontend.views.html.revocation.StartView

class StartSpec extends CommonViewSpec {

  trait Setup {
    val start = app.injector.instanceOf[StartView]
  }

  "start page" should {

    "render correctly" in new Setup {
      val page: Html         = start.render(FakeRequest(), messagesProvider.messages, appConfig, footerConfig)
      val document: Document = Jsoup.parse(page.body)

      document.getElementById("page-heading").text() shouldBe "Manage the authority you have granted to software applications"
    }
  }

}
