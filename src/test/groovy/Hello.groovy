import com.pw.timeplanner.feature.user.controller.UserController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification;

class HelloSpockSpec extends Specification {
    def "length of Spock's and his friends' names"() {
        expect:
            name.size() == length

        where:
            name     | length
            "Spock"  | 5
            "Kirk"   | 4
            "Scotty" | 6
    }
}

@SpringBootTest
class LoadContextTest extends Specification {

    @Autowired (required = false)
    private UserController userController;

    def "when context is loaded then all expected beans are created"() {
        expect: "the WebController is created"
            userController
    }
}