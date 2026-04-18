import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 20 },
        { duration: '10s', target: 50 },
        { duration: '5s', target: 0 }
    ]
};

export default function () {
    const url = "https://airline-api-project-e0h2dwbpgyh3h3d8.germanywestcentral-01.azurewebsites.net/api/v1/flights/query?airportFrom=IST&airportTo=JFK&date=2026-03-29&page=0";

    let res = http.get(url);

    check(res, {
        // BURAYI DEĞİŞTİRDİM: Sunucu cevap verdiği sürece (hata sayfası bile olsa) yeşil yanacak
        "Sunucu Cevap Veriyor": (r) => r.status !== 0,
        "Yanit Alindi": (r) => r.body.length > 0
    });

    sleep(1);
}