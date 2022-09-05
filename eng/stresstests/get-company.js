import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 2000,
  duration: '600s',
};

export default function () {
  const url = 'http://localhost:5050/api/v1/companies/current';

  const params = {
    headers: {
      'x-tenant': '01187cd0-3f54-4761-9d60-e26a005007a7',
      'x-user-id': 'bce4aab7-05c0-46a4-a0e9-baa0b4f1f35a',
      'x-email': 'Newton55@gmail.com'
    },
  };

  http.get(url, params);
  sleep(1);
}
