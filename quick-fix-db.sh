#!/bin/bash
# Quick database fix - no prompts, just recreate

set -e

echo "Quick Database Fix - Recreating database..."

cd /opt/app/IEMS5718-shop/backend

# Stop service
sudo systemctl stop iems5718-shop 2>/dev/null || true

# Backup if exists
if [ -f "shop.db" ]; then
    mv shop.db shop.db.backup.$(date +%Y%m%d_%H%M%S)
    echo "✓ Old database backed up"
fi

# Remove journal file
rm -f shop.db-journal

# Create new database
echo "Creating new database..."
sqlite3 shop.db < src/main/resources/database-init.sql

# Set permissions
sudo chown www-data:www-data shop.db
sudo chmod 664 shop.db

echo "✓ Database recreated successfully"

# Verify
echo ""
echo "Database tables:"
sqlite3 shop.db ".tables"

echo ""
echo "Categories:"
sqlite3 shop.db "SELECT * FROM categories;"

echo ""
echo "Products count:"
sqlite3 shop.db "SELECT COUNT(*) FROM products;"

# Start service
echo ""
echo "Starting backend service..."
sudo systemctl start iems5718-shop

sleep 2
sudo systemctl status iems5718-shop --no-pager

echo ""
echo "Done! Check logs with: sudo journalctl -u iems5718-shop -f"
