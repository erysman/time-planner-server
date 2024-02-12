import http from 'k6/http';
import {check, group, sleep} from "k6";
import {vu} from 'k6/execution';

import {initUser} from "./common.js";

const VU = 200;

let day = '2024-02-03';

export const options = {
    scenarios: {
        test_name: {
            executor: 'per-vu-iterations',
            vus: VU,
            iterations: 1,
        }
    }
};


export default function () {
    initUser(vu.idInTest, day);
}
