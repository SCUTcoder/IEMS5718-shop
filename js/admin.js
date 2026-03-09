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

let originalImageMediaItems = [];
let imageMediaItems = [];
let originalVideoUrl = '';
let currentExistingVideoUrl = '';
let selectedVideoFile = null;
let videoPreviewObjectUrl = '';
let tempImageId = 0;

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
    renderImagePreview();
    renderVideoPreview();
}

function initImageUpload() {
    const zone = document.getElementById('image-upload-zone');
    const fileInput = document.getElementById('product-images');
    const browseBtn = document.getElementById('image-browse-btn');
    const clearBtn = document.getElementById('image-clear-all-btn');

    browseBtn.addEventListener('click', () => fileInput.click());
    clearBtn.addEventListener('click', clearAllImages);

    fileInput.addEventListener('change', (event) => {
        appendImageFiles(Array.from(event.target.files || []));
        fileInput.value = '';
    });

    zone.addEventListener('dragover', (event) => {
        event.preventDefault();
        zone.classList.add('dragover');
    });

    zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));

    zone.addEventListener('drop', (event) => {
        event.preventDefault();
        zone.classList.remove('dragover');
        appendImageFiles(Array.from(event.dataTransfer.files || []));
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
                    appendImageFiles([file]);
                }
                return;
            }
        }
    });
}

function initVideoUpload() {
    const zone = document.getElementById('video-upload-zone');
    const fileInput = document.getElementById('product-video');
    const browseBtn = document.getElementById('video-browse-btn');
    const clearBtn = document.getElementById('video-clear-btn');

    browseBtn.addEventListener('click', () => fileInput.click());
    clearBtn.addEventListener('click', clearVideoSelection);

    fileInput.addEventListener('change', (event) => {
        const file = event.target.files?.[0];
        if (file) {
            setVideoFile(file);
        }
        fileInput.value = '';
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

function appendImageFiles(files) {
    const validFiles = files.filter(validateImageFile);
    if (validFiles.length === 0) {
        return;
    }

    validFiles.forEach(file => {
        imageMediaItems.push({
            id: `new-${Date.now()}-${tempImageId++}`,
            source: 'new',
            file,
            imageUrl: '',
            thumbnailUrl: '',
            previewUrl: URL.createObjectURL(file),
            name: file.name
        });
    });

    renderImagePreview();
}

function clearAllImages() {
    disposeImageMediaItems(imageMediaItems);
    imageMediaItems = [];
    document.getElementById('product-images').value = '';
    renderImagePreview();
}

function removeImageItem(itemId) {
    const index = imageMediaItems.findIndex(item => item.id === itemId);
    if (index === -1) {
        return;
    }

    const [removedItem] = imageMediaItems.splice(index, 1);
    disposeImageMediaItems([removedItem]);
    renderImagePreview();
}

function setVideoFile(file) {
    if (!validateVideoFile(file)) {
        return;
    }

    revokeVideoPreviewUrl();
    selectedVideoFile = file;
    videoPreviewObjectUrl = URL.createObjectURL(file);
    renderVideoPreview();
}

function clearVideoSelection() {
    if (selectedVideoFile) {
        revokeVideoPreviewUrl();
        selectedVideoFile = null;
        renderVideoPreview();
        return;
    }

    if (currentExistingVideoUrl) {
        currentExistingVideoUrl = '';
        renderVideoPreview();
    }
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

function renderImagePreview() {
    const zone = document.getElementById('image-upload-zone');
    const previewWrap = document.getElementById('image-preview-wrap');
    const previewList = document.getElementById('image-preview-list');
    const clearBtn = document.getElementById('image-clear-all-btn');

    if (imageMediaItems.length === 0) {
        previewList.innerHTML = '';
        previewWrap.classList.remove('show');
        zone.classList.remove('has-image');
        clearBtn.disabled = true;
        return;
    }

    previewList.innerHTML = imageMediaItems.map((item, index) => {
        const previewSrc = item.source === 'new' ? item.previewUrl : (item.thumbnailUrl || item.imageUrl);
        const title = index === 0 ? 'Cover Photo' : `Photo ${index + 1}`;
        const meta = item.source === 'new'
            ? escapeHtml(item.name)
            : (item.thumbnailUrl && item.thumbnailUrl !== item.imageUrl ? 'Current image (with thumbnail)' : 'Current image');

        return `
            <div class="image-preview-card ${index === 0 ? 'is-cover' : ''}">
                <button type="button" class="image-preview-item-remove" data-image-id="${item.id}" title="Remove this photo">×</button>
                <span class="image-preview-status">${item.source === 'new' ? 'New' : 'Current'}</span>
                <img src="${previewSrc}" alt="Product preview ${index + 1}">
                <div class="image-preview-card-body">
                    <div class="image-preview-card-title">${escapeHtml(title)}</div>
                    <div class="image-preview-card-meta">${meta}</div>
                </div>
            </div>
        `;
    }).join('');

    previewList.querySelectorAll('.image-preview-item-remove').forEach(button => {
        button.addEventListener('click', (event) => {
            event.stopPropagation();
            removeImageItem(button.dataset.imageId);
        });
    });

    previewWrap.classList.add('show');
    zone.classList.add('has-image');
    clearBtn.disabled = false;
}

function renderVideoPreview() {
    const zone = document.getElementById('video-upload-zone');
    const previewWrap = document.getElementById('video-preview-wrap');
    const preview = document.getElementById('video-preview');
    const nameEl = document.getElementById('video-preview-name');
    const browseBtn = document.getElementById('video-browse-btn');
    const clearBtn = document.getElementById('video-clear-btn');

    const currentVideoState = getCurrentVideoState();
    if (!currentVideoState) {
        preview.removeAttribute('src');
        preview.load();
        nameEl.textContent = '';
        previewWrap.classList.remove('show');
        zone.classList.remove('has-image');
        browseBtn.textContent = 'Choose Video';
        clearBtn.disabled = true;
        return;
    }

    preview.src = currentVideoState.src;
    preview.load();
    nameEl.textContent = currentVideoState.label;
    previewWrap.classList.add('show');
    zone.classList.add('has-image');
    browseBtn.textContent = 'Replace Video';
    clearBtn.disabled = false;
}

function getCurrentVideoState() {
    if (selectedVideoFile && videoPreviewObjectUrl) {
        return {
            src: videoPreviewObjectUrl,
            label: `New video: ${selectedVideoFile.name}`
        };
    }

    if (currentExistingVideoUrl) {
        return {
            src: currentExistingVideoUrl,
            label: 'Current product video'
        };
    }

    return null;
}

function disposeImageMediaItems(items) {
    items.forEach(item => {
        if (item.source === 'new' && item.previewUrl) {
            URL.revokeObjectURL(item.previewUrl);
        }
    });
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
    `;
    }).join('');
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

    const newImageFiles = getNewImageFiles();
    const imageChanged = hasImageMediaChanges();
    const videoChanged = hasVideoMediaChanges();
    const shouldUseUploadEndpoint = id
        ? (imageChanged || videoChanged)
        : (newImageFiles.length > 0 || !!selectedVideoFile);

    try {
        let response;

        if (shouldUseUploadEndpoint) {
            const formData = new FormData();
            formData.append('catid', categoryId);
            formData.append('name', name);
            formData.append('price', price);
            formData.append('description', description);
            formData.append('stockQuantity', stockQuantity);
            formData.append('weight', weight);

            newImageFiles.forEach(file => formData.append('images', file));
            if (selectedVideoFile) {
                formData.append('video', selectedVideoFile);
            }

            if (id) {
                const retainedExistingImages = getRetainedExistingImageItems();
                formData.append('replaceImages', String(imageChanged));
                formData.append('retainedGalleryImageUrls', retainedExistingImages.map(item => item.imageUrl).join(','));
                formData.append('retainedThumbnailUrls', retainedExistingImages.map(item => item.thumbnailUrl || item.imageUrl).join(','));
                formData.append('clearVideo', String(shouldClearVideo()));
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

    resetMediaState();
    originalImageMediaItems = buildExistingImageMediaItems(product);
    imageMediaItems = originalImageMediaItems.map(item => ({ ...item }));
    originalVideoUrl = product.videoUrl || '';
    currentExistingVideoUrl = originalVideoUrl;

    renderImagePreview();
    renderVideoPreview();

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
    resetMediaState();
    renderImagePreview();
    renderVideoPreview();
}

function resetMediaState() {
    disposeImageMediaItems(imageMediaItems);
    imageMediaItems = [];
    originalImageMediaItems = [];
    originalVideoUrl = '';
    currentExistingVideoUrl = '';
    selectedVideoFile = null;
    revokeVideoPreviewUrl();
    document.getElementById('product-images').value = '';
    document.getElementById('product-video').value = '';
}

function buildExistingImageMediaItems(product) {
    const imagePairs = resolveProductImagePairs(product);
    return imagePairs.map((pair, index) => ({
        id: `existing-${index}-${pair.imageUrl}`,
        source: 'existing',
        imageUrl: pair.imageUrl,
        thumbnailUrl: pair.thumbnailUrl
    }));
}

function resolveProductImagePairs(product) {
    const galleryImages = splitMediaCsv(product.galleryImageUrls);
    const thumbnailImages = splitMediaCsv(product.thumbnailUrls);

    if (galleryImages.length > 1) {
        return buildImagePairs(galleryImages, thumbnailImages.length === galleryImages.length ? thumbnailImages : galleryImages);
    }
    if (galleryImages.length <= 1 && thumbnailImages.length > galleryImages.length) {
        return buildImagePairs(thumbnailImages, thumbnailImages);
    }
    if (galleryImages.length === 1) {
        return buildImagePairs(galleryImages, thumbnailImages.length === 1 ? thumbnailImages : galleryImages);
    }
    if (thumbnailImages.length > 0) {
        return buildImagePairs(thumbnailImages, thumbnailImages);
    }
    return product.imageUrl ? buildImagePairs([product.imageUrl], [product.imageUrl]) : [];
}

function buildImagePairs(imageUrls, thumbnailUrls) {
    return imageUrls.map((imageUrl, index) => ({
        imageUrl,
        thumbnailUrl: thumbnailUrls[index] || imageUrl
    }));
}

function getNewImageFiles() {
    return imageMediaItems
        .filter(item => item.source === 'new')
        .map(item => item.file);
}

function getRetainedExistingImageItems() {
    return imageMediaItems.filter(item => item.source === 'existing');
}

function hasImageMediaChanges() {
    const originalKeys = originalImageMediaItems.map(buildImageMediaKey);
    const currentKeys = getRetainedExistingImageItems().map(buildImageMediaKey);

    if (getNewImageFiles().length > 0) {
        return true;
    }
    if (originalKeys.length !== currentKeys.length) {
        return true;
    }
    return originalKeys.some((key, index) => key !== currentKeys[index]);
}

function hasVideoMediaChanges() {
    return !!selectedVideoFile || shouldClearVideo();
}

function shouldClearVideo() {
    return !!originalVideoUrl && !currentExistingVideoUrl && !selectedVideoFile;
}

function buildImageMediaKey(item) {
    return `${item.imageUrl}|${item.thumbnailUrl || item.imageUrl}`;
}

function splitMediaCsv(csv) {
    if (!csv) {
        return [];
    }
    return csv.split(',')
        .map(item => item.trim())
        .filter(Boolean);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
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
