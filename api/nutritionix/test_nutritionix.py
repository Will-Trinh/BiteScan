"""
test_nutritionix.py - Manual integration tests for Nutritionix API endpoints

Run this to verify the API proxy works correctly before integrating with Android.

Usage:
    python test_nutritionix.py

Requirements:
    - NUTRITIONIX_APP_ID and NUTRITIONIX_APP_KEY must be set in environment
    - nutritionapi.py server must be running (or run tests against deployed URL)
"""

import requests
import json
from typing import Dict, Any

# Configuration
BASE_URL = "http://localhost:8080"  # Change if deployed elsewhere

def print_test_header(test_name: str):
    """Print a formatted test header."""
    print(f"\n{'='*60}")
    print(f"TEST: {test_name}")
    print('='*60)

def print_result(response: requests.Response):
    """Print formatted response details."""
    print(f"Status: {response.status_code}")
    print(f"Response:")
    print(json.dumps(response.json(), indent=2))

def test_health():
    """Test the health check endpoint."""
    print_test_header("Health Check")
    response = requests.get(f"{BASE_URL}/")
    print_result(response)
    assert response.status_code == 200
    assert response.json()["ok"] == True
    print("✅ PASSED")

def test_natural_common_item():
    """Test natural endpoint with a common grocery item."""
    print_test_header("Natural Query - Common Item (Banana)")
    response = requests.post(
        f"{BASE_URL}/api/nutritionix/natural",
        json={"query": "banana"}
    )
    print_result(response)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["macros"] is not None
    assert data["macros"]["calories"] > 0
    print("✅ PASSED")

def test_natural_with_quantity():
    """Test natural endpoint with quantity."""
    print_test_header("Natural Query - With Quantity (2 eggs)")
    response = requests.post(
        f"{BASE_URL}/api/nutritionix/natural",
        json={"query": "2 eggs"}
    )
    print_result(response)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["macros"]["protein"] > 0
    print("✅ PASSED")

def test_natural_branded_item():
    """Test natural endpoint with branded item."""
    print_test_header("Natural Query - Branded Item (Coca Cola)")
    response = requests.post(
        f"{BASE_URL}/api/nutritionix/natural",
        json={"query": "coca cola 12 oz"}
    )
    print_result(response)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] in ["ok", "missing"]  # May vary
    print("✅ PASSED")

def test_natural_obscure_item():
    """Test natural endpoint with item that likely won't match."""
    print_test_header("Natural Query - Obscure Item (Should return missing)")
    response = requests.post(
        f"{BASE_URL}/api/nutritionix/natural",
        json={"query": "xyzabc123nonsense"}
    )
    print_result(response)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "missing"
    assert data["macros"] is None
    print("✅ PASSED")

def test_natural_empty_query():
    """Test natural endpoint with empty query."""
    print_test_header("Natural Query - Empty Query (Should fail)")
    response = requests.post(
        f"{BASE_URL}/api/nutritionix/natural",
        json={"query": ""}
    )
    print_result(response)
    assert response.status_code == 400
    print("✅ PASSED")

def test_instant_search():
    """Test instant search endpoint."""
    print_test_header("Instant Search - Autocomplete (chi)")
    response = requests.get(
        f"{BASE_URL}/api/nutritionix/instant",
        params={"query": "chi"}
    )
    print_result(response)
    assert response.status_code == 200
    data = response.json()
    assert "common" in data
    assert "branded" in data
    print("✅ PASSED")

def test_instant_empty_query():
    """Test instant search with empty query."""
    print_test_header("Instant Search - Empty Query (Should fail)")
    response = requests.get(
        f"{BASE_URL}/api/nutritionix/instant",
        params={"query": ""}
    )
    print_result(response)
    assert response.status_code == 400
    print("✅ PASSED")

def run_all_tests():
    """Run all integration tests."""
    print("\n" + "="*60)
    print("NUTRITIONIX API INTEGRATION TESTS")
    print("="*60)
    print(f"Testing against: {BASE_URL}")

    tests = [
        test_health,
        test_natural_common_item,
        test_natural_with_quantity,
        test_natural_branded_item,
        test_natural_obscure_item,
        test_natural_empty_query,
        test_instant_search,
        test_instant_empty_query,
    ]

    passed = 0
    failed = 0

    for test in tests:
        try:
            test()
            passed += 1
        except AssertionError as e:
            print(f"❌ FAILED: {e}")
            failed += 1
        except requests.exceptions.ConnectionError:
            print(f"❌ FAILED: Cannot connect to {BASE_URL}")
            print("   Make sure the server is running: python nutritionapi.py")
            break
        except Exception as e:
            print(f"❌ FAILED: Unexpected error: {e}")
            failed += 1

    print("\n" + "="*60)
    print(f"RESULTS: {passed} passed, {failed} failed")
    print("="*60)

if __name__ == "__main__":
    run_all_tests()

