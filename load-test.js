import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 20 },
        { duration: '10s', target: 50 },
        { duration: '5s', target: 0 },
    ],
};

export default function () {
  
    http.get('http://localhost:8080/api/v1/flights/query?from=IST&to=JFK&dateFrom=2026-03-21T10:00:00&page=0');
    sleep(1);
}
