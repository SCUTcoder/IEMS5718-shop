const API_BASE_URL = window.shopAuth ? window.shopAuth.API_BASE_URL : (
    (window.location.protocol === 'file:' ||
     window.location.hostname === 'localhost' ||
     window.location.hostname === '127.0.0.1' ||
     window.location.hostname === '')
        ? 'http://localhost:8080/api'
        : window.location.origin + '/api'
);

let productsData = {};
let cart = [];

document.addEventListener('DOMContentLoaded', async function() {
    if (window.shopAuth) {
        await window.shopAuth.initializeHeaderAuth();
    }
    await loadProducts();

    cart = JSON.parse(localStorage.getItem('iems5718-cart')) || [];
    updateCartDisplay();

    if (window.location.pathname.includes('product.html')) {
        setTimeout(() => loadProductDetail(), 100);
    }

    const cartToggle = document.getElementById('cart-toggle');
    if (cartToggle) {
        cartToggle.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) {
                e.preventDefault();
                this.parentElement.classList.toggle('active');
            }
        });
    }

    const checkoutBtn = document.getElementById('checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', () => {
            alert('Checkout functionality coming soon!');
        });
    }
});

async function loadProducts() {
    productsData = {
        '1': { id: 1, pid: 1, name: 'Gaming Laptop', price: 1299.99, imageUrl: 'images/product1.jpg', galleryImageUrls: 'images/product1.jpg,images/product1-2.jpg,images/product1-3.jpg,images/product1-4.jpg', thumbnailUrls: 'images/product1.jpg,images/product1-2.jpg,images/product1-3.jpg,images/product1-4.jpg', description: 'Experience gaming like never before with our high-performance gaming laptop. Featuring the latest Intel Core i7 processor, NVIDIA RTX 4070 graphics card, and 16GB DDR5 RAM.', category: { catid: 1, name: 'Electronics' } },
        '2': { id: 2, pid: 2, name: 'Wireless Headphones', price: 249.99, imageUrl: 'images/product2.jpg', galleryImageUrls: 'images/product2.jpg', thumbnailUrls: 'images/product2.jpg', description: 'Premium wireless headphones with noise cancellation. Crystal-clear audio with advanced active noise cancellation technology.', category: { catid: 1, name: 'Electronics' } },
        '3': { id: 3, pid: 3, name: 'Smart Watch', price: 399.99, imageUrl: 'images/product3.jpg', galleryImageUrls: 'images/product3.jpg', thumbnailUrls: 'images/product3.jpg', description: 'Latest smartwatch with health monitoring features. Track fitness goals, heart rate, sleep patterns, and stay connected.', category: { catid: 1, name: 'Electronics' } },
        '4': { id: 4, pid: 4, name: 'Tablet PC', price: 599.99, imageUrl: 'images/product4.jpg', galleryImageUrls: 'images/product4.jpg', thumbnailUrls: 'images/product4.jpg', description: 'Portable tablet perfect for work and entertainment. Stunning high-resolution display, powerful processor, and all-day battery.', category: { catid: 1, name: 'Electronics' } }
    };

    try {
        const res = await fetch(`${API_BASE_URL}/products`);
        if (!res.ok) throw new Error('Failed to fetch');
        const products = await res.json();
        productsData = {};
        products.forEach(p => {
            const id = p.pid || p.id;
            productsData[id.toString()] = p;
        });
    } catch (e) {
        console.log('Using local fallback data:', e.message);
    }
}

function splitMediaCsv(csv) {
    if (!csv) {
        return [];
    }

    return csv.split(',')
        .map(item => item.trim())
        .filter(Boolean);
}

function getProductImageSets(product) {
    const galleryImages = splitMediaCsv(product.galleryImageUrls);
    const thumbnailImages = splitMediaCsv(product.thumbnailUrls);

    if (galleryImages.length > 1) {
        return {
            images: galleryImages,
            thumbnails: thumbnailImages.length === galleryImages.length ? thumbnailImages : galleryImages
        };
    }

    if (galleryImages.length <= 1 && thumbnailImages.length > galleryImages.length) {
        return {
            images: thumbnailImages,
            thumbnails: thumbnailImages
        };
    }

    if (galleryImages.length === 1) {
        return {
            images: galleryImages,
            thumbnails: thumbnailImages.length === 1 ? thumbnailImages : galleryImages
        };
    }

    if (thumbnailImages.length > 0) {
        return {
            images: thumbnailImages,
            thumbnails: thumbnailImages
        };
    }

    return {
        images: product.imageUrl ? [product.imageUrl] : [],
        thumbnails: product.imageUrl ? [product.imageUrl] : []
    };
}

function buildProductMediaItems(product) {
    const imageSet = getProductImageSets(product);
    const mediaItems = imageSet.images.map((image, index) => ({
        type: 'image',
        src: image,
        thumbnail: imageSet.thumbnails[index] || image,
        alt: `${product.name} ${index + 1}`
    }));

    if (product.videoUrl) {
        mediaItems.push({
            type: 'video',
            src: product.videoUrl,
            thumbnail: imageSet.thumbnails[0] || imageSet.images[0] || '',
            alt: `${product.name} video`
        });
    }

    if (mediaItems.length === 0 && product.videoUrl) {
        mediaItems.push({
            type: 'video',
            src: product.videoUrl,
            thumbnail: '',
            alt: `${product.name} video`
        });
    }

    return mediaItems;
}

function renderMainProductMedia(mediaItem, productName) {
    if (!mediaItem) {
        return '<div class="media-empty-state">No media available for this product.</div>';
    }

    if (mediaItem.type === 'video') {
        const posterAttr = mediaItem.thumbnail ? ` poster="${escapeHtml(mediaItem.thumbnail)}"` : '';
        return `<video controls playsinline preload="metadata"${posterAttr}>
            <source src="${escapeHtml(mediaItem.src)}">
            Your browser does not support the video tag.
        </video>`;
    }

    return `<img src="${escapeHtml(mediaItem.src)}" alt="${escapeHtml(mediaItem.alt || productName)}" id="main-product-image">`;
}

function loadProductDetail() {
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');

    if (!productId) {
        document.getElementById('product-detail').innerHTML = '<div style="text-align:center;padding:60px;grid-column:1/-1;color:var(--text-secondary)"><p>Product not found</p></div>';
        return;
    }

    const product = productsData[productId];
    if (!product) {
        document.getElementById('product-detail').innerHTML = '<div style="text-align:center;padding:60px;grid-column:1/-1;color:var(--text-secondary)"><p>Product not found (ID: ' + productId + ')</p><p><a href="index.html" style="color:var(--primary)">Back to home</a></p></div>';
        return;
    }

    const mediaItems = buildProductMediaItems(product);
    const primaryMedia = mediaItems[0];
    const catName = product.category ? (product.category.name || product.category) : '';
    const pid = product.pid || product.id;

    const breadcrumb = document.getElementById('breadcrumb');
    if (breadcrumb) {
        breadcrumb.innerHTML = `<a href="index.html">Home</a> &gt; ${catName ? `<a href="index.html">${escapeHtml(catName)}</a> &gt; ` : ''}<span>${escapeHtml(product.name)}</span>`;
    }

    document.title = `${product.name} - 章程's Shop`;

    document.getElementById('product-detail').innerHTML = `
        <div class="product-gallery">
            <div class="main-image ${primaryMedia?.type === 'video' ? 'is-video' : ''}" id="main-product-media">
                ${renderMainProductMedia(primaryMedia, product.name)}
            </div>
            ${mediaItems.length > 1 ? `
            <div class="thumbnail-gallery">
                ${mediaItems.map((item, index) => `
                    <button type="button"
                            class="thumbnail ${index === 0 ? 'active' : ''} ${item.type === 'video' ? 'video-thumbnail' : ''}"
                            data-media-type="${item.type}"
                            data-media-src="${escapeHtml(item.src)}"
                            data-media-thumb="${escapeHtml(item.thumbnail || '')}"
                            aria-label="${item.type === 'video' ? 'Show product video' : `Show product image ${index + 1}`}">
                        ${item.thumbnail ? `<img src="${escapeHtml(item.thumbnail)}" alt="${escapeHtml(item.alt)}">` : `<span class="thumbnail-fallback">Video</span>`}
                        ${item.type === 'video' ? '<span class="thumbnail-badge">Video</span>' : ''}
                    </button>
                `).join('')}
            </div>
            ` : ''}
        </div>
        <div class="product-info">
            ${catName ? `<div class="breadcrumb-text" style="font-size:0.8rem;color:var(--primary);text-transform:uppercase;letter-spacing:0.05em;font-weight:600;margin-bottom:8px">${escapeHtml(catName)}</div>` : ''}
            <h1>${escapeHtml(product.name)}</h1>
            <div class="price">$${product.price.toFixed(2)}</div>
            ${product.videoUrl ? '<div class="media-tag">Includes product video</div>' : ''}
            <div class="description">
                <h3>Description</h3>
                <p>${escapeHtml(product.description)}</p>
            </div>
            <div class="add-to-cart-section">
                <button class="btn-add-cart" data-product-id="${pid}" id="detail-add-cart">Add to Cart</button>
                <button class="btn-buy-now">Buy Now</button>
            </div>
        </div>
    `;

    document.querySelectorAll('.thumbnail').forEach(thumb => {
        thumb.addEventListener('click', function() {
            const mediaItem = {
                type: this.dataset.mediaType,
                src: this.dataset.mediaSrc,
                thumbnail: this.dataset.mediaThumb
            };
            const mainMedia = document.getElementById('main-product-media');
            mainMedia.innerHTML = renderMainProductMedia(mediaItem, product.name);
            mainMedia.classList.toggle('is-video', mediaItem.type === 'video');
            document.querySelectorAll('.thumbnail').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
        });
    });

    const addBtn = document.getElementById('detail-add-cart');
    if (addBtn) {
        addBtn.addEventListener('click', function() {
            addToCart(this.dataset.productId, this);
        });
    }
}

function addToCart(productId, buttonEl) {
    const product = productsData[productId.toString()];
    if (!product) return;

    const cartProductId = (product.pid || product.id).toString();
    const existing = cart.find(item => item.id === cartProductId);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({
            id: cartProductId,
            name: product.name,
            price: product.price,
            image: product.imageUrl,
            quantity: 1
        });
    }

    localStorage.setItem('iems5718-cart', JSON.stringify(cart));
    updateCartDisplay();

    if (buttonEl) {
        const origText = buttonEl.textContent;
        buttonEl.textContent = '✓ Added!';
        buttonEl.style.background = 'var(--success)';
        flyToCart(buttonEl);
        setTimeout(() => {
            buttonEl.textContent = origText;
            buttonEl.style.background = '';
        }, 1200);
    }

    showNotification(`${product.name} added to cart!`);
}

function flyToCart(btnEl) {
    const btnRect = btnEl.getBoundingClientRect();
    const cartEl = document.getElementById('cart-count');
    if (!cartEl) return;
    const cartRect = cartEl.getBoundingClientRect();

    const flyEl = document.createElement('div');
    flyEl.className = 'fly-item';
    flyEl.textContent = '🛒';
    flyEl.style.left = btnRect.left + btnRect.width / 2 - 20 + 'px';
    flyEl.style.top = btnRect.top + btnRect.height / 2 - 20 + 'px';

    const dx = cartRect.left - btnRect.left;
    const dy = cartRect.top - btnRect.top;

    flyEl.style.setProperty('--fly-x', dx + 'px');
    flyEl.style.setProperty('--fly-y', dy + 'px');
    flyEl.style.setProperty('--fly-x-mid', dx * 0.4 + 'px');
    flyEl.style.setProperty('--fly-y-mid', (dy - 80) + 'px');
    flyEl.style.animation = 'flyToCart 0.65s cubic-bezier(0.2, 0, 0.38, 0.9) forwards';

    document.body.appendChild(flyEl);
    setTimeout(() => {
        flyEl.remove();
        cartEl.classList.add('bounce');
        setTimeout(() => cartEl.classList.remove('bounce'), 500);
    }, 650);
}

function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
    localStorage.setItem('iems5718-cart', JSON.stringify(cart));
    updateCartDisplay();
}

function updateCartQuantity(productId, newQty) {
    const item = cart.find(item => item.id === productId);
    if (item) {
        item.quantity = parseInt(newQty);
        if (item.quantity <= 0) removeFromCart(productId);
        else {
            localStorage.setItem('iems5718-cart', JSON.stringify(cart));
            updateCartDisplay();
        }
    }
}

function updateCartDisplay() {
    const countEl = document.getElementById('cart-count');
    const itemsEl = document.getElementById('cart-items');
    const checkoutBtn = document.getElementById('checkout-btn');
    if (!countEl || !itemsEl) return;

    const totalItems = cart.reduce((s, i) => s + i.quantity, 0);
    const totalAmount = cart.reduce((s, i) => s + i.price * i.quantity, 0);
    countEl.textContent = totalItems;

    if (cart.length === 0) {
        itemsEl.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
        if (checkoutBtn) checkoutBtn.disabled = true;
    } else {
        itemsEl.innerHTML = cart.map(item => `
            <div class="cart-item">
                <img src="${escapeHtml(item.image)}" alt="${escapeHtml(item.name)}">
                <div class="cart-item-details">
                    <h4>${escapeHtml(item.name)}</h4>
                    <span class="cart-item-price">$${item.price.toFixed(2)} × ${item.quantity}</span>
                    <input type="number" class="cart-item-quantity" value="${item.quantity}" min="1" data-product-id="${item.id}">
                </div>
                <button class="cart-item-remove" data-product-id="${item.id}">×</button>
            </div>
        `).join('') + `
            <div class="cart-total">
                <strong>Total: $${totalAmount.toFixed(2)}</strong>
            </div>
        `;
        if (checkoutBtn) checkoutBtn.disabled = false;

        itemsEl.querySelectorAll('.cart-item-quantity').forEach(input => {
            input.addEventListener('change', function() {
                updateCartQuantity(this.dataset.productId, this.value);
            });
        });

        itemsEl.querySelectorAll('.cart-item-remove').forEach(btn => {
            btn.addEventListener('click', function() {
                removeFromCart(this.dataset.productId);
            });
        });
    }
}

function showNotification(message) {
    if (window.shopAuth) {
        window.shopAuth.showNotification(message);
        return;
    }
    const el = document.createElement('div');
    el.className = 'notification slide-in';
    el.textContent = message;
    document.body.appendChild(el);
    setTimeout(() => {
        el.classList.remove('slide-in');
        el.classList.add('slide-out');
        setTimeout(() => el.remove(), 300);
    }, 2500);
}

function escapeHtml(value) {
    return window.shopAuth ? window.shopAuth.escapeHtml(value) : String(value ?? '');
}


function proceedToCheckout() {
    const session = window.shopAuth ? window.shopAuth.getSession() : JSON.parse(localStorage.getItem('session') || '{}');
    if (!session || !session.email) {
        window.location.href = 'login.html?redirect=checkout.html';
        return;
    }
    window.location.href = 'checkout.html';
}

