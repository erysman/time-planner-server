import http from 'k6/http';
import {check, group, sleep} from "k6";
import {vu} from 'k6/execution';

const AUTH_API_KEY = "" //SET API KEY TO FIREBASE!
const AUTH_BASE_URL = 'https://identitytoolkit.googleapis.com/v1/accounts';
const key = `key=${AUTH_API_KEY}`;
const AUTH_HEADERS = {headers: {'Content-Type': 'application/json'}};
const password = '123456';
export const BASE_URL = "http://localhost:8080";
export const SLEEP_DURATION = 1;

export function createUser(id) {
    const email = `user${id}@email.com`;
    const body = {
        email: email,
        password: password,
        returnSecureToken: true
    };
    const url = `${AUTH_BASE_URL}:signUp?`;
    const res = http.post(url + key, JSON.stringify(body), AUTH_HEADERS);
    if (res.status !== 200) {
        const response = JSON.parse(res.body);
        if (response.error.message === "EMAIL_EXISTS") {
            return loginUser(id);
        }
        throw new Error(`Error creating user ${id}: ${res.status} ${res.body}`);
    }
    try {
        const response = JSON.parse(res.body);
        return {token: response.idToken, email, userId: response.localId}
    } catch (e) {
        console.error("Error parsing JSON:", e);
        throw new Error(`Error parsing JSON while creating user ${id}: ${res.status} ${res.body}`);
    }
}

export function loginUser(id) {
    const email = `user${id}@email.com`;
    const body = {
        email: email,
        password: password,
        returnSecureToken: true
    };
    const url = `${AUTH_BASE_URL}:signInWithPassword?`;
    const res = http.post(url + key, JSON.stringify(body), AUTH_HEADERS);
    if (res.status !== 200) {
        throw new Error(`Error logging user ${id}: ${res.status} ${res.body}`);
    }
    try {
        const response = JSON.parse(res.body);
        return {token: response.idToken, email, userId: response.localId}
    } catch (e) {
        console.error("Error parsing JSON:", e);
        throw new Error(`Error parsing JSON while logging user ${id}: ${res.status} ${res.body}`);
    }
}

export function createProject(token, iteration) {
    let url = BASE_URL + `/projects`;
    // TODO: edit the parameters of the request body.
    let body = {"name": `project${iteration}`, "color": "#eb4034"};
    let params = {headers: {"Content-Type": "application/json", "Accept": "*/*", "Authorization": `Bearer ${token}`}};
    let res = http.post(url, JSON.stringify(body), params);

    check(res, {
        "OK": (r) => r.status === 200
    });
    if (res.status !== 200) {
        return null;
    }
    try {
        const response = JSON.parse(res.body);
        return response;
    } catch (e) {
        console.error("Error parsing JSON:", e);
    }
}

export function deleteProject(token, id) {
    let url = BASE_URL + `/projects/${id}`;
    let params = {headers: {"Authorization": `Bearer ${token}`}};
    let res = http.del(url, null, params);

    check(res, {
        "OK": (r) => r.status === 200
    });
}

export function getProjects(token) {
    let url = BASE_URL + `/projects`;
    let res = http.get(url, {headers: {"Authorization": `Bearer ${token}`}});

    check(res, {
        "OK": (r) => r.status === 200
    });
    if (res.status !== 200) {
        return null;
    }
    try {
        const response = JSON.parse(res.body);
        return response;
    } catch (e) {
        console.error("Error parsing JSON:", e);
    }
}

export function createTask(token, iteration, day, time, projectId, isImportant = false, isUrgent = false) {
    let url = BASE_URL + `/tasks`;
    // TODO: edit the parameters of the request body.
    let body = {
        "name": `task${iteration}`,
        "startDay": day,
        "startTime": time,
        "durationMin": 60,
        "isImportant": isImportant,
        "isUrgent": isUrgent,
        "projectId": projectId
    };
    let params = {headers: {"Content-Type": "application/json", "Accept": "*/*", Authorization: `Bearer ${token}`}};
    let request = http.post(url, JSON.stringify(body), params);

    check(request, {
        "OK": (r) => r.status === 200
    });
    if (request.status !== 200) {
        return null;
    }
    try {
        const response = JSON.parse(request.body);
        return response;
    } catch (e) {
        console.error("Error parsing JSON:", e);
    }
}

export function updateTask(token, id, name, startDay, startTime, durationMin, isImportant, isUrgent, projectId) {

        let url = BASE_URL + `/tasks/${id}`;
        let body = {};
        if(name) {
            body.name = name;
        }
        if(startDay) {
            body.startDay = startDay;
        }
        if(startTime) {
            body.startTime = startTime;
        }
        if(durationMin) {
            body.durationMin = durationMin;
        }
        if(isImportant) {
            body.isImportant = isImportant;
        }
        if(isUrgent) {
            body.isUrgent = isUrgent;
        }
        if(projectId) {
            body.projectId = projectId;
        }


        let params = {headers: {"Content-Type": "application/json", "Accept": "*/*", "Authorization": `Bearer ${token}`}};
        let request = http.patch(url, JSON.stringify(body), params);

        check(request, {
            "OK": (r) => r.status === 200
        });

}

export function getDayTasks(token, day) {
    let url = BASE_URL + `/day/${day}/tasks`;
    let request = http.get(url, {headers: {"Authorization": `Bearer ${token}`}});

    check(request, {
        "OK": (r) => r.status === 200
    });
}

export function getTasksDayOrder(token, day) {
    let url = BASE_URL + `/day/${day}/tasks/order`;
    let res = http.get(url, {headers: {"Authorization": `Bearer ${token}`}});

    check(res, {
        "OK": (r) => r.status === 200
    });
    if (res.status !== 200) {
        return null;
    }
    try {
        const response = JSON.parse(res.body);
        return response;
    } catch (e) {
        console.error("Error parsing JSON:", e);
    }
}

export function updateTasksDayOrder(token, day, order) {
    let url = BASE_URL + `/day/${day}/tasks/order`;
    let params = {headers: {"Content-Type": "application/json", "Accept": "*/*", "Authorization": `Bearer ${token}`}};
    let request = http.put(url, JSON.stringify(order), params);

    check(request, {
        "OK": (r) => r.status === 200
    });
}

export function swapTwoRandomElements(array) {
    // Ensure the array has at least two elements to swap
    if (array.length < 2) {
        console.log("Array needs to have at least two elements to perform a swap.");
        return array;
    }

    // Generate two distinct random indices in the array
    let i = Math.floor(Math.random() * array.length);
    let j = Math.floor(Math.random() * array.length);
    while (i === j) {
        j = Math.floor(Math.random() * array.length); // Ensure i and j are different
    }
    // Swap the elements at indices i and j
    [array[i], array[j]] = [array[j], array[i]];
    return array;
}

export function initUser(id, day) {
    const projectsNumber = 5;
    const tasksNumber = 5;
    const tasksToScheduleNumber = 1;
    const user = createUser(id);
    if(getProjects(user.token).length === 0) {
        for (let i = 0; i < projectsNumber; i++) {
            const project = createProject(user.token, i);
            // console.log(project);
        }
    }
    const projects = getProjects(user.token);
    for (let i = 0; i < projects.length; i++) {
        console.log(projects[i]);
        for (let j = 0; j < tasksNumber; j++) {
            createTask(user.token, j, day, `0${j}:00`, projects[i].id, false, false);
        }
        for (let j = 0; j < tasksToScheduleNumber; j++) {
            createTask(user.token, j, day, null, projects[i].id, true, false);
        }
    }
    return user;
}


export function userSimulationNoSchedule(localData, day, iteration) {
    let projectId = null;
    group("/projects", () => {
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
        {
            let project = createProject(localData.token, Math.random()*10000);
            projectId = project.id;
            sleep(SLEEP_DURATION);
        }
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
    });

    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
        {
            const task = createTask(localData.token, iteration, day, "08:00", projectId, false, false);
            sleep(SLEEP_DURATION);
            updateTask(localData.token, task.id, "different");
        }
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
        {
            const task = createTask(localData.token, iteration, day, null, projectId, false, false);
            sleep(SLEEP_DURATION);
            updateTask(localData.token, task.id, null, null, "14:00");
        }
    });

    group("/projects", () => {
        {
            deleteProject(localData.token, projectId);
            sleep(SLEEP_DURATION);
        }
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
        {
            let project = createProject(localData.token, Math.random()*10000);
            projectId = project.id;
            sleep(SLEEP_DURATION);
        }
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
        {
            deleteProject(localData.token, projectId);
            sleep(SLEEP_DURATION);
        }
    });


    group("/day/{day}/tasks/order", () => {
        getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        // order.sort(() => Math.random() - 0.5);
        updateTasksDayOrder(localData.token, day, swapTwoRandomElements(order));

    });

    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
    });

    group("/day/{day}/tasks/order", () => {
        getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        // order.sort(() => Math.random() - 0.5);
        updateTasksDayOrder(localData.token, day, swapTwoRandomElements(order));

    });
    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
    });

    group("/day/{day}/tasks/order", () => {
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
    });
}


function schedule(day, localData) {
    group("/day/{day}/tasks/schedule", () => {
        // Request No. 1: getAutoScheduleInfo
        {
            let url = BASE_URL + `/day/${day}/tasks/schedule`;
            let request = http.get(url, {headers: {"Authorization": `Bearer ${localData.token}`}});

            check(request, {
                "OK": (r) => r.status === 200
            });

            sleep(1);
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
        {
            let url = BASE_URL + `/day/${day}/tasks/schedule`;
            let request = http.del(url, null, {headers: {"Authorization": `Bearer ${localData.token}`}});

            check(request, {
                "OK": (r) => r.status === 200
            });
        }
    });

}

export function userSimulationWithSchedule(localData, day, iteration) {
    let projectId = null;

    const scheduleMoment = Math.floor(Math.random()*5);

    if(scheduleMoment === 0) {
        schedule(day, localData);
    }
    group("/projects", () => {
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
        {
            let project = createProject(localData.token, Math.random()*10000);
            projectId = project.id;
            sleep(SLEEP_DURATION);
        }
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
    });

    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
        {
            const task = createTask(localData.token, iteration, day, "08:00", projectId, false, false);
            sleep(SLEEP_DURATION);
            updateTask(localData.token, task.id, "different");
        }
    })
    if(scheduleMoment === 1) {
        schedule(day, localData);
    }
    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
        {
            const task = createTask(localData.token, iteration, day, null, projectId, false, false);
            sleep(SLEEP_DURATION);
            updateTask(localData.token, task.id, null, null, "14:00");
        }
    });

    group("/projects", () => {
        {
            deleteProject(localData.token, projectId);
            sleep(SLEEP_DURATION);
        }
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
    });
    if(scheduleMoment === 2) {
        schedule(day, localData);
    }
    group("/projects", () => {
        {
            let project = createProject(localData.token, Math.random()*10000);
            projectId = project.id;
            sleep(SLEEP_DURATION);
        }
        {
            getProjects(localData.token)
            sleep(SLEEP_DURATION);
        }
        {
            deleteProject(localData.token, projectId);
            sleep(SLEEP_DURATION);
        }
    });


    group("/day/{day}/tasks/order", () => {
        getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        // order.sort(() => Math.random() - 0.5);
        updateTasksDayOrder(localData.token, day, swapTwoRandomElements(order));

    });
    if(scheduleMoment === 3) {
        schedule(day, localData);
    }

    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
    });

    group("/day/{day}/tasks/order", () => {
        getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
        // order.sort(() => Math.random() - 0.5);
        updateTasksDayOrder(localData.token, day, swapTwoRandomElements(order));

    });
    group("/tasks", () => {
        {
            getDayTasks(localData.token, day);
            sleep(SLEEP_DURATION);
        }
    });

    group("/day/{day}/tasks/order", () => {
        const order = getTasksDayOrder(localData.token, day);
        sleep(SLEEP_DURATION);
    });
    if(scheduleMoment === 4) {
        schedule(day, localData);
    }
}