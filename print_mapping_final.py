import json

try:
    with open('mapping_final.json', 'r', encoding='utf-16') as f:
        data = json.load(f)
    print(json.dumps(data['products']['mappings']['properties'], indent=2))
except Exception as e:
    print(f"Error: {e}")
