import controllers.EdgeProxy;
import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                assertThat(browser.pageSource()).contains("Your new application is ready.");
            }
        });
    }

    @Test
    public void testFoundCachedPath() {
        EdgeProxy.PROXIED_HOST = "http://localhost:3333";
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333/time");
                assertThat(browser.pageSource()).contains("{\"name\":\"tipsy\"}");
            }
        });
    }
//    @Test
//    public void testNotFoundCachedPath() {
//        EdgeProxy.PROXIED_HOST = "http://localhost:3333";
//        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
//            public void invoke(TestBrowser browser) {
//                browser.goTo("http://localhost:3333/notfoundwhatsoever");
//                assertThat(browser.pageSource()).contains("ShieldTech");
//        }
//        });
//    }

}
