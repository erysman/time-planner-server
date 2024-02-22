import http from 'k6/http';
import {check, group, sleep} from "k6";
import {vu} from 'k6/execution';
import {
    BASE_URL,
    createProject, createTask, deleteProject, getDayTasks, getProjects, getTasksDayOrder, loginUser,
    swapTwoRandomElements,
    updateTasksDayOrder, userSimulationNoSchedule
} from "../common.js";

const MIN_VU = 10;
const VU1 = 25;
const VU2 = 50;
const VU3 = 75;
const MAX_VU = 100;

const WARM_UP = '20s';
const WORK = '40s';

let day = '2024-02-03'; // specify value as there is no example value for this parameter in OpenAPI spec


export const options = {
    scenarios: {
        contacts: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: WARM_UP, target: MIN_VU },
                { duration: WORK, target: MIN_VU },
                { duration: WARM_UP, target: VU1 },
                { duration: WORK, target: VU1 },
                { duration: WARM_UP, target: VU2 },
                { duration: WORK, target: VU2 },
                { duration: WARM_UP, target: VU3 },
                { duration: WORK, target: VU3 },
                { duration: WARM_UP, target: MAX_VU },
                { duration: WORK, target: MAX_VU },
                { duration: WORK, target: MAX_VU },
            ],
            gracefulRampDown: '0s',
            gracefulStop: '0s',
        },
    }
};


export function setup() {
    console.log('setup');
    const users = {}
    for (let i = 1; i <= MAX_VU; i++) {
        const user = loginUser(i);
        users[i] = user;
    }
    return {users};
}

export default function (data) {
    const localData = data.users[vu.idInTest]
    // console.log('localData', localData)
    const iteration = vu.iterationInInstance;
    userSimulationNoSchedule(localData, day, iteration);
}
