import json

with open('current_mapping.json', 'r', encoding='utf-16') as f:
    data = json.load(f)

variants_mapping = data['products']['mappings']['properties'].get('variants')
print(json.dumps(variants_mapping, indent=2))
