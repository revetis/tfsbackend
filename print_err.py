import json

try:
    with open('err_response.json', 'r', encoding='utf-16') as f:
        data = json.load(f)
    print(json.dumps(data, indent=2))
except Exception as e:
    print(f"Error: {e}")
