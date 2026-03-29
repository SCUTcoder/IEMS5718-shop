window.__SHOP_API_BASE_URL = (window.location.protocol === 'file:' ||
                     window.location.hostname === 'localhost' ||
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '')
    ? 'http://localhost:8080/api'
    : `${window.location.origin}/api`;

// ── Local auth helpers (used when backend auth is unavailable) ──────────────

const LOCAL_USERS_KEY  = 'iems5718_local_users';
const LOCAL_SESSION_KEY = 'iems5718_local_session';

function getLocalUsers() {
    try { return JSON.parse(localStorage.getItem(LOCAL_USERS_KEY)) || []; }
    catch { return []; }
}

function saveLocalUsers(users) {
    localStorage.setItem(LOCAL_USERS_KEY, JSON.stringify(users));
}

function getLocalSession() {
    try { return JSON.parse(localStorage.getItem(LOCAL_SESSION_KEY)) || null; }
    catch { return null; }
}

function setLocalSession(user) {
    localStorage.setItem(LOCAL_SESSION_KEY, JSON.stringify(user));
}

function clearLocalSession() {
    localStorage.removeItem(LOCAL_SESSION_KEY);
}

/** Register a user locally. Returns {ok, message}. */
function localRegister(displayName, email, password) {
    const users = getLocalUsers();
    if (users.find(u => u.email.toLowerCase() === email.toLowerCase())) {
        return { ok: false, message: 'Email is already registered.' };
    }
    users.push({ displayName, email: email.toLowerCase(), password });
    saveLocalUsers(users);
    setLocalSession({ displayName, email: email.toLowerCase(), authenticated: true, admin: false });
    return { ok: true };
}

/** Login a user locally. Returns {ok, user?, message}. */
function localLogin(email, password) {
    const users = getLocalUsers();
    const user = users.find(u => u.email.toLowerCase() === email.toLowerCase() && u.password === password);
    if (!user) {
        return { ok: false, message: 'Incorrect email or password.' };
    }
    const session = { displayName: user.displayName, email: user.email, authenticated: true, admin: false };
    setLocalSession(session);
    return { ok: true, user: session };
}

function readCookie(name) {
    const prefix = `${name}=`;
    return document.cookie.split('; ')
        .find(entry => entry.startsWith(prefix))
        ?.substring(prefix.length) || '';
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

async function ensureCsrfToken() {
    const existing = readCookie('iems5718_csrf');
    if (existing) {
        return existing;
    }

    const response = await fetch(`${window.__SHOP_API_BASE_URL}/auth/csrf`, {
        method: 'GET',
        credentials: 'include'
    });

    if (!response.ok) {
        throw new Error('Unable to initialize CSRF token');
    }

    const body = await response.json();
    return body.csrfToken;
}

async function authFetch(url, options = {}) {
    const config = { ...options, credentials: 'include' };
    const method = (config.method || 'GET').toUpperCase();

    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
        const csrfToken = await ensureCsrfToken();
        config.headers = {
            ...(config.headers || {}),
            'X-CSRF-Token': csrfToken
        };
        if (config.body instanceof FormData) {
            config.body.set('_csrf', csrfToken);
        }
    }

    return fetch(url, config);
}

function showNotification(message, type = 'success') {
    const el = document.createElement('div');
    el.className = `notification ${type} slide-in`;
    el.textContent = message;
    document.body.appendChild(el);
    setTimeout(() => {
        el.classList.remove('slide-in');
        el.classList.add('slide-out');
        setTimeout(() => el.remove(), 300);
    }, 2500);
}

async function loadCurrentUser() {
    try {
        const response = await authFetch(`${window.__SHOP_API_BASE_URL}/auth/me`);
        if (!response.ok) {
            return getLocalSession() || { authenticated: false, displayName: 'Guest', admin: false };
        }
        return await response.json();
    } catch (error) {
        return getLocalSession() || { authenticated: false, displayName: 'Guest', admin: false };
    }
}

function renderHeaderAuth(user) {
    const userName = document.getElementById('header-user-name');
    const adminLink = document.getElementById('admin-link');
    const loginLink = document.getElementById('login-link');
    const registerLink = document.getElementById('register-link');
    const logoutBtn = document.getElementById('logout-btn');
    const changePasswordLink = document.getElementById('change-password-link');

    if (userName) {
        userName.innerHTML = user.authenticated
            ? `<small>Signed in as</small> ${escapeHtml(user.displayName)}`
            : '<small>Signed in as</small> Guest';
    }

    if (adminLink) {
        adminLink.style.display = user.admin ? 'inline-flex' : 'none';
    }
    if (loginLink) {
        loginLink.style.display = user.authenticated ? 'none' : 'inline-flex';
    }
    if (registerLink) {
        registerLink.style.display = user.authenticated ? 'none' : 'inline-flex';
    }
    if (logoutBtn) {
        logoutBtn.style.display = user.authenticated ? 'inline-flex' : 'none';
    }
    if (changePasswordLink) {
        changePasswordLink.style.display = user.authenticated ? 'inline-flex' : 'none';
    }
}

async function initializeHeaderAuth() {
    await ensureCsrfToken();
    const user = await loadCurrentUser();
    renderHeaderAuth(user);

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            try {
                const response = await authFetch(`${window.__SHOP_API_BASE_URL}/auth/logout`, { method: 'POST' });
                if (!response.ok) throw new Error('backend logout failed');
            } catch {
                clearLocalSession();
            }
            showNotification('Logged out successfully');
            window.location.href = 'index.html';
        });
    }

    return user;
}

window.shopAuth = {
    API_BASE_URL: window.__SHOP_API_BASE_URL,
    authFetch,
    ensureCsrfToken,
    loadCurrentUser,
    initializeHeaderAuth,
    showNotification,
    escapeHtml,
    readCookie,
    localRegister,
    localLogin,
    clearLocalSession,
    getLocalSession
};
