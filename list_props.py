import json

try:
    with open('current_mapping.json', 'r', encoding='utf-16') as f:
        data = json.load(f)
    
    props = data['products']['mappings']['properties']
    print(json.dumps(list(props.keys()), indent=2))
except Exception as e:
    print(f"Error: {e}")
