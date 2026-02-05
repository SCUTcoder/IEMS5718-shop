#!/bin/bash
set -e

echo "=========================================="
echo "  Database Fix Script"
echo "=========================================="
echo ""

# Navigate to backend directory
cd /opt/app/IEMS5718-shop/backend

# Stop the service first
echo "Stopping backend service..."
sudo systemctl stop iems5718-shop || true

echo ""
echo "Checking database status..."

# Check if database exists
if [ -f "shop.db" ]; then
    echo "✓ Database file exists"
    
    # Check if categories table exists
    if sqlite3 shop.db "SELECT name FROM sqlite_master WHERE type='table' AND name='categories';" | grep -q "categories"; then
        echo "✓ Categories table exists"
        echo "Database is up to date!"
    else
        echo "✗ Categories table missing"
        echo ""
        echo "Option 1: Backup and recreate database (recommended)"
        echo "Option 2: Add categories table to existing database"
        echo ""
        read -p "Choose option (1 or 2): " choice
        
        if [ "$choice" == "1" ]; then
            # Backup existing database
            if [ -f "shop.db" ]; then
                echo "Backing up existing database..."
                cp shop.db shop.db.backup.$(date +%Y%m%d_%H%M%S)
                echo "✓ Backup created"
            fi
            
            # Remove old database
            echo "Removing old database..."
            rm -f shop.db shop.db-journal
            
            # Initialize new database
            echo "Creating new database with SQL script..."
            sqlite3 shop.db < src/main/resources/database-init.sql
            echo "✓ New database created"
            
        elif [ "$choice" == "2" ]; then
            echo "Adding categories table..."
            sqlite3 shop.db <<EOF
CREATE TABLE IF NOT EXISTS categories (
    catid INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

INSERT INTO categories (name) VALUES ('Electronics');
INSERT INTO categories (name) VALUES ('Clothing');

-- Update products table to add catid column if not exists
-- Note: SQLite doesn't support ALTER TABLE ADD COLUMN with FOREIGN KEY
-- You may need to migrate data manually
EOF
            echo "✓ Categories table added"
            echo "⚠️  Warning: You may need to update existing products with category IDs"
        fi
    fi
else
    echo "✗ Database file not found"
    echo "Creating new database..."
    sqlite3 shop.db < src/main/resources/database-init.sql
    echo "✓ Database created"
fi

echo ""
echo "Setting permissions..."
sudo chown www-data:www-data shop.db
sudo chmod 664 shop.db
echo "✓ Permissions set"

echo ""
echo "Verifying database structure..."
echo "Tables in database:"
sqlite3 shop.db "SELECT name FROM sqlite_master WHERE type='table';"

echo ""
echo "Categories count:"
sqlite3 shop.db "SELECT COUNT(*) FROM categories;"

echo ""
echo "Products count:"
sqlite3 shop.db "SELECT COUNT(*) FROM products;"

echo ""
echo "=========================================="
echo "Database fix completed!"
echo "=========================================="
echo ""
echo "Starting backend service..."
sudo systemctl start iems5718-shop

echo ""
echo "Waiting for service to start..."
sleep 3

echo "Service status:"
sudo systemctl status iems5718-shop --no-pager

echo ""
echo "To view logs, run:"
echo "sudo journalctl -u iems5718-shop -f"
