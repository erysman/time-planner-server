import http from 'k6/http';
import {check, group, sleep} from "k6";
import {vu} from 'k6/execution';

const API_KEY = "AIzaSyB6Y6MGv1hMvHY4Mdx6nFuhDAAGDF_Yo_U"
const AUTH_BASE_URL = 'https://identitytoolkit.googleapis.com/v1/accounts';
const key = `key=${API_KEY}`;
const AUTH_HEADERS = {headers: {'Content-Type': 'application/json'}};
const password = '123456';

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

export function initUser(id) {
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
            createTask(user.token, j, "2024-02-03", `0${j}:00`, projects[i].id, false, false);
        }
        for (let j = 0; j < tasksToScheduleNumber; j++) {
            createTask(user.token, j, "2024-02-03", null, projects[i].id, true, false);
        }
    }
    return user;
}

export const BASE_URL = "http://localhost:8080";
// Sleep duration between successive requests.
// You might want to edit the value of this variable or remove calls to the sleep function on the script.
export const SLEEP_DURATION = 0;


