# IEMS5718 Shop

A full-stack e-commerce website built for IEMS5718 Network Security course.

## Features

### Phase 2B: Data Presentation & Management
- ✅ SQLite database with categories and products tables
- ✅ Category management (CRUD operations)
- ✅ Product management (CRUD operations)
- ✅ Admin panel for managing products and categories
- ✅ File upload with automatic image resizing
- ✅ Thumbnail generation for product images
- ✅ Dynamic product listing from database
- ✅ Product detail pages with multiple images

### Phase 3: AJAX Shopping Cart
- ✅ Add to cart functionality (main page & product detail page)
- ✅ Update cart quantity and remove items
- ✅ LocalStorage persistence
- ✅ Dynamic cart total calculation
- ✅ Page reload without losing cart data

## Technology Stack

### Backend
- Java Spring Boot 3.2.1
- SQLite Database
- Spring Data JPA
- Thumbnailator (image processing)
- Maven

### Frontend
- HTML5, CSS3, JavaScript
- Responsive design
- LocalStorage API

## Setup and Installation

### Prerequisites
- Java 17 or higher
- Maven
- Modern web browser

### Backend Setup

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   java -jar target/shop-backend-1.0.0.jar
   ```

   Or use the run script (Windows):
   ```bash
   run.bat
   ```

The backend server will start on `http://localhost:8080`.

### Database

The SQLite database (`shop.db`) is automatically created on first run with:
- 2 categories (Electronics, Clothing)
- 6 products (4 Electronics, 2 Clothing)

To manually initialize the database:
```bash
sqlite3 shop.db < src/main/resources/database-init.sql
```

### Frontend

Open in browser:
- Main shop: `index-dynamic.html`
- Admin panel: `admin.html`
- Product details: `product.html?id=1`

## API Endpoints

### Products
- `GET /api/products` - Get all active products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{catid}` - Get products by category
- `POST /api/products` - Create product (JSON)
- `POST /api/products/upload` - Create product with image upload
- `PUT /api/products/{id}` - Update product (JSON)
- `PUT /api/products/{id}/upload` - Update product with image upload
- `DELETE /api/products/{id}` - Soft delete product

### Categories
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

## Assignment Requirements Met

### Phase 2B (18 points)
1. ✅ SQL database with categories and products (1pt)
2. ✅ Admin panel for products with dropdown, inputs, textarea, file upload (2pt)
3. ✅ Admin panel for categories (2pt)
4. ✅ Form submission updates database (3pt)
5. ✅ Main page populates categories from DB (1pt)
6. ✅ Main page filters products by category (3pt)
7. ✅ Product detail page displays from DB (2pt)
8. ✅ Automatic image resizing and thumbnails (3pt)

### Phase 3 (10 points)
1. ✅ Add to cart on main/product pages (1pt)
2. ✅ Same product shows as one row (1pt)
3. ✅ Update quantity and delete (1pt)
4. ✅ Store in localStorage (2pt)
5. ✅ Fetch product info from backend (3pt)
6. ✅ Calculate total at client-side (1pt)
7. ✅ Restore cart on page reload (2pt)

## Author

章程
IEMS5718 Network Security Course
