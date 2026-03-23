window.__SHOP_API_BASE_URL = (window.location.protocol === 'file:' ||
                     window.location.hostname === 'localhost' ||
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '')
    ? 'http://localhost:8080/api'
    : `${window.location.origin}/api`;

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
            return { authenticated: false, displayName: 'Guest', admin: false };
        }
        return await response.json();
    } catch (error) {
        return { authenticated: false, displayName: 'Guest', admin: false };
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
            const response = await authFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST' });
            if (response.ok) {
                showNotification('Logged out successfully');
                window.location.href = 'index.html';
            } else {
                showNotification('Logout failed', 'error');
            }
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
    readCookie
};
