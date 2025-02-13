import http from 'k6/http';
import {check, group, sleep} from "k6";
import {vu} from 'k6/execution';
import {
    BASE_URL,
    createProject, createTask, deleteProject, getDayTasks, getProjects, getTasksDayOrder, loginUser, SLEEP_DURATION,
    swapTwoRandomElements,
    updateTasksDayOrder
} from "./common.js";

const VU = 1;

export const options = {
    scenarios: {
        test_name: {
            executor: 'per-vu-iterations',
            vus: VU,
            iterations: 1,
        }
    }
};


export function setup() {
    console.log('setup');
    const users = {}
    for (let i = 1; i <= VU; i++) {
        const user = loginUser(i);
        users[i] = user;
    }
    return {users};
}

export default function (data) {
    const localData = data.users[vu.idInTest]
    console.log('localData', localData)
    const iteration = vu.iterationInInstance;

    // initUser(vu.idInTest);

    let projectId = null;

    group("/projects", () => {

        // Request No. 1: getProjects
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }

        // // Request No. 2: createProject
        {
            let project = createProject(localData.token, Math.random()*10000);
            projectId = project.id;
            sleep(SLEEP_DURATION);
        }
    });

    group("/tasks", () => {
        // Request No. 2: createTask
        {
            createTask(localData.token, iteration, "2024-02-03", "08:00", projectId, false, false);
            sleep(SLEEP_DURATION);
        }
    });

    group("/day/{day}/tasks", () => {
        let day = '2024-02-03'; // specify value as there is no example value for this parameter in OpenAPI spec

        // Request No. 1: getDayTasks
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
    });

    group("/projects", () => {

        // Request No. 1: getProjects
        {
            deleteProject(localData.token, projectId);
            sleep(SLEEP_DURATION);
        }
    });


    group("/day/{day}/tasks/order", () => {
        let day = '2024-02-03'; // specify value as there is no example value for this parameter in OpenAPI spec
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        // order.sort(() => Math.random() - 0.5);
        updateTasksDayOrder(localData.token, day, swapTwoRandomElements(order));

    });

    group("/day/{day}/tasks/schedule", () => {
        let day = '2024-02-03'; // specify value as there is no example value for this parameter in OpenAPI spec

        // Request No. 1: getAutoScheduleInfo
        {
            let url = BASE_URL + `/day/${day}/tasks/schedule`;
            let request = http.get(url, {headers: {"Authorization": `Bearer ${localData.token}`}});

            check(request, {
                "OK": (r) => r.status === 200
            });

            sleep(SLEEP_DURATION);
        }

        // Request No. 2: postAutoScheduleInfo
        {
            let url = BASE_URL + `/day/${day}/tasks/schedule`;
            let request = http.post(url, null, {headers: {"Authorization": `Bearer ${localData.token}`}});

            check(request, {
                "OK": (r) => r.status === 200
            });

            sleep(SLEEP_DURATION);
        }

        // Request No. 3: revokeSchedule
        {
            let url = BASE_URL + `/day/${day}/tasks/schedule`;
            let request = http.del(url, null, {headers: {"Authorization": `Bearer ${localData.token}`}});

            check(request, {
                "OK": (r) => r.status === 200
            });
        }
    });

}
