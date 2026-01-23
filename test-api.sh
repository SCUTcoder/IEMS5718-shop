#!/bin/bash

# API Test Script for IEMS5718 Shop Backend

echo "======================================"
echo "  IEMS5718 Shop Backend API Test"
echo "======================================"
echo ""

API_URL="http://localhost:8080/api"

echo "Testing backend API at: $API_URL"
echo ""

# Test 1: Get all products
echo "📋 Test 1: GET /api/products"
echo "--------------------------------------"
curl -s -w "\nHTTP Status: %{http_code}\n" "$API_URL/products" | head -20
echo ""
echo ""

# Test 2: Get single product
echo "📦 Test 2: GET /api/products/1"
echo "--------------------------------------"
curl -s -w "\nHTTP Status: %{http_code}\n" "$API_URL/products/1"
echo ""
echo ""

# Test 3: Search products
echo "🔍 Test 3: GET /api/products/search?q=laptop"
echo "--------------------------------------"
curl -s -w "\nHTTP Status: %{http_code}\n" "$API_URL/products/search?q=laptop"
echo ""
echo ""

# Test 4: Get products by category
echo "📁 Test 4: GET /api/products/category/Electronics"
echo "--------------------------------------"
curl -s -w "\nHTTP Status: %{http_code}\n" "$API_URL/products/category/Electronics" | head -20
echo ""
echo ""

echo "======================================"
echo "  API Test Completed!"
echo "======================================"
echo ""
echo "If all tests show HTTP Status: 200, your backend is working correctly!"
echo "If you see connection errors, make sure the backend is running."
echo ""
