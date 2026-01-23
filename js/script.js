// IEMS5718 Shop JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Cart functionality
    let cart = JSON.parse(localStorage.getItem('iems5718-cart')) || [];
    updateCartDisplay();

    // Add to cart buttons
    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            addToCart(productId);
        });
    });

    // Thumbnail image switching (for product detail page)
    document.querySelectorAll('.thumbnail').forEach(thumbnail => {
        thumbnail.addEventListener('click', function() {
            const mainImage = document.getElementById('main-product-image');
            if (mainImage) {
                mainImage.src = this.getAttribute('data-image');

                // Update active thumbnail
                document.querySelectorAll('.thumbnail').forEach(thumb => {
                    thumb.classList.remove('active');
                });
                this.classList.add('active');
            }
        });
    });

    // Mobile cart toggle
    const cartIcon = document.querySelector('.cart-icon');
    if (cartIcon) {
        cartIcon.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) {
                e.preventDefault();
                const cartContainer = this.parentElement;
                cartContainer.classList.toggle('active');
            }
        });
    }

    // Checkout button
    const checkoutBtn = document.querySelector('.checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', function() {
            alert('Checkout functionality will be implemented in Phase 2!');
        });
    }
});

function addToCart(productId) {
    const products = {
        '1': {
            id: '1',
            name: 'Gaming Laptop',
            price: 1299.99,
            image: 'images/product1.jpg'
        },
        '2': {
            id: '2',
            name: 'Wireless Headphones',
            price: 249.99,
            image: 'images/product2.jpg'
        },
        '3': {
            id: '3',
            name: 'Smart Watch',
            price: 399.99,
            image: 'images/product3.jpg'
        },
        '4': {
            id: '4',
            name: 'Tablet PC',
            price: 599.99,
            image: 'images/product4.jpg'
        }
    };

    const product = products[productId];
    if (!product) return;

    // Check if product already in cart
    const existingItem = cart.find(item => item.id === productId);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            ...product,
            quantity: 1
        });
    }

    localStorage.setItem('iems5718-cart', JSON.stringify(cart));
    updateCartDisplay();

    // Show feedback
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

    // Update cart count
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalItems;

    // Update cart dropdown
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

        // Add event listeners for quantity changes and remove buttons
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
    // Create notification element
    const notification = document.createElement('div');
    notification.className = 'notification slide-in';
    notification.textContent = message;

    document.body.appendChild(notification);

    // Remove after 3 seconds
    setTimeout(() => {
        notification.classList.remove('slide-in');
        notification.classList.add('slide-out');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}