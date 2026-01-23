const API_BASE_URL = 'http://localhost:8080/api';
let productsData = {};

document.addEventListener('DOMContentLoaded', async function() {
    await loadProducts();
    
    let cart = JSON.parse(localStorage.getItem('iems5718-cart')) || [];
    updateCartDisplay();

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
    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error('Failed to fetch products');
        
        const products = await response.json();
        products.forEach(product => {
            productsData[product.id.toString()] = {
                id: product.id.toString(),
                name: product.name,
                price: product.price,
                image: product.imageUrl
            };
        });
        console.log('✅ Products loaded:', products.length);
    } catch (error) {
        console.error('⚠️  Using fallback data:', error);
        productsData = {
            '1': { id: '1', name: 'Gaming Laptop', price: 1299.99, image: 'images/product1.jpg' },
            '2': { id: '2', name: 'Wireless Headphones', price: 249.99, image: 'images/product2.jpg' },
            '3': { id: '3', name: 'Smart Watch', price: 399.99, image: 'images/product3.jpg' },
            '4': { id: '4', name: 'Tablet PC', price: 599.99, image: 'images/product4.jpg' }
        };
    }
}

function addToCart(productId) {
    const product = productsData[productId];
    if (!product) return;

    const existingItem = cart.find(item => item.id === productId);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ ...product, quantity: 1 });
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