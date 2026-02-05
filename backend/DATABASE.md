# Database Setup Instructions

## Database Type
This project uses **SQLite** database.

## Database File Location
The database file `shop.db` will be created automatically in the backend root directory when the application starts for the first time.

## Database Schema

### Categories Table
- `catid` (INTEGER PRIMARY KEY): Category ID
- `name` (TEXT NOT NULL UNIQUE): Category name

### Products Table
- `pid` (INTEGER PRIMARY KEY): Product ID
- `catid` (INTEGER NOT NULL): Foreign key to categories table
- `name` (TEXT NOT NULL): Product name
- `price` (REAL NOT NULL): Product price
- `description` (TEXT): Product description
- `image_url` (TEXT): Main product image URL
- `thumbnail_urls` (TEXT): Comma-separated thumbnail image URLs
- `stock_quantity` (INTEGER): Stock quantity
- `active` (INTEGER NOT NULL DEFAULT 1): Active status (1 = active, 0 = inactive)

## Initialization

### Automatic Initialization
The database is automatically initialized with sample data when the application starts for the first time (when the database is empty). This is handled by `DataInitializer.java`.

### Manual Initialization (Using SQL Script)
If you need to recreate the database manually, you can use the SQL script:

1. **Using SQLite Command Line:**
   ```bash
   sqlite3 shop.db < src/main/resources/database-init.sql
   ```

2. **Using SQLite Browser:**
   - Open SQLite Browser
   - Create a new database file `shop.db`
   - Open the SQL script `src/main/resources/database-init.sql`
   - Execute the script

## Sample Data

The database is initialized with:
- **2 Categories:** Electronics, Clothing
- **6 Products:** 
  - Electronics: Gaming Laptop, Wireless Headphones, Smart Watch, Tablet PC
  - Clothing: Cotton T-Shirt, Classic Jeans

## Database Backup

To backup the database:
```bash
cp shop.db shop.db.backup
```

To restore from backup:
```bash
cp shop.db.backup shop.db
```

## Submission Requirements

For assignment submission, you can either:
1. Submit the `shop.db` file directly
2. Submit the `database-init.sql` script that can recreate the database

The SQL script is located at: `src/main/resources/database-init.sql`
