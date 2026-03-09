const API_BASE_URL = (window.location.protocol === 'file:' ||
                     window.location.hostname === 'localhost' ||
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '')
    ? 'http://localhost:8080/api'
    : window.location.origin + '/api';

const MAX_IMAGE_SIZE = 10 * 1024 * 1024;
const MAX_VIDEO_SIZE = 100 * 1024 * 1024;

let categories = [];
let products = [];
let selectedImageFiles = [];
let selectedVideoFile = null;
let existingImageUrls = [];
let existingVideoUrl = '';
let imagePreviewObjectUrls = [];
let videoPreviewObjectUrl = '';

document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
    loadProducts();

    document.querySelectorAll('.admin-tab').forEach(button => {
        button.addEventListener('click', function() {
            switchSection(this.getAttribute('data-section'));
        });
    });

    initMediaUploads();

    document.getElementById('product-form').addEventListener('submit', handleProductSubmit);
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
});

function initMediaUploads() {
    initImageUpload();
    initVideoUpload();
}

function initImageUpload() {
    const zone = document.getElementById('image-upload-zone');
    const fileInput = document.getElementById('product-images');
    const removeBtn = document.getElementById('image-remove-btn');

    fileInput.addEventListener('change', (event) => {
        setImageFiles(Array.from(event.target.files || []));
    });

    removeBtn.addEventListener('click', (event) => {
        event.stopPropagation();
        clearImageSelection(true);
    });

    zone.addEventListener('dragover', (event) => {
        event.preventDefault();
        zone.classList.add('dragover');
    });

    zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));

    zone.addEventListener('drop', (event) => {
        event.preventDefault();
        zone.classList.remove('dragover');
        const files = Array.from(event.dataTransfer.files || []).filter(file => file.type.startsWith('image/'));
        if (files.length > 0) {
            setImageFiles(files);
        }
    });

    document.addEventListener('paste', (event) => {
        const productSection = document.getElementById('products-section');
        if (!productSection.classList.contains('active')) return;

        const items = event.clipboardData?.items;
        if (!items) return;

        for (const item of items) {
            if (item.type.startsWith('image/')) {
                event.preventDefault();
                const file = item.getAsFile();
                if (file) {
                    setImageFiles([file]);
                }
                return;
            }
        }
    });
}

function initVideoUpload() {
    const zone = document.getElementById('video-upload-zone');
    const fileInput = document.getElementById('product-video');
    const removeBtn = document.getElementById('video-remove-btn');

    fileInput.addEventListener('change', (event) => {
        const file = event.target.files?.[0];
        if (file) {
            setVideoFile(file);
        }
    });

    removeBtn.addEventListener('click', (event) => {
        event.stopPropagation();
        clearVideoSelection(true);
    });

    zone.addEventListener('dragover', (event) => {
        event.preventDefault();
        zone.classList.add('dragover');
    });

    zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));

    zone.addEventListener('drop', (event) => {
        event.preventDefault();
        zone.classList.remove('dragover');
        const file = Array.from(event.dataTransfer.files || []).find(candidate => candidate.type.startsWith('video/'));
        if (file) {
            setVideoFile(file);
        }
    });
}

function setImageFiles(files) {
    const validFiles = files.filter(file => validateImageFile(file));
    if (validFiles.length === 0) {
        return;
    }

    selectedImageFiles = validFiles;
    renderImagePreview(validFiles, true);
}

function clearImageSelection(showExistingMedia) {
    revokeImagePreviewUrls();
    selectedImageFiles = [];
    document.getElementById('product-images').value = '';
    renderImagePreview(showExistingMedia ? existingImageUrls : [], false);
}

function setVideoFile(file) {
    if (!validateVideoFile(file)) {
        return;
    }

    selectedVideoFile = file;
    renderVideoPreview(file, true);
}

function clearVideoSelection(showExistingMedia) {
    revokeVideoPreviewUrl();
    selectedVideoFile = null;
    document.getElementById('product-video').value = '';
    renderVideoPreview(showExistingMedia ? existingVideoUrl : '', false);
}

function validateImageFile(file) {
    if (!file.type.startsWith('image/')) {
        showNotification('Only image files are supported', 'error');
        return false;
    }
    if (file.size > MAX_IMAGE_SIZE) {
        showNotification('Each image must be 10MB or smaller', 'error');
        return false;
    }
    return true;
}

function validateVideoFile(file) {
    if (!file.type.startsWith('video/')) {
        showNotification('Only video files are supported', 'error');
        return false;
    }
    if (file.size > MAX_VIDEO_SIZE) {
        showNotification('Video must be 100MB or smaller', 'error');
        return false;
    }
    return true;
}

function renderImagePreview(items, fromFiles) {
    const zone = document.getElementById('image-upload-zone');
    const previewWrap = document.getElementById('image-preview-wrap');
    const previewList = document.getElementById('image-preview-list');
    const removeBtn = document.getElementById('image-remove-btn');

    revokeImagePreviewUrls();

    if (!items || items.length === 0) {
        previewList.innerHTML = '';
        previewWrap.classList.remove('show');
        zone.classList.remove('has-image');
        removeBtn.style.display = 'none';
        return;
    }

    const previewUrls = fromFiles
        ? items.map(file => {
            const url = URL.createObjectURL(file);
            imagePreviewObjectUrls.push(url);
            return { url, label: file.name };
        })
        : items.map((url, index) => ({
            url,
            label: index === 0 ? 'Current cover' : `Current image ${index + 1}`
        }));

    previewList.innerHTML = previewUrls.map((item, index) => `
        <div class="image-preview-card ${index === 0 ? 'is-cover' : ''}">
            <img src="${item.url}" alt="Product preview ${index + 1}">
            <span>${index === 0 ? 'Cover' : `Image ${index + 1}`}</span>
        </div>
    `).join('');

    previewWrap.classList.add('show');
    zone.classList.add('has-image');
    removeBtn.style.display = fromFiles ? 'flex' : 'none';
}

function renderVideoPreview(videoSource, fromFile) {
    const zone = document.getElementById('video-upload-zone');
    const previewWrap = document.getElementById('video-preview-wrap');
    const preview = document.getElementById('video-preview');
    const nameEl = document.getElementById('video-preview-name');
    const removeBtn = document.getElementById('video-remove-btn');

    revokeVideoPreviewUrl();

    if (!videoSource) {
        preview.removeAttribute('src');
        preview.load();
        nameEl.textContent = '';
        previewWrap.classList.remove('show');
        zone.classList.remove('has-image');
        removeBtn.style.display = 'none';
        return;
    }

    const src = fromFile ? URL.createObjectURL(videoSource) : videoSource;
    if (fromFile) {
        videoPreviewObjectUrl = src;
    }

    preview.src = src;
    preview.load();
    nameEl.textContent = fromFile ? videoSource.name : 'Current product video';
    previewWrap.classList.add('show');
    zone.classList.add('has-image');
    removeBtn.style.display = fromFile ? 'flex' : 'none';
}

function revokeImagePreviewUrls() {
    imagePreviewObjectUrls.forEach(url => URL.revokeObjectURL(url));
    imagePreviewObjectUrls = [];
}

function revokeVideoPreviewUrl() {
    if (videoPreviewObjectUrl) {
        URL.revokeObjectURL(videoPreviewObjectUrl);
        videoPreviewObjectUrl = '';
    }
}

function switchSection(section) {
    document.querySelectorAll('.admin-tab').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelectorAll('.admin-section').forEach(sec => {
        sec.classList.remove('active');
    });
    
    document.querySelector(`.admin-tab[data-section="${section}"]`).classList.add('active');
    document.getElementById(`${section}-section`).classList.add('active');
}

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

async function handleCategorySubmit(event) {
    event.preventDefault();
    
    const id = document.getElementById('category-id').value;
    const name = document.getElementById('category-name').value;
    
    const categoryData = { name };
    
    try {
        let response;
        if (id) {
            categoryData.catid = parseInt(id, 10);
            response = await fetch(`${API_BASE_URL}/categories/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(categoryData)
            });
        } else {
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
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No products found</td></tr>';
        return;
    }

    tbody.innerHTML = products.map(product => {
        const weight = product.weight || 0;
        const badgeClass = weight >= 100 ? 'top' : weight >= 50 ? 'high' : '';
        return `
        <tr>
            <td>${product.pid}</td>
            <td>${product.name}</td>
            <td>${product.category ? product.category.name : 'N/A'}</td>
            <td>$${product.price.toFixed(2)}</td>
            <td>${product.stockQuantity || 0}</td>
            <td>
                <div class="weight-cell">
                    <span class="weight-badge ${badgeClass}" id="weight-badge-${product.pid}">${weight}</span>
                    <input type="number" class="weight-input" id="weight-input-${product.pid}"
                           value="${weight}" min="0" max="9999" style="display:none">
                    <button class="btn-weight-save" id="weight-edit-btn-${product.pid}"
                            onclick="toggleWeightEdit(${product.pid})" title="Edit weight">✏️</button>
                    <button class="btn-weight-save" id="weight-save-btn-${product.pid}"
                            onclick="saveWeight(${product.pid})" style="display:none">Save</button>
                </div>
            </td>
            <td class="action-buttons">
                <button class="btn-edit" onclick="editProduct(${product.pid})">Edit</button>
                <button class="btn-delete" onclick="deleteProduct(${product.pid})">Delete</button>
            </td>
        </tr>
    `}).join('');
}

function toggleWeightEdit(pid) {
    const badge = document.getElementById(`weight-badge-${pid}`);
    const input = document.getElementById(`weight-input-${pid}`);
    const editBtn = document.getElementById(`weight-edit-btn-${pid}`);
    const saveBtn = document.getElementById(`weight-save-btn-${pid}`);
    const isEditing = input.style.display !== 'none';

    if (isEditing) {
        badge.style.display = '';
        input.style.display = 'none';
        editBtn.style.display = '';
        saveBtn.style.display = 'none';
    } else {
        badge.style.display = 'none';
        input.style.display = '';
        input.focus();
        input.select();
        editBtn.style.display = 'none';
        saveBtn.style.display = '';
    }
}

async function saveWeight(pid) {
    const input = document.getElementById(`weight-input-${pid}`);
    const weight = parseInt(input.value, 10) || 0;

    try {
        const response = await fetch(`${API_BASE_URL}/products/${pid}/weight`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ weight })
        });

        if (!response.ok) throw new Error('Failed to update weight');

        const updated = await response.json();
        const product = products.find(item => item.pid === pid);
        if (product) product.weight = updated.weight;

        const badge = document.getElementById(`weight-badge-${pid}`);
        const nextWeight = updated.weight || 0;
        badge.textContent = nextWeight;
        badge.className = 'weight-badge' + (nextWeight >= 100 ? ' top' : nextWeight >= 50 ? ' high' : '');

        toggleWeightEdit(pid);
        showNotification(`Weight updated to ${nextWeight}`);

        products.sort((a, b) => (b.weight || 0) - (a.weight || 0) || a.pid - b.pid);
        renderProductsTable();
    } catch (error) {
        showNotification('Failed to update weight', 'error');
    }
}

async function handleProductSubmit(event) {
    event.preventDefault();
    
    const id = document.getElementById('product-id').value;
    const categoryId = document.getElementById('product-category').value;
    const name = document.getElementById('product-name').value;
    const price = parseFloat(document.getElementById('product-price').value);
    const description = document.getElementById('product-description').value;
    const stockQuantity = parseInt(document.getElementById('product-stock').value, 10) || 0;
    const weight = parseInt(document.getElementById('product-weight').value, 10) || 0;
    const hasNewMedia = selectedImageFiles.length > 0 || !!selectedVideoFile;

    try {
        let response;

        if (hasNewMedia) {
            const formData = new FormData();
            formData.append('catid', categoryId);
            formData.append('name', name);
            formData.append('price', price);
            formData.append('description', description);
            formData.append('stockQuantity', stockQuantity);
            formData.append('weight', weight);
            selectedImageFiles.forEach(file => formData.append('images', file));
            if (selectedVideoFile) {
                formData.append('video', selectedVideoFile);
            }

            response = await fetch(`${API_BASE_URL}/products${id ? `/${id}/upload` : '/upload'}`, {
                method: id ? 'PUT' : 'POST',
                body: formData
            });
        } else {
            const productData = {
                category: { catid: parseInt(categoryId, 10) },
                name,
                price,
                description,
                stockQuantity,
                weight,
                active: true
            };
            
            if (id) {
                productData.pid = parseInt(id, 10);
                response = await fetch(`${API_BASE_URL}/products/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(productData)
                });
            } else {
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
    const product = products.find(item => item.pid === id);
    if (!product) {
        return;
    }

    document.getElementById('product-id').value = product.pid;
    document.getElementById('product-category').value = product.category ? product.category.catid : '';
    document.getElementById('product-name').value = product.name;
    document.getElementById('product-price').value = product.price;
    document.getElementById('product-description').value = product.description;
    document.getElementById('product-stock').value = product.stockQuantity || 0;
    document.getElementById('product-weight').value = product.weight || 0;

    selectedImageFiles = [];
    selectedVideoFile = null;
    document.getElementById('product-images').value = '';
    document.getElementById('product-video').value = '';

    existingImageUrls = getProductGalleryImages(product);
    existingVideoUrl = product.videoUrl || '';
    renderImagePreview(existingImageUrls, false);
    renderVideoPreview(existingVideoUrl, false);

    switchSection('products');
    window.scrollTo({ top: 0, behavior: 'smooth' });
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
    existingImageUrls = [];
    existingVideoUrl = '';
    clearImageSelection(false);
    clearVideoSelection(false);
}

function getProductGalleryImages(product) {
    const galleryImages = splitMediaCsv(product.galleryImageUrls);
    const thumbnailImages = splitMediaCsv(product.thumbnailUrls);

    if (galleryImages.length > 1) {
        return galleryImages;
    }
    if (galleryImages.length <= 1 && thumbnailImages.length > galleryImages.length) {
        return thumbnailImages;
    }
    if (galleryImages.length === 1) {
        return galleryImages;
    }
    if (thumbnailImages.length > 0) {
        return thumbnailImages;
    }
    return product.imageUrl ? [product.imageUrl] : [];
}

function splitMediaCsv(csv) {
    if (!csv) {
        return [];
    }
    return csv.split(',')
        .map(item => item.trim())
        .filter(Boolean);
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}
