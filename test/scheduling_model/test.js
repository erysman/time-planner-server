import http from 'k6/http';
import {sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

export const options = {
    // A number specifying the number of VUs to run concurrently.
    vus: 1,
    // A string specifying the total duration of the test run.
    iterations: 1
};

const validRate = new Rate('valid_responses');
const totalScheduledTasks = new Trend(`total_scheduled_tasks_percent`);
const scheduledTasks_t5 = new Trend(`scheduled_tasks_percent_t5`);
const scheduledTasks_t10 = new Trend(`scheduled_tasks_percent_t10`);
const scheduledTasks_t15 = new Trend(`scheduled_tasks_percent_t15`);
const scheduledTasks_t20 = new Trend(`scheduled_tasks_percent_t20`);
const scheduledTasks_t25 = new Trend(`scheduled_tasks_percent_t25`);
const scheduledTasks_p1 = new Trend(`scheduled_tasks_percent_p1`);
const scheduledTasks_p3 = new Trend(`scheduled_tasks_percent_p3`);
const scheduledTasks_b0 = new Trend(`scheduled_tasks_percent_b0`);
const scheduledTasks_b2 = new Trend(`scheduled_tasks_percent_b2`);
const totalWaitingTime = new Trend('total_waiting_time');
const waitingTime_t5 = new Trend('waiting_time_t5');
const waitingTime_t10 = new Trend('waiting_time_t10');
const waitingTime_t15 = new Trend('waiting_time_t15');
const waitingTime_t20 = new Trend('waiting_time_t20');
const waitingTime_t25 = new Trend('waiting_time_t25');
const waitingTime_p1 = new Trend('waiting_time_p1');
const waitingTime_p3 = new Trend('waiting_time_p3');
const waitingTime_b0 = new Trend('waiting_time_b0');
const waitingTime_b2 = new Trend('waiting_time_b2');
const totalScore = new Trend('total_score');
const score_t5 = new Trend('score_t5');
const score_t10 = new Trend('score_t10');
const score_t15 = new Trend('score_t15');
const score_t20 = new Trend('score_t20');
const score_t25 = new Trend('score_t25');
const score_p1 = new Trend('score_p1');
const score_p3 = new Trend('score_p3');
const score_b0 = new Trend('score_b0');
const score_b2 = new Trend('score_b2');

function testCase(name) {
    const tIndex = name.indexOf('t') + 1; // Start after 't'
    const pIndex = name.indexOf('p'); // End before 'p'
    const bIndex = name.indexOf('b');

    const tasksCount = parseInt(name.substring(tIndex, pIndex), 10);
    const projectsCount = parseInt(name.substring(pIndex + 1, bIndex), 10);
    const bannedRangesCount = parseInt(name.substring(bIndex + 1), 10);

    const payload = open(`./hard_data/${name}.json`);
    const waitTime = new Trend(`waiting_time_ms_${name}`);
    const scheduledTasks = new Trend(`scheduled_tasks_percent_${name}`);
    return {name, payload, waitTime, scheduledTasks, tasksCount, projectsCount, bannedRangesCount};
}

const t5p1b0 = testCase('t5p1b0');
const t10p1b0 = testCase('t10p1b0');
const t15p1b0 = testCase('t15p1b0');
const t20p1b0 = testCase('t20p1b0');
const t25p1b0 = testCase('t25p1b0');
const t5p3b0 = testCase('t5p3b0');
const t10p3b0 = testCase('t10p3b0');
const t15p3b0 = testCase('t15p3b0');
const t20p3b0 = testCase('t20p3b0');
const t25p3b0 = testCase('t25p3b0');

const t5p1b2 = testCase('t5p1b2');
const t10p1b2 = testCase('t10p1b2');
const t15p1b2 = testCase('t15p1b2');
const t20p1b2 = testCase('t20p1b2');
const t25p1b2 = testCase('t25p1b2');
const t5p3b2 = testCase('t5p3b2');
const t10p3b2 = testCase('t10p3b2');
const t15p3b2 = testCase('t15p3b2');
const t20p3b2 = testCase('t20p3b2');
const t25p3b2 = testCase('t25p3b2');

function areScheduledTasksValid(res, testCase) {
    const {body} = res;
    let isResponseValid = res.status === 200 && res.body.length > 0;
    if(!isResponseValid) {
        return {isResponseValid: false};
    }
    let response;
    try {
        response = JSON.parse(body);
    } catch (e) {
        console.error("Error parsing JSON:", e);
        return {isResponseValid: false};
    }
    console.log(response);
    if (!response || !response.scheduledTasks || !Array.isArray(response.scheduledTasks)) {
        return {isResponseValid: false};
    }
    const scheduledTasksRate = response.scheduledTasks.length/testCase.tasksCount*100;
    testCase.scheduledTasks.add(scheduledTasksRate);
    totalScheduledTasks.add(scheduledTasksRate);
    switch(testCase.tasksCount) {
        case 5:
            scheduledTasks_t5.add(scheduledTasksRate);
            break;
        case 10:
            scheduledTasks_t10.add(scheduledTasksRate);
            break;
        case 15:
            scheduledTasks_t15.add(scheduledTasksRate);
            break;
        case 20:
            scheduledTasks_t20.add(scheduledTasksRate);
            break;
        case 25:
            scheduledTasks_t25.add(scheduledTasksRate);
            break;
    }
    switch (testCase.projectsCount) {
        case 1:
            scheduledTasks_p1.add(scheduledTasksRate);
            break;
        case 3:
            scheduledTasks_p3.add(scheduledTasksRate);
            break;
    }
    switch (testCase.bannedRangesCount) {
        case 0:
            scheduledTasks_b0.add(scheduledTasksRate);
            break;
        case 2:
            scheduledTasks_b2.add(scheduledTasksRate);
            break;
    }
    return {isResponseValid: response.scheduledTasks.length > 0 && response.score > 0, score: response.score || 0};
}


function runTestCase(testCase) {
    const headers = {headers: {'Content-Type': 'application/json'}};
    const res = http.post('http://localhost:8082/v1/scheduleTasks', testCase.payload, headers);
    // console.log(res.body);
    let {isResponseValid, score} = areScheduledTasksValid(res, testCase);
    validRate.add(isResponseValid);
    if (isResponseValid) {

    }
    testCase.waitTime.add(res.timings.waiting);
    totalWaitingTime.add(res.timings.waiting);
    switch(testCase.tasksCount) {
        case 5:
            waitingTime_t5.add(res.timings.waiting);
            break;
        case 10:
            waitingTime_t10.add(res.timings.waiting);
            break;
        case 15:
            waitingTime_t15.add(res.timings.waiting);
            break;
        case 20:
            waitingTime_t20.add(res.timings.waiting);
            break;
        case 25:
            waitingTime_t25.add(res.timings.waiting);
            break;
    }
    switch (testCase.projectsCount) {
        case 1:
            waitingTime_p1.add(res.timings.waiting);
            break;
        case 3:
            waitingTime_p3.add(res.timings.waiting);
            break;
    }
    switch (testCase.bannedRangesCount) {
        case 0:
            waitingTime_b0.add(res.timings.waiting);
            break;
        case 2:
            waitingTime_b2.add(res.timings.waiting);
            break;
    }
    totalScore.add(score);
    switch(testCase.tasksCount) {
        case 5:
            score_t5.add(score);
            break;
        case 10:
            score_t10.add(score);
            break;
        case 15:
            score_t15.add(score);
            break;
        case 20:
            score_t20.add(score);
            break;
        case 25:
            score_t25.add(score);
            break;
    }
    switch (testCase.projectsCount) {
        case 1:
            score_p1.add(score);
            break;
        case 3:
            score_p3.add(score);
            break;
    }
    switch (testCase.bannedRangesCount) {
        case 0:
            score_b0.add(score);
            break;
        case 2:
            score_b2.add(score);
            break;
    }
    sleep(1);
}

export default function () {
    runTestCase(t5p1b0);
    runTestCase(t10p1b0);
    runTestCase(t15p1b0);
    runTestCase(t20p1b0);
    runTestCase(t25p1b0);
    runTestCase(t5p3b0);
    runTestCase(t10p3b0);
    runTestCase(t15p3b0);
    runTestCase(t20p3b0);
    runTestCase(t25p3b0);

    runTestCase(t5p1b2);
    runTestCase(t10p1b2);
    runTestCase(t15p1b2);
    runTestCase(t20p1b2);
    runTestCase(t25p1b2);
    runTestCase(t5p3b2);
    runTestCase(t10p3b2);
    runTestCase(t15p3b2);
    runTestCase(t20p3b2);
    runTestCase(t25p3b2);
}
