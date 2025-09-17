const SHELL = 'shell-v1';
const APP_SHELL = ['/', '/index.html', '/pacientes.html', '/app.js', '/manifest.webmanifest'];

self.addEventListener('install', e => {
    e.waitUntil(caches.open(SHELL).then(c => c.addAll(APP_SHELL)));
    self.skipWaiting();
});
self.addEventListener('activate', e => { self.clients.claim(); });

self.addEventListener('fetch', event => {
    const req = event.request;
    const isApi = req.url.includes('/api/');
    // UI: cache-first
    if (!isApi && req.method==='GET') {
        event.respondWith((async ()=>{
            const cached = await caches.match(req);
            if (cached) return cached;
            try {
                const res = await fetch(req);
                const cache = await caches.open(SHELL);
                cache.put(req, res.clone());
                return res;
            } catch {
                return caches.match('/index.html');
            }
        })());
    }
    // Las APIs siguen directo a red en este paso.
});
