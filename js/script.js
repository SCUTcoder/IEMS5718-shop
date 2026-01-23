// 动态获取API地址：开发环境用8080，生产环境用当前域名/api
const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080/api' 
    : window.location.origin + '/api';

let productsData = {};
let cart = [];

console.log('API Base URL:', API_BASE_URL);

document.addEventListener('DOMContentLoaded', async function() {
    await loadProducts();
    
    cart = JSON.parse(localStorage.getItem('iems5718-cart')) || [];
    updateCartDisplay();

    if (window.location.pathname.includes('product.html')) {
        console.log('Product page detected, loading detail...');
        setTimeout(() => {
            loadProductDetail();
        }, 100);
    }

    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            addToCart(productId);
        });
    });

    document.querySelectorAll('.thumbnail').forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const mainImage = document.getElementById('main-product-image');
            if (mainImage) {
                mainImage.src = this.getAttribute('data-image');
                document.querySelectorAll('.thumbnail').forEach(thumb => {
                    thumb.classList.remove('active');
                });
                this.classList.add('active');
            }
        });
    });

    const cartIcon = document.querySelector('.cart-icon');
    if (cartIcon) {
        cartIcon.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) {
                e.preventDefault();
                this.parentElement.classList.toggle('active');
            }
        });
    }

    const checkoutBtn = document.querySelector('.checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', function() {
            alert('Checkout functionality will be implemented in Phase 2!');
        });
    }
});

async function loadProducts() {
    productsData = {
        '1': { 
            id: 1, 
            name: 'Gaming Laptop', 
            price: 1299.99, 
            imageUrl: 'images/product1.jpg',
            thumbnailUrls: 'images/product1.jpg,images/product1-2.jpg,images/product1-3.jpg,images/product1-4.jpg',
            description: 'Experience gaming like never before with our high-performance gaming laptop. Featuring the latest Intel Core i7 processor, NVIDIA RTX 4070 graphics card, and 16GB DDR5 RAM, this powerhouse delivers exceptional performance for gaming, content creation, and multitasking.',
            category: 'Electronics'
        },
        '2': { 
            id: 2, 
            name: 'Wireless Headphones', 
            price: 249.99, 
            imageUrl: 'images/product2.jpg',
            thumbnailUrls: 'images/product2.jpg',
            description: 'Premium wireless headphones with noise cancellation. Immerse yourself in crystal-clear audio with advanced active noise cancellation technology. Perfect for music lovers, commuters, and professionals.',
            category: 'Electronics'
        },
        '3': { 
            id: 3, 
            name: 'Smart Watch', 
            price: 399.99, 
            imageUrl: 'images/product3.jpg',
            thumbnailUrls: 'images/product3.jpg',
            description: 'Latest smartwatch with health monitoring features. Track your fitness goals, monitor your heart rate, sleep patterns, and stay connected with notifications. Water-resistant design perfect for any lifestyle.',
            category: 'Electronics'
        },
        '4': { 
            id: 4, 
            name: 'Tablet PC', 
            price: 599.99, 
            imageUrl: 'images/product4.jpg',
            thumbnailUrls: 'images/product4.jpg',
            description: 'Portable tablet perfect for work and entertainment. Featuring a stunning high-resolution display, powerful processor, and all-day battery life. Whether you\'re working, studying, or relaxing, this tablet adapts to your needs.',
            category: 'Electronics'
        }
    };
    
    console.log('✅ Local products data initialized');

    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error('Failed to fetch products');
        
        const products = await response.json();
        productsData = {};
        products.forEach(product => {
            productsData[product.id.toString()] = product;
        });
        console.log('✅ Products updated from backend:', products.length);
    } catch (error) {
        console.log('ℹ️  Using local data (backend not available):', error.message);
    }
}

function loadProductDetail() {
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    console.log('Loading product detail for ID:', productId);
    console.log('Available products:', Object.keys(productsData));
    console.log('Full productsData:', productsData);
    
    if (!productId) {
        document.getElementById('product-detail').innerHTML = '<div style="text-align: center; padding: 50px;"><p>Product not found - No ID provided</p></div>';
        return;
    }

    const product = productsData[productId];
    if (!product) {
        console.error('Product not found in productsData for ID:', productId);
        console.error('Trying to access productsData["' + productId + '"]');
        document.getElementById('product-detail').innerHTML = '<div style="text-align: center; padding: 50px;"><p>Product not found - ID: ' + productId + '</p><p>Available IDs: ' + Object.keys(productsData).join(', ') + '</p></div>';
        return;
    }
    
    console.log('Found product:', product);

    const thumbnails = product.thumbnailUrls ? product.thumbnailUrls.split(',') : [product.imageUrl];
    const breadcrumb = document.getElementById('breadcrumb');
    if (breadcrumb) {
        breadcrumb.innerHTML = `<a href="index.html">Home</a> &gt; <a href="#">${product.category}</a> &gt; <span>${product.name}</span>`;
    }

    document.title = `${product.name} - IEMS5718 Shop`;

    const detailHTML = `
        <div class="product-gallery">
            <div class="main-image">
                <img src="${thumbnails[0]}" alt="${product.name}" id="main-product-image">
            </div>
            <div class="thumbnail-gallery">
                ${thumbnails.map((img, index) => `
                    <img src="${img}" alt="${product.name} View ${index + 1}" 
                         class="thumbnail ${index === 0 ? 'active' : ''}" 
                         data-image="${img}">
                `).join('')}
            </div>
        </div>
        <div class="product-info">
            <h1>${product.name}</h1>
            <div class="price">$${product.price.toFixed(2)}</div>
            <div class="description">
                <h3>Description</h3>
                <p>${product.description}</p>
            </div>
            <div class="add-to-cart-section">
                <button class="btn add-to-cart" data-product-id="${product.id}">Add to Cart</button>
                <button class="btn buy-now">Buy Now</button>
            </div>
        </div>
    `;

    document.getElementById('product-detail').innerHTML = detailHTML;

    document.querySelectorAll('.thumbnail').forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const mainImage = document.getElementById('main-product-image');
            if (mainImage) {
                mainImage.src = this.getAttribute('data-image');
                document.querySelectorAll('.thumbnail').forEach(thumb => {
                    thumb.classList.remove('active');
                });
                this.classList.add('active');
            }
        });
    });

    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            addToCart(productId);
        });
    });
}

function addToCart(productId) {
    const product = productsData[productId.toString()];
    if (!product) return;

    const cartItem = {
        id: product.id.toString(),
        name: product.name,
        price: product.price,
        image: product.imageUrl,
        quantity: 1
    };

    const existingItem = cart.find(item => item.id === cartItem.id);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push(cartItem);
    }

    localStorage.setItem('iems5718-cart', JSON.stringify(cart));
    updateCartDisplay();
    showNotification(`${product.name} added to cart!`);
}

function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
    localStorage.setItem('iems5718-cart', JSON.stringify(cart));
    updateCartDisplay();
}

function updateCartQuantity(productId, newQuantity) {
    const item = cart.find(item => item.id === productId);
    if (item) {
        item.quantity = parseInt(newQuantity);
        if (item.quantity <= 0) {
            removeFromCart(productId);
        } else {
            localStorage.setItem('iems5718-cart', JSON.stringify(cart));
            updateCartDisplay();
        }
    }
}

function updateCartDisplay() {
    const cartCount = document.querySelector('.cart-count');
    const cartItems = document.querySelector('.cart-items');
    const checkoutBtn = document.querySelector('.checkout-btn');

    if (!cartCount || !cartItems || !checkoutBtn) return;

    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalItems;

    if (cart.length === 0) {
        cartItems.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
        checkoutBtn.disabled = true;
    } else {
        cartItems.innerHTML = cart.map(item => `
            <div class="cart-item">
                <img src="${item.image}" alt="${item.name}">
                <div class="cart-item-details">
                    <h4>${item.name}</h4>
                    <span class="cart-item-price">$${item.price}</span>
                    <input type="number" class="cart-item-quantity" value="${item.quantity}"
                           min="1" data-product-id="${item.id}">
                </div>
                <button class="cart-item-remove" data-product-id="${item.id}">×</button>
            </div>
        `).join('');

        checkoutBtn.disabled = false;

        document.querySelectorAll('.cart-item-quantity').forEach(input => {
            input.addEventListener('change', function() {
                updateCartQuantity(this.getAttribute('data-product-id'), this.value);
            });
        });

        document.querySelectorAll('.cart-item-remove').forEach(button => {
            button.addEventListener('click', function() {
                removeFromCart(this.getAttribute('data-product-id'));
            });
        });
    }
}

function showNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'notification slide-in';
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.remove('slide-in');
        notification.classList.add('slide-out');
        setTimeout(() => document.body.removeChild(notification), 300);
    }, 3000);
}