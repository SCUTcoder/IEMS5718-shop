// API Base URL
const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080/api' 
    : window.location.origin + '/api';

let categories = [];
let products = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
    loadProducts();
    
    // Section navigation
    document.querySelectorAll('.admin-nav button').forEach(button => {
        button.addEventListener('click', function() {
            const section = this.getAttribute('data-section');
            switchSection(section);
        });
    });
    
    // Image preview
    document.getElementById('product-image').addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.getElementById('image-preview');
                preview.src = e.target.result;
                preview.classList.add('show');
            };
            reader.readAsDataURL(file);
        }
    });
    
    // Form submissions
    document.getElementById('product-form').addEventListener('submit', handleProductSubmit);
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
});

function switchSection(section) {
    document.querySelectorAll('.admin-nav button').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelectorAll('.admin-section').forEach(sec => {
        sec.classList.remove('active');
    });
    
    document.querySelector(`[data-section="${section}"]`).classList.add('active');
    document.getElementById(`${section}-section`).classList.add('active');
}

// Categories Management
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/categories`);
        if (!response.ok) throw new Error('Failed to fetch categories');
        
        categories = await response.json();
        renderCategoriesTable();
        updateCategoryDropdown();
        showNotification('Categories loaded successfully');
    } catch (error) {
        console.error('Error loading categories:', error);
        showNotification('Error loading categories', 'error');
    }
}

function renderCategoriesTable() {
    const tbody = document.getElementById('categories-table-body');
    
    if (categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center;">No categories found</td></tr>';
        return;
    }
    
    tbody.innerHTML = categories.map(cat => `
        <tr>
            <td>${cat.catid}</td>
            <td>${cat.name}</td>
            <td class="action-buttons">
                <button class="btn-edit" onclick="editCategory(${cat.catid})">Edit</button>
                <button class="btn-delete" onclick="deleteCategory(${cat.catid})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function updateCategoryDropdown() {
    const select = document.getElementById('product-category');
    select.innerHTML = '<option value="">Select a category</option>' + 
        categories.map(cat => `<option value="${cat.catid}">${cat.name}</option>`).join('');
}

async function handleCategorySubmit(e) {
    e.preventDefault();
    
    const id = document.getElementById('category-id').value;
    const name = document.getElementById('category-name').value;
    
    const categoryData = { name };
    
    try {
        let response;
        if (id) {
            // Update
            categoryData.catid = parseInt(id);
            response = await fetch(`${API_BASE_URL}/categories/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(categoryData)
            });
        } else {
            // Create
            response = await fetch(`${API_BASE_URL}/categories`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(categoryData)
            });
        }
        
        if (!response.ok) throw new Error('Failed to save category');
        
        showNotification(id ? 'Category updated successfully' : 'Category created successfully');
        resetCategoryForm();
        loadCategories();
    } catch (error) {
        console.error('Error saving category:', error);
        showNotification('Error saving category', 'error');
    }
}

function editCategory(id) {
    const category = categories.find(c => c.catid === id);
    if (category) {
        document.getElementById('category-id').value = category.catid;
        document.getElementById('category-name').value = category.name;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

async function deleteCategory(id) {
    if (!confirm('Are you sure you want to delete this category?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/categories/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete category');
        
        showNotification('Category deleted successfully');
        loadCategories();
    } catch (error) {
        console.error('Error deleting category:', error);
        showNotification('Error deleting category. It may have products associated with it.', 'error');
    }
}

function resetCategoryForm() {
    document.getElementById('category-form').reset();
    document.getElementById('category-id').value = '';
}

// Products Management
async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error('Failed to fetch products');
        
        products = await response.json();
        renderProductsTable();
        showNotification('Products loaded successfully');
    } catch (error) {
        console.error('Error loading products:', error);
        showNotification('Error loading products', 'error');
    }
}

function renderProductsTable() {
    const tbody = document.getElementById('products-table-body');
    
    if (products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No products found</td></tr>';
        return;
    }
    
    tbody.innerHTML = products.map(product => `
        <tr>
            <td>${product.pid}</td>
            <td>${product.name}</td>
            <td>${product.category ? product.category.name : 'N/A'}</td>
            <td>$${product.price.toFixed(2)}</td>
            <td>${product.stockQuantity || 0}</td>
            <td class="action-buttons">
                <button class="btn-edit" onclick="editProduct(${product.pid})">Edit</button>
                <button class="btn-delete" onclick="deleteProduct(${product.pid})">Delete</button>
            </td>
        </tr>
    `).join('');
}

async function handleProductSubmit(e) {
    e.preventDefault();
    
    const id = document.getElementById('product-id').value;
    const categoryId = document.getElementById('product-category').value;
    const name = document.getElementById('product-name').value;
    const price = parseFloat(document.getElementById('product-price').value);
    const description = document.getElementById('product-description').value;
    const stockQuantity = parseInt(document.getElementById('product-stock').value) || 0;
    const imageFile = document.getElementById('product-image').files[0];
    
    try {
        let response;
        
        // If image file is provided, use multipart/form-data
        if (imageFile) {
            const formData = new FormData();
            formData.append('catid', categoryId);
            formData.append('name', name);
            formData.append('price', price);
            formData.append('description', description);
            formData.append('stockQuantity', stockQuantity);
            formData.append('image', imageFile);
            
            if (id) {
                // Update with image
                response = await fetch(`${API_BASE_URL}/products/${id}/upload`, {
                    method: 'PUT',
                    body: formData
                });
            } else {
                // Create with image
                response = await fetch(`${API_BASE_URL}/products/upload`, {
                    method: 'POST',
                    body: formData
                });
            }
        } else {
            // Use JSON without file upload
            const productData = {
                category: { catid: parseInt(categoryId) },
                name,
                price,
                description,
                stockQuantity,
                active: true
            };
            
            if (id) {
                productData.pid = parseInt(id);
                // Update
                response = await fetch(`${API_BASE_URL}/products/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(productData)
                });
            } else {
                // Create
                response = await fetch(`${API_BASE_URL}/products`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(productData)
                });
            }
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save product');
        }
        
        showNotification(id ? 'Product updated successfully' : 'Product created successfully');
        resetProductForm();
        loadProducts();
    } catch (error) {
        console.error('Error saving product:', error);
        showNotification('Error saving product: ' + error.message, 'error');
    }
}

function editProduct(id) {
    const product = products.find(p => p.pid === id);
    if (product) {
        document.getElementById('product-id').value = product.pid;
        document.getElementById('product-category').value = product.category ? product.category.catid : '';
        document.getElementById('product-name').value = product.name;
        document.getElementById('product-price').value = product.price;
        document.getElementById('product-description').value = product.description;
        document.getElementById('product-stock').value = product.stockQuantity || 0;
        
        // Preview existing image if available
        if (product.imageUrl) {
            const preview = document.getElementById('image-preview');
            preview.src = product.imageUrl;
            preview.classList.add('show');
        }
        
        switchSection('products');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/products/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete product');
        
        showNotification('Product deleted successfully');
        loadProducts();
    } catch (error) {
        console.error('Error deleting product:', error);
        showNotification('Error deleting product', 'error');
    }
}

function resetProductForm() {
    document.getElementById('product-form').reset();
    document.getElementById('product-id').value = '';
    document.getElementById('image-preview').classList.remove('show');
}

// Utility functions
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}
