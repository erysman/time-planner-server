package integration

import com.pw.timeplanner.TimePlannerApplication
import com.pw.timeplanner.config.MySQLInitializer
import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository
import com.pw.timeplanner.feature.user.api.UserResource
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = TimePlannerApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = MySQLInitializer.class)
@ActiveProfiles("test")
@EnableSharedInjection
abstract class UserInitializedSpecification extends Specification {
    @Shared
    static final String USER_ID = "user1"

    @Autowired
    @Shared MockMvc mockMvc

    @Autowired
    @Shared private ProjectsRepository projectsRepository

    @Autowired
    @Shared TasksProperties tasksProperties

    @Shared ProjectEntity defaultProject

    def setupSpec() {
        def response = mockMvc.perform(post(UserResource.RESOURCE_PATH + "/initialize")
                .with(jwt().jwt {it.claim("user_id", USER_ID)}))
        assert response.andExpect(status().isOk())
        defaultProject = projectsRepository.findOneByUserIdAndName(USER_ID, tasksProperties.getDefaultProjectName()).orElseThrow()
    }

    def cleanupSpec() {
        projectsRepository.deleteAll()
    }

    def userIdJwt() {
        jwt().jwt {it.claim("user_id", USER_ID)}
    }
}
