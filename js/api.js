// ── SENET API Client ──────────────────────────────────────────
// Communicates with the Spring Boot microservices via the API Gateway at :8080
// All fetch calls are async; UI code awaits them and calls showToast() on errors.

const API_BASE = 'http://localhost:8080';

// ── Token helpers ─────────────────────────────────────────────

function getToken()        { return localStorage.getItem('senet_token'); }
function getRefreshToken() { return localStorage.getItem('senet_refresh'); }
function getRole()         { return localStorage.getItem('senet_role'); }
function getUserId()       { return localStorage.getItem('senet_user_id'); }

function saveTokens(data) {
    localStorage.setItem('senet_token',   data.accessToken);
    localStorage.setItem('senet_refresh', data.refreshToken);
    localStorage.setItem('senet_role',    data.role);
    localStorage.setItem('senet_user_id', data.userId);
    if (data.name) localStorage.setItem('senet_name', data.name);
}

function clearTokens() {
    ['senet_token', 'senet_refresh', 'senet_role', 'senet_user_id', 'senet_name'].forEach(k =>
        localStorage.removeItem(k));
}

// ── Core fetch wrapper ────────────────────────────────────────

async function apiFetch(path, options = {}, retry = true) {
    const headers = {
        'Content-Type': 'application/json',
        ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {})
    };

    const res = await fetch(API_BASE + path, { ...options, headers });

    // Auto-refresh on 401
    if (res.status === 401 && retry && getRefreshToken()) {
        const refreshed = await tryRefresh();
        if (refreshed) return apiFetch(path, options, false);
        clearTokens();
        showPage('page-login');
        return null;
    }

    if (!res.ok) {
        const body = await res.text().catch(() => '');
        let message = res.statusText || 'Request failed';
        try { const j = JSON.parse(body); message = j.error || j.message || message; }
        catch { if (body) message = body; }
        throw new Error(message);
    }

    if (res.status === 204) return null;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function tryRefresh() {
    try {
        const data = await apiFetch('/api/auth/refresh', {
            method: 'POST',
            body: JSON.stringify({ refreshToken: getRefreshToken() })
        }, false);
        if (data) { saveTokens(data); return true; }
    } catch (_) {}
    return false;
}

// ── Auth API ──────────────────────────────────────────────────

async function apiLogin(email, password) {
    const data = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });
    saveTokens(data);
    return data;
}

async function apiRegister(email, password, name) {
    const data = await apiFetch('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ email, password, name })
    });
    saveTokens(data);
    return data;
}

async function apiLogout() {
    const refresh = getRefreshToken();
    if (refresh) {
        await apiFetch('/api/auth/logout', {
            method: 'POST',
            body: JSON.stringify({ refreshToken: refresh })
        }).catch(() => {});
    }
    clearTokens();
}

// ── Cars API ──────────────────────────────────────────────────

async function apiGetCars(params = {}) {
    const qs = new URLSearchParams(params).toString();
    return apiFetch('/api/cars' + (qs ? '?' + qs : ''));
}

async function apiGetCar(id) {
    return apiFetch(`/api/cars/${id}`);
}

async function apiCreateCar(data) {
    return apiFetch('/api/cars', { method: 'POST', body: JSON.stringify(data) });
}

async function apiUpdateCar(id, data) {
    return apiFetch(`/api/cars/${id}`, { method: 'PUT', body: JSON.stringify(data) });
}

async function apiDeleteCar(id) {
    return apiFetch(`/api/cars/${id}`, { method: 'DELETE' });
}

// ── Bookings API ──────────────────────────────────────────────

async function apiGetMyBookings() {
    return apiFetch('/api/bookings/my');
}

async function apiGetAllBookings() {
    return apiFetch('/api/bookings');
}

async function apiDeleteBooking(id) {
    return apiFetch(`/api/bookings/${id}`, { method: 'DELETE' });
}

async function apiGetAllUsers() {
    return apiFetch('/api/users');
}

async function apiUpdateUser(id, data) {
    return apiFetch(`/api/users/${id}`, { method: 'PUT', body: JSON.stringify(data) });
}

async function apiDeleteUser(id) {
    return apiFetch(`/api/users/${id}`, { method: 'DELETE' });
}

async function apiCreateBooking(data) {
    return apiFetch('/api/bookings', { method: 'POST', body: JSON.stringify(data) });
}

async function apiUpdateBookingStatus(id, status) {
    return apiFetch(`/api/bookings/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status })
    });
}

// ── Payments API ──────────────────────────────────────────────

async function apiCreatePayment(data) {
    return apiFetch('/api/payments', { method: 'POST', body: JSON.stringify(data) });
}

async function apiGetPaymentForBooking(bookingId) {
    return apiFetch(`/api/payments/booking/${bookingId}`);
}

async function apiGetMyPayments() {
    return apiFetch('/api/payments/my');
}

async function apiGetAllPayments() {
    return apiFetch('/api/payments');
}

// ── Users API ─────────────────────────────────────────────────

async function apiGetMyProfile() {
    return apiFetch('/api/users/me');
}

async function apiUpdateMyProfile(data) {
    return apiFetch('/api/users/me', { method: 'PUT', body: JSON.stringify(data) });
}

async function apiChangePassword(currentPassword, newPassword) {
    return apiFetch('/api/users/me/password', {
        method: 'PATCH',
        body: JSON.stringify({ currentPassword, newPassword })
    });
}
