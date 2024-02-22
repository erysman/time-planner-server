package integration

import com.pw.timeplanner.TimePlannerApplication
import com.pw.timeplanner.config.MySQLInitializer
import com.pw.timeplanner.feature.user.controller.UserController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest(classes = TimePlannerApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = MySQLInitializer.class)
@ActiveProfiles("test")
class LoadContextTest extends Specification {

    @Autowired(required = false)
    private UserController userController;

    def "when context is loaded then all expected beans are created"() {
        expect: "the WebController is created"
            userController
    }
}